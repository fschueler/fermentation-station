package de.fschueler.fermentation.config

import pureconfig._
import pureconfig.generic.auto._

case class ServiceConf(host: String, port: Int)
