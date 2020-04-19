package de.fschueler.fermentation.domain

import java.time.ZonedDateTime
import java.util.UUID

import cats.Applicative
import cats.effect.Sync
import io.circe.Decoder.Result
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}
import squants.thermal.{Celsius, Temperature}
import io.circe._
import io.circe.generic.semiauto._

case class Experiment(id: UUID, name: String, goalTemp: Temperature, goalHumidity: Humidity, created: ZonedDateTime)

object Experiment {
  implicit val experimentEncoder: Encoder[Experiment] = new Encoder[Experiment] {
    override def apply(a: Experiment): Json = Json.obj(
      ("id", Json.fromString(a.id.toString)),
      ("name", Json.fromString(a.name)),
      ("goalTemp", Json.fromDouble(a.goalTemp.value).getOrElse(Json.Null)),
      ("goalHumidity", Json.fromDouble(a.goalHumidity.value).getOrElse(Json.Null)),
      ("created", Json.fromString(a.created.toString))
    )
  }

  implicit val experimentDecoder: Decoder[Experiment] = new Decoder[Experiment] {
    override def apply(c: HCursor): Result[Experiment] =
      for {
        id           <- c.downField("id").as[UUID]
        name         <- c.downField("name").as[String]
        goalTemp     <- c.downField("goalTemp").as[Double]
        goalHumidity <- c.downField("goalHumidity").as[Double]
        created      <- c.downField("created").as[ZonedDateTime]
      } yield Experiment(id, name, Temperature(goalTemp, Celsius), Humidity(goalHumidity), created)
  }

  implicit def readingEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Experiment] =
    jsonEncoderOf[F, Experiment]

  implicit def readingEntityDecoder[F[_]: Sync]: EntityDecoder[F, Experiment] =
    jsonOf[F, Experiment]
}
