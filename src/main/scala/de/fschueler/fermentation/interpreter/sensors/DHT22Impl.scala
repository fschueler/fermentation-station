package de.fschueler.fermentation.interpreter.sensors

import de.fschueler.fermentation.algebra.Sensor
import de.fschueler.fermentation.config.DHT22Configuration
import de.fschueler.fermentation.Measurement
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.RaspiPin
import cats.syntax.all._
import cats.Applicative
import scala.concurrent.duration._
import com.pi4j.io.gpio.Pin
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.PinPullResistance

class DHT22Impl[F[_]: Applicative](gpioController: GpioController) extends Sensor[F] {

  import DHT22Impl._

  private val pinNumber = RaspiPin.GPIO_04 // TODO take pin from constructor
  private val name = "dht-22"
  private val pin: GpioPinDigitalMultipurpose = gpioController.provisionDigitalMultipurposePin(pinNumber, PinMode.DIGITAL_OUTPUT)
  pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF)
  pin.addListener(new StartSignalListener())

  override def getMeasurement: F[Measurement] = {
    readData() *> println("waiting 2s").pure[F] *> Thread.sleep(5000).pure[F] *>
    Measurement(1L, 12.0, 80.0).pure[F]
  }

  private def readData(): F[Unit] = {
    println("Reading data...")
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    // send start signal
    pin.setMode(PinMode.DIGITAL_OUTPUT)
    pin.low()
    Thread.sleep(10)
    pin.high()

    // get response signal
    pin.setMode(PinMode.DIGITAL_INPUT)

    println("Finished reading!")
  }.pure[F]
}

object DHT22Impl {
  // time to separate ZERO and ONE signals
  private val LONGEST_ZERO: Duration = 50 microseconds

  // minimum time to wait between sensor reads
  private val MIN_BETWEEN_READS = 2500 milliseconds

}

class StartSignalListener() extends GpioPinListenerDigital {

  override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
    println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                + event.getState())
  }

  
}