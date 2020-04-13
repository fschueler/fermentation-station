package de.fschueler.fermentation

import cats.implicits._
import io.circe._
import io.circe.syntax._
import cats.effect._
import org.http4s.circe._
import org.http4s.implicits._
import de.fschueler.fermentation.controller.Routes
import de.fschueler.fermentation.domain.Reading
import de.fschueler.fermentation.interpreter.InMemoryReadingStore
import org.http4s.{EntityDecoder, Method, Request, Response, Status}

class ReadingControllerSpec extends munit.FunSuite {

  test("Get status route") {
    val response: IO[Response[IO]] = Routes
      .statusRoutes[IO]
      .orNotFound
      .run(
        Request(method = Method.GET, uri = uri"/")
      )

    val expectedJson: Option[Json] = None

    check[Json](response, Status.Ok, expectedJson)
  }

  test("Get empty readings") {
    val store = new InMemoryReadingStore[IO]

    val response: IO[Response[IO]] = Routes
      .readingRoutes[IO](store)
      .orNotFound
      .run(
        Request(method = Method.GET, uri = uri"/readings")
      )

    val expectedJson: Json = Json.arr()

    check[Json](response, Status.Ok, Some(expectedJson))
  }

  test("Get readings after instertion") {
    val store = new InMemoryReadingStore[IO]

    val reading = Reading(12.5, 53.0)
    store.store(reading)

    val response: IO[Response[IO]] = Routes
      .readingRoutes[IO](store)
      .orNotFound
      .run(
        Request(method = Method.GET, uri = uri"/readings")
      )

    val expectedJson: Json = Json.arr(reading.asJson)

    check[Json](response, Status.Ok, Some(expectedJson))
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
    implicit ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp  = actual.unsafeRunSync()
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](actualResp.body.compile.toVector.unsafeRunSync().isEmpty)(expected =>
      actualResp.as[A].unsafeRunSync() == expected
    )

    statusCheck && bodyCheck
  }
}
