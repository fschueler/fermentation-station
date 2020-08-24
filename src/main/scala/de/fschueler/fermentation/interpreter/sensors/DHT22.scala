package de.fschueler.fermentation.interpreter.sensors

import de.fschueler.fermentation.algebra.Sensor
import de.fschueler.fermentation.Measurement

class DHT22[F[_]] extends Sensor[F] {

  override def getMeasurement: F[Measurement] = ???

  
}