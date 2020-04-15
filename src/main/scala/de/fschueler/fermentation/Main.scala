package de.fschueler.fermentation

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import de.fschueler.fermentation.config.ServiceConf
import de.fschueler.fermentation.controller.Server
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val configE: Either[ConfigReaderFailures, ServiceConf] = ConfigSource.default.load[ServiceConf]

    val code: EitherT[IO, ConfigReaderFailures, ExitCode] = for {
      config   <- EitherT.fromEither[IO](configE)
      exitCode <- EitherT.liftF(Server.stream[IO](config).compile.drain.as(ExitCode.Success))
    } yield exitCode

    code.fold(
      configReaderFailure => ExitCode.Error,
      exitCode => exitCode
    )
  }
}
