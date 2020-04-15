package de.fschueler.fermentation.algebra

import java.util.UUID

import de.fschueler.fermentation.domain.Experiment

trait ExperimentStore[F[_]] {
  def store(experiment: Experiment): F[Unit]

  def get(experimentId: UUID): F[Option[Experiment]]
}
