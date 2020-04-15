package de.fschueler.fermentation.algebra

import de.fschueler.fermentation.domain.{Experiment, Reading}

trait ExperimentController[F[_]] {
  def process(reading: Reading, experiment: Experiment): F[Unit]
}
