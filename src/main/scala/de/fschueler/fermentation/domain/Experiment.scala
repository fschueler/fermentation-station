package de.fschueler.fermentation.domain

import java.time.ZonedDateTime
import java.util.UUID

case class Experiment(id: UUID, name: String, goalTemp: Double, goalHumidity: Double, created: ZonedDateTime) {}
