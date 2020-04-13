package de.fschueler.fermentation.controller

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import de.fschueler.fermentation.algebra.ReadingStore
import de.fschueler.fermentation.domain.Reading
import io.circe.{Encoder, Json}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}

object Routes {

  implicit def readingSeqEncoder(implicit readingEncoder: Encoder[Reading]): Encoder[Seq[Reading]] =
    new Encoder[Seq[Reading]] {
      override def apply(a: Seq[Reading]): Json = Json.arr(a.map(readingEncoder.apply): _*)
    }

  implicit def readingSeqEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Seq[Reading]] =
    jsonEncoderOf[F, Seq[Reading]]

  def readingRoutes[F[_]: Sync](readingStore: ReadingStore[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "readings" =>
        for {
          readings <- readingStore.getAll
          response <- Ok(readings)
        } yield response

      case req @ POST -> Root / "reading" =>
        for {
          reading  <- req.as[Reading]
          _        <- readingStore.store(reading)
          response <- Ok("""Reading stored""")
        } yield response
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
