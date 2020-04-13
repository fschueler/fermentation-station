package de.fschueler.fermentation.interpreter

import cats.Applicative
import cats.implicits._
import de.fschueler.fermentation.algebra.ReadingStore
import de.fschueler.fermentation.domain.Reading

class InMemoryReadingStore[F[_]: Applicative] extends ReadingStore[F] {
  private var readings: Seq[Reading] = List.empty[Reading]

  override def store(reading: Reading): F[Unit] = {
    readings = readings :+ reading
  }.pure[F]

  override def getAll: F[Seq[Reading]] = readings.pure[F]
}
