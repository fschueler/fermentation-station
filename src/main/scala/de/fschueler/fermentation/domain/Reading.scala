package de.fschueler.fermentation.domain

import cats.Applicative
import cats.effect.Sync
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

/** Represents a temperature and humidity reading.
  *
  * @param temp The temperature as read from the sensor.
  * @param humidity The humidity as read from the sensor.
  */
final case class Reading(temp: Double, humidity: Double)

object Reading {
  implicit val readingEncoder: Encoder[Reading] = new Encoder[Reading] {
    final def apply(a: Reading): Json = Json.obj(
      ("temperature", Json.fromDouble(a.temp).getOrElse(Json.Null)),
      ("humidity", Json.fromDouble(a.humidity).getOrElse(Json.Null)),
    )
  }

  implicit def readingEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Reading] =
    jsonEncoderOf[F, Reading]

  implicit val readingDecoder: Decoder[Reading] = new Decoder[Reading] {
    override def apply(c: HCursor): Result[Reading] = for {
      temp <- c.downField("temperature").as[Double]
      humi <- c.downField("humidity").as[Double]
    } yield Reading(temp, humi)
  }

  implicit def readingEntityDecoder[F[_]: Sync]: EntityDecoder[F, Reading] =
    jsonOf[F, Reading]
}
