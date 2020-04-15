package de.fschueler.fermentation.interpreter

import cats.Applicative
import cats.implicits._
import de.fschueler.fermentation.algebra.ExperimentController
import de.fschueler.fermentation.domain.{Experiment, Reading}

class SimpleExperimentController[F[_]: Applicative] extends ExperimentController[F] {
  private val EPSILON = 1.0

  override def process(reading: Reading, experiment: Experiment): F[Unit] = {
    if (reading.temperature != experiment.goalTemp) {
      println("Adjusting temperature...")
    }

    if (reading.humidity != experiment.goalTemp) {
      println("Adjusting humidity...")
    }

    ().pure[F]
  }
}
