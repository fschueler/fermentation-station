package de.fschueler.fermentation.interpreter.sensors

import de.fschueler.fermentation.algebra.Sensor
import de.fschueler.fermentation.Measurement
import cats.effect.Timer

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import cats.data.State
import cats.syntax.all._
import cats.effect.Concurrent

class MockDHT22[F[_]](
  implicit F: Concurrent[F],
  timer: Timer[F]
) extends Sensor[F] {

  import MockDHT22._

  override def getMeasurement: F[Measurement] =
    for {
      seed <- timer.clock.realTime(TimeUnit.MILLISECONDS)
    } yield createProbeReading.runA(Seed(seed)).value

  private val nextLong: State[Seed, Long]     = State(seed => (seed.next, seed.long))
  private val nextDouble: State[Seed, Double] = nextLong.map(long => long.toDouble)

  private def createProbeReading: State[Seed, Measurement] =
    for {
      timestamp <- nextLong
      temp      <- nextDouble
      humidity  <- nextDouble
    } yield Measurement(timestamp, temp, humidity)

}

object MockDHT22 {
  final case class Seed(long: Long) {
    def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
  }
}
