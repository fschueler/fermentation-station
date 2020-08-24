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
import de.fschueler.fermentation.interpreter.sensors.MockDHT22
import de.fschueler.fermentation.algebra.Sensor

sealed trait Event
case class Text(value: String) extends Event
case class Measurement(timestamp: Long, temp: Double, humidity: Double) extends Event
case object Quit               extends Event

final case class Seed(long: Long) {
  def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
}

class EventService[F[_]](eventsTopic: Topic[F, Event], interrupter: SignallingRef[F, Boolean], dht22: Sensor[F])(
  implicit F: Concurrent[F],
  timer: Timer[F]
) {

  def probeTemperatureAndHumidity: F[Measurement] = dht22.getMeasurement

  val turnOnHeatingMat: F[Unit] = F.delay(println("heating: ON"))
  val turnOffHeatingMat: F[Unit] = F.delay(println("heating: OFF"))

  val turnOnVaporizer: F[Unit] = F.delay(println("vaporizer: ON"))
  val turnOffVaporizer: F[Unit] = F.delay(println("vaporizer: OFF"))

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
  val targetHum: Double = 80.0

  def heatingMat: Pipe[F, Event, Unit] = source => source.flatMap {
    case Measurement(time, temp, _) =>
      val logReading = Stream.eval(F.delay(println(s"Time: $time, temp: $temp")))
      val adjustTemp = Stream.eval(if (temp < targetTemp) turnOnHeatingMat else turnOffHeatingMat)
      logReading ++ adjustTemp
    case Quit => Stream.eval(interrupter.set(true))
    case _ => Stream.empty
  }

  def vaporizer: Pipe[F, Event, Unit] = source => source.flatMap {
    case Measurement(time, _, humidity) =>
      val logReading = Stream.eval(F.delay(println(s"Time: $time, humidity: $humidity")))
      val adjustHumi = Stream.eval(if (humidity < targetHum) turnOnVaporizer else turnOffVaporizer)
      logReading ++ adjustHumi
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
    service = new EventService[IO](topic, signal, new MockDHT22())
    _ <- service.startPublisher.concurrently(service.startSubscribers)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = program.compile.drain.as(ExitCode.Success)
}
