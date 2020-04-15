package de.fschueler.fermentation

import java.time.ZonedDateTime
import java.util.UUID

import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import cats.effect._
import org.http4s.circe._
import org.http4s.implicits._
import de.fschueler.fermentation.controller.Routes
import de.fschueler.fermentation.domain.Reading
import de.fschueler.fermentation.interpreter.InMemoryReadingStore
import org.http4s.{EntityDecoder, Method, Request, Response, Status}

class ReadingControllerSpec extends munit.FunSuite {

  val id      = UUID.randomUUID()
  val ts      = ZonedDateTime.now()
  val temp    = 12.5
  val humi    = 53.0
  val reading = Reading(id, temp, humi, ts)

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

  test("Insert a reading") {
    val store = new InMemoryReadingStore[IO]

    val insertIO: IO[Response[IO]] = Routes
      .readingRoutes(store)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/reading").withEntity(reading.asJson)
      )

    val expectedJson: Json = json"""{"stored": "true"}"""
    check[Json](insertIO, Status.Ok, Some(expectedJson))
  }

  test("Get readings") {
    val store = new InMemoryReadingStore[IO]
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

  test("insert and retrieve a reading") {
    val store = new InMemoryReadingStore[IO]
    val insertIO: IO[Response[IO]] = Routes
      .readingRoutes(store)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/reading").withEntity(reading.asJson)
      )

    val expectedJsonAfterInsert: Json = json"""{"stored": "true"}"""
    check[Json](insertIO, Status.Ok, Some(expectedJsonAfterInsert))

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
