package de.fschueler.fermentation.controller

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import de.fschueler.fermentation.config.ServiceConf
import de.fschueler.fermentation.interpreter.{InMemoryExperimentStore, InMemoryReadingStore, SimpleExperimentController}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_]: ConcurrentEffect](
    config: ServiceConf
  )(implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      readingAlg           = new InMemoryReadingStore[F]
      experimentAlg        = new InMemoryExperimentStore[F]
      experimentController = new SimpleExperimentController[F]

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        Routes
          .readingRoutes[F](
            readingAlg,
            experimentAlg,
            experimentController
          )
        )
        .orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(config.port, config.host)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
