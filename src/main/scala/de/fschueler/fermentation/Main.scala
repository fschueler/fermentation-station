package de.fschueler.fermentation

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import de.fschueler.fermentation.controller.Server

object Main extends IOApp {
  def run(args: List[String]) =
    Server.stream[IO].compile.drain.as(ExitCode.Success)
}