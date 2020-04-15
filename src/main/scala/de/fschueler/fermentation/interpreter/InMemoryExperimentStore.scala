package de.fschueler.fermentation.interpreter

import java.util.UUID

import cats.Applicative
import cats.implicits._
import de.fschueler.fermentation.algebra.ExperimentStore
import de.fschueler.fermentation.domain.Experiment

class InMemoryExperimentStore[F[_]: Applicative] extends ExperimentStore[F] {
  private var experiments = Map.empty[UUID, Experiment]

  override def store(experiment: Experiment): F[Unit] = {
    experiments = experiments + (experiment.id -> experiment)
  }.pure[F]

  override def get(experimentId: UUID): F[Option[Experiment]] = experiments.get(experimentId).pure[F]
}
