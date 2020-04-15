package de.fschueler.fermentation.controller

import cats.Applicative
import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import de.fschueler.fermentation.algebra.{ExperimentController, ExperimentStore, ReadingStore}
import de.fschueler.fermentation.domain.Reading
import io.circe.{Encoder, Json}
import io.circe.literal._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes, Response}

object Routes {

  implicit def readingSeqEncoder(implicit readingEncoder: Encoder[Reading]): Encoder[Seq[Reading]] =
    new Encoder[Seq[Reading]] {
      override def apply(a: Seq[Reading]): Json = Json.arr(a.map(readingEncoder.apply): _*)
    }

  implicit def readingSeqEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Seq[Reading]] =
    jsonEncoderOf[F, Seq[Reading]]

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

      case req @ POST -> Root / "reading" =>
        (for {
          reading    <- OptionT.liftF(req.as[Reading])
          experiment <- OptionT(experimentStore.get(reading.experimentId))
          _          <- OptionT.liftF(readingStore.store(reading))
          _          <- OptionT.liftF(experimentController.process(reading, experiment))
        } yield Ok(json"""{"stored": "true"}"""))
          .getOrElseF(BadRequest())
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
