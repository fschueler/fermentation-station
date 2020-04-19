package de.fschueler.fermentation.domain

case class Humidity(value: Double) extends AnyVal {
  def approx(h: Humidity)(implicit tolerance: Humidity): Boolean =
    value - h.value <= tolerance.value
}
