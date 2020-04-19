package de.fschueler.fermentation.domain

import java.time.ZonedDateTime
import java.util.UUID

import cats.Applicative
import cats.effect.Sync
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import squants.thermal.{Celsius, Temperature}

/** Represents a temperature and humidity reading.
  *
  * @param experimentId The ID of the experiment this reading belongs to.
  * @param temperature The temperature as read from the sensor.
  * @param humidity The humidity as read from the sensor.
  * @param timestamp The timestamp of the reading from the sensor.
  */
final case class Reading(experimentId: UUID, temperature: Temperature, humidity: Humidity, timestamp: ZonedDateTime)

object Reading {
  implicit val jsonReadingEncoder: Encoder[Reading] = new Encoder[Reading] {
    final def apply(a: Reading): Json = Json.obj(
      ("experimentId", Json.fromString(a.experimentId.toString)),
      ("temperature", Json.fromDouble(a.temperature.value).getOrElse(Json.Null)),
      ("humidity", Json.fromDouble(a.humidity.value).getOrElse(Json.Null)),
      ("timestamp", Json.fromString(a.timestamp.toString))
    )
  }

  implicit val jsonReadingDecoder: Decoder[Reading] = new Decoder[Reading] {
    override def apply(c: HCursor): Result[Reading] =
      for {
        id   <- c.downField("experimentId").as[UUID]
        temp <- c.downField("temperature").as[Double]
        humi <- c.downField("humidity").as[Double]
        ts   <- c.downField("timestamp").as[ZonedDateTime]
      } yield Reading(id, Temperature(temp, Celsius), Humidity(humi), ts)
  }
}
