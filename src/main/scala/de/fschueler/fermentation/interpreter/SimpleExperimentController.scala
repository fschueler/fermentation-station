package de.fschueler.fermentation.interpreter

import cats.Applicative
import cats.implicits._
import de.fschueler.fermentation.algebra.ExperimentController
import de.fschueler.fermentation.domain.{Experiment, Humidity, Reading}
import squants.thermal.{Celsius, Temperature}

class SimpleExperimentController[F[_]: Applicative] extends ExperimentController[F] {
  implicit private val temperatureTolerance: Temperature = Temperature(1.0, Celsius)
  implicit private val humidityTolerance: Humidity       = Humidity(1.0)

  override def process(reading: Reading, experiment: Experiment): F[Unit] = {
    if (reading.temperature.approx(experiment.goalTemp)) {
      println("Adjusting temperature...")
    }

    if (reading.humidity.approx(experiment.goalHumidity)) {
      println("Adjusting humidity...")
    }

    ().pure[F]
  }
}
