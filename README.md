# adafruit-motorhat-stepper-controller
Control stepper motors connected to an Adafruit MotorHat

A sort-of-port-of the Adafruit Motor Hat Python library (https://github.com/adafruit/Adafruit-Motor-HAT-Python-Library) with some differences- see below. Drive connected stepper motors in Java. There are other Java ports of this library floating around, and plenty of stuff here is borrowed from those.

###You will need to use an embedded JDK like the EJDK or Zulu
https://blogs.oracle.com/jtc/introducing-the-ejdk.
https://www.azul.com/downloads/zulu-community/

Driving stepper motors requires realtime control, which is not guaranteed by JVMs based on Java SE.

Using the regular ol' JVM to drive stepper motors will run, however a non-embedded JVM will have garbage collection, optimizations, and scheduling that act as randomizers for signal timings. Your motors will probably spin, but the step transitions will not be smooth, the motors will heat up unduly, and damage to the hat/motors/pi becomes a possiblity.

## Differences from the Adafruit Python Library (and some of the reference implementations)
1. Hat shutdown or reset must be manually invoked via AdafruitMotorHat.shutdown().
2. I2C bus close() function exposed and is invoked by MotorControl.shutdown().
3. Stepper Motor objects not initialized upon Hat initialization. They are initialized by calls to MotorControl.addMotor().
4. Motors are referenced by a user-designated name rather than directly by the motor number or port.
5. Dead code removed.
6. Fixed bugs and inconsistencies managing speed and steps per revolution.
7. Some math is done on class instance instantiation rather than over and over again in commonly-used functions.
8. More debug output.
9. DC Motors not supported for now.

## Dependencies
1. Pi4J for I2C communication and PWM management.

## Usage
The MotorControl class is the facade and provides the API for managing hats and moving motors.

This is a motor controller library with a few client programs.

## Acknowledgements and Reference Implementations
* https://github.com/zugaldia/adafruit-motor-hat
* https://github.com/fcazalet/adafruit-motor-hat
* Others
