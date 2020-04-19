package de.fschueler.fermentation.controller

import java.time.ZonedDateTime
import java.util.UUID

import cats.Applicative
import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import de.fschueler.fermentation.algebra.{ExperimentController, ExperimentStore, ReadingStore}
import de.fschueler.fermentation.domain.{Experiment, Humidity, Reading}
import io.circe.{Encoder, Json}
import io.circe.literal._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Response, UrlForm}
import squants.thermal.{Celsius, Temperature}

object Routes {

  implicit def readingSeqEncoder(implicit readingEncoder: Encoder[Reading]): Encoder[Seq[Reading]] =
    new Encoder[Seq[Reading]] {
      override def apply(a: Seq[Reading]): Json = Json.arr(a.map(readingEncoder.apply): _*)
    }

  implicit def readingSeqEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Seq[Reading]] =
    jsonEncoderOf[F, Seq[Reading]]

  def experimentRoutes[F[_]: Sync](
    experimentStore: ExperimentStore[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "experiments" / "create" =>
        val experimentF = req.as[CreateExperimentForm].map { form =>
          val id = UUID.randomUUID()
          val ts = ZonedDateTime.now()
          Experiment(id, form.name, Temperature(form.goalTemp, Celsius), Humidity(form.goalHumidity), ts)
        }

        for {
          experiment <- experimentF
          _          <- experimentStore.store(experiment)
          response   <- Ok(experiment)
        } yield response
    }
  }

  implicit def readingEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Reading] =
    jsonEncoderOf[F, Reading]
  implicit def readingEntityDecoder[F[_]: Sync]: EntityDecoder[F, Reading] =
    jsonOf[F, Reading]

  def readingRoutes[F[_]: Sync](
    readingStore: ReadingStore[F],
    experimentStore: ExperimentStore[F],
    experimentController: ExperimentController[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "readings" =>
        for {
          readings <- readingStore.getAll
          response <- Ok(readings)
        } yield response

      case req @ POST -> Root / "readings" / "create" =>
        val res: OptionT[F, Json] = for {
          reading    <- OptionT.liftF(req.as[Reading])
          experiment <- OptionT(experimentStore.get(reading.experimentId))
          _          <- OptionT.liftF(readingStore.store(reading))
          _          <- OptionT.liftF(experimentController.process(reading, experiment))
        } yield json"""{"stored": "true"}"""

        res.value.flatMap(v => v.fold(BadRequest())(json => Ok(json)))
    }
  }

  def statusRoutes[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root => Ok()
    }
  }
}
