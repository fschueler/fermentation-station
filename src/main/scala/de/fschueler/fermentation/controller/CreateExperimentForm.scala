package de.fschueler.fermentation.controller

import cats.Applicative
import cats.effect.Sync
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class CreateExperimentForm(name: String, goalTemp: Double, goalHumidity: Double)
object CreateExperimentForm {
  implicit val formEncoder: Encoder[CreateExperimentForm] = deriveEncoder[CreateExperimentForm]
  implicit val formDecoder: Decoder[CreateExperimentForm] = deriveDecoder[CreateExperimentForm]

  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, CreateExperimentForm] =
    jsonEncoderOf[F, CreateExperimentForm]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, CreateExperimentForm] =
    jsonOf[F, CreateExperimentForm]
}
