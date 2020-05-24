# Fermentation Station

A project to build hardware and software for a fermentation station.

The first part is to build a controller for a fermentation chamber as described in [The Noma Guide to Fermentation](https://www.amazon.com/Noma-Guide-Fermentation-lacto-ferments-Foundations/dp/1579657184/).

Fermentation often requires (or benefits from) a controlled temperature and humidity environment. One way to achieve that is
described in instruction in [The Noma Guide to Fermentation](https://www.amazon.com/Noma-Guide-Fermentation-lacto-ferments-Foundations/dp/1579657184/).
A thermostat and a humidistat which are connected to a heating mat and humidifier maintain the temperature and humidity
inside a styrofoam box. This is an easy, quick, and relatively cheap solution to ferment things such as koji or sourdough.

The goal of this project is to build a general ecosystem for fermentation projects that use flexible hardware and open software.
In the same spirit of experimentation we want to be able to control and observe aspects of fermentation which are not provided
by the out-of-the box thermostats. Hardware parts can be added and combined more freely and custom software allows for data collection,
project logging and, sharing. 

After all, systems of hardware and software already resemble interconnected networks of mycelium, bacteria, and yeasts ;)
 
>The mycelium stays in constant molecular communication with its environment, devising diverse enzymatic and chemical
>responses to complex challenges.”

\- *Paul Stamets* in: [Mycelium Running: How Mushrooms Can Help Save the World](https://www.amazon.com/Mycelium-Running-Mushrooms-Help-World/dp/1580085792/ref=sr_1_1?s=books&ie=UTF8&qid=1329830582&sr=1-1)

## Architecture

The project includes building hardware components and software components.

### Hardware
- [ ] An isolated box of some sort (wood, styrofoam, a cooler, a paper box)
- [x] Raspberry Pi or other computer that can be connected to sensors using GPIO pins
- [x] Sensors to measure humidity and temperature (DHT22)
- [x] Heating mat to achieve desired temperature
- [x] humidifier to achieve desired humidity
- [x] Mhz controllable outlets
- [x] Mhz communication device to control humidifier and heating mat (via outlets)

## Software

- [ ] Architecture diagram
- [ ] Application server that receives measurements, aggregates them, and sends signals to actuators in case of adjustments
- [ ] Software which runs on Raspberry Pi Zero which sends measurements to the application server
- [ ] Dashboard to show temperature and humidity readings
- [ ] Project management / Log service