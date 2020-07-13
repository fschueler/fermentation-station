package de.fschueler.fermentation

import cats.syntax.all._
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import fs2.concurrent.Signal
import fs2.concurrent.SignallingRef
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import cats.data.State

sealed trait Event
case class Text(value: String) extends Event
case class Measurement(timestamp: Long, temp: Double, humidity: Double) extends Event
case object Quit               extends Event

final case class Seed(long: Long) {
  def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
}

class EventService[F[_]](eventsTopic: Topic[F, Event], interrupter: SignallingRef[F, Boolean])(
  implicit F: Concurrent[F],
  timer: Timer[F]
) {

  val nextLong: State[Seed, Long] = State(seed => (seed.next, seed.long))
  val nextDouble: State[Seed, Double] = nextLong.map(long => long.toDouble)

  def createProbeReading: State[Seed, Measurement] = for {
    timestamp <- nextLong
    temp <- nextDouble
    humidity <- nextDouble
  } yield Measurement(timestamp, temp, humidity)

  def probeTemperatureAndHumidity: F[Measurement] = for {
    seed <- timer.clock.realTime(TimeUnit.MILLISECONDS)
  } yield createProbeReading.runA(Seed(seed)).value

  // publishing 15 text events, then single quit event, then publish more text events
  def startPublisher: fs2.Stream[F, Unit] = {
    val readingEvents = eventsTopic.publish(
      Stream
        .awakeEvery[F](1.second)
        .zipRight(Stream.eval(probeTemperatureAndHumidity)).repeat
    )

    val quitEvent = Stream.eval(eventsTopic.publish1(Quit))

    (readingEvents.take(15) ++ quitEvent ++ readingEvents).interruptWhen(interrupter)
  }

  val targetTemp: Double = 23.0

  def heatingMat: Pipe[F, Event, Unit] = source => source.flatMap {
    case Measurement(time, temp, _) =>
      Stream.eval(F.delay(println(s"Time: $time, temp: $temp")))
    case Quit => Stream.eval(interrupter.set(true))
    case _ => Stream.empty
  }

  def vaporizer: Pipe[F, Event, Unit] = source => source.flatMap {
    case Measurement(time, _, humidity) =>
      Stream.eval(F.delay(println(s"Time: $time, humidity: $humidity")))
    case Quit => Stream.eval(interrupter.set(true))
    case _ => Stream.empty
  }

  // creating 3 subscripbers in a different period of time and join them to run concurrently
  def startSubscribers: Stream[F, Unit] = {

    val events: Stream[F, Event] = eventsTopic.subscribe(10)

    Stream(
      events.through(heatingMat),
      events.through(vaporizer),
    ).parJoin(2)
  }
}

object PubSubStream extends IOApp {
  val program = for {
    topic  <- Stream.eval(Topic[IO, Event](Text("Initial Event")))
    signal <- Stream.eval(SignallingRef[IO, Boolean](false))
    service = new EventService[IO](topic, signal)
    _ <- service.startPublisher.concurrently(service.startSubscribers)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.compile.drain.as(ExitCode.Success)
}
