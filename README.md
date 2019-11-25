# adafruit-motorhat-stepper-controller
Control stepper motors connected to an Adafruit MotorHat

A sort-of-port-of the Adafruit Motor Hat Python library (https://github.com/adafruit/Adafruit-Motor-HAT-Python-Library). Drive connected stepper motors in Java. 

You will need to use an embedded JDK like the EJDK (https://blogs.oracle.com/jtc/introducing-the-ejdk). Driving stepper motors requires realtime control, which is not guaranteed by JVMs based on Java SE.

Using the regular ol' JVM to drive stepper motors will run, however a non-embedded JVM will have optimizations and scheduling that act as randomizers for signal timings. Your motors will probably spin, but the step transitions will not be smooth, the motors will heat up unduly, and damage to the hat/motors/pi becomes a possiblity.
