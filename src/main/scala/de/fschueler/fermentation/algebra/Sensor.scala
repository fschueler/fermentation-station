package de.fschueler.fermentation.algebra

import de.fschueler.fermentation.Measurement

trait Sensor[F[_]] {

  def getMeasurement: F[Measurement]
}