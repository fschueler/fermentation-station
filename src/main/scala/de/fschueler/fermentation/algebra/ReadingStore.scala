package de.fschueler.fermentation.algebra

import de.fschueler.fermentation.domain.Reading

/* Stores readings and retrieves them. */
trait ReadingStore[F[_]] {
  def store(reading: Reading): F[Unit]
  def getAll: F[Seq[Reading]]
}
