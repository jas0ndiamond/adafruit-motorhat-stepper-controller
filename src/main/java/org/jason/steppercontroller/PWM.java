package org.jason.steppercontroller;

import java.io.IOException;

import org.jason.steppercontroller.exceptions.HatException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class PWM {
	//some variables not used by the hat's pwm operation
	
	// Registers/etc.
	private final static int MODE1 = 0x00;
	private final static int MODE2 = 0x01;
	
	@SuppressWarnings("unused")
	private final static int SUBADR1 = 0x02;
	
	@SuppressWarnings("unused")
	private final static int SUBADR2 = 0x03;
	
	@SuppressWarnings("unused")
	private final static int SUBADR3 = 0x04;
	
	private final static int PRESCALE = 0xFE;
	private final static int LED0_ON_L = 0x06;
	private final static int LED0_ON_H = 0x07;
	private final static int LED0_OFF_L = 0x08;
	private final static int LED0_OFF_H = 0x09;
	private final static int ALL_LED_ON_L = 0xFA;
	private final static int ALL_LED_ON_H = 0xFB;
	private final static int ALL_LED_OFF_L = 0xFC;
	private final static int ALL_LED_OFF_H = 0xFD;

	// Bits
	@SuppressWarnings("unused")
	private final static int RESTART = 0x80;
	
	private final static int SLEEP = 0x10;
	private final static int ALLCALL = 0x01;
	
	@SuppressWarnings("unused")
	private final static int INVRT = 0x10;
	
	private final static int OUTDRV = 0x04;

	private I2CBus bus;
	private I2CDevice servoDriver;

	//TODO: what is this?
	public final static int SERVO_ADDRESS = 0x40;
	
	private int deviceAddr;

	private boolean verbose = false;

	public PWM() throws HatException, IOException  {
		this(SERVO_ADDRESS, true);
	}
	
	public PWM(int address) throws HatException, IOException  {
		this(address, true);
	}

	public PWM(int address, boolean resetPWM) throws IOException, HatException  {
		this.deviceAddr = address;

		// Get I2C bus
		try {
		bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPI version
		if (verbose)
			System.out.println("Connected to bus. OK.");
		}
		catch (UnsupportedBusNumberException e) {
			throw new HatException(e);
		}
		
		// Get the device itself
		servoDriver = bus.getDevice(address);
		if (verbose)
			System.out.println("Connected to device. OK.");

	    /*
	     * ********************************************************** 
	     * Jason - 
	     * 
	     * Optionally skip the reset. Deviates from the reference implementation.
	     * 
	     * We may want the motors to hold between instantiations of PWM.
	     * Treat the Hat like a sort of IO resource - it's up to the user to cut power 
	     * to the Hat's motors when they're done, using MotorControl.releaseMotor(int).
	     * **********************************************************
	     */  
		if(resetPWM) {
			if (verbose) {
				System.out.println("Resetting AdafruitHAT MODE1 (without SLEEP) and MODE2");
			}
			resetPWM();
		}
		

		if (verbose)
			System.out.printf("01 - Writing 0x%02x to register 0x%02x\n", OUTDRV, MODE2);
		this.servoDriver.write(MODE2, (byte) OUTDRV);

		if (verbose)
			System.out.printf("02 - Writing 0x%02x to register 0x%02x\n", ALLCALL, MODE1);
		this.servoDriver.write(MODE1, (byte) ALLCALL);

		delay(5); // wait for oscillator

		int mode1 = this.servoDriver.read(MODE1);
		if (verbose)
			System.out.printf("03 - Device 0x%02x returned 0x%02x from register 0x%02x\n", this.deviceAddr, mode1, MODE1);
		
		mode1 = mode1 & ~SLEEP; // wake up (reset sleep)

		if (verbose)
			System.out.printf("04 - Writing 0x%02x to register 0x%02x\n", mode1, MODE1);
		this.servoDriver.write(MODE1, (byte) mode1);
		delay(5); // wait for oscillator
	}
	
	public void resetPWM() throws IOException
	{
		this.setAllPWM((byte) 0, (byte) 0);
	}

	private static void delay(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	public void setAllPWM(byte on, byte off) throws IOException {
		// Sets a all PWM channels
		if (verbose) {
			System.out.printf("05 - Writing 0x%02x to register 0x%02x\n", (on & 0xFF), ALL_LED_ON_L);
			System.out.printf("06 - Writing 0x%02x to register 0x%02x\n", (on >> 8), ALL_LED_ON_H);
			System.out.printf("07 - Writing 0x%02x to register 0x%02x\n", (off & 0xFF), ALL_LED_OFF_L);
			System.out.printf("08 - Writing 0x%02x to register 0x%02x\n", (off >> 8), ALL_LED_OFF_H);
		}
		this.servoDriver.write(ALL_LED_ON_L, (byte) (on & 0xFF));
		this.servoDriver.write(ALL_LED_ON_H, (byte) (on >> 8));
		this.servoDriver.write(ALL_LED_OFF_L, (byte) (off & 0xFF));
		this.servoDriver.write(ALL_LED_OFF_H, (byte) (off >> 8));
	}

	public void setPWM(int channel, short on, short off) throws IOException {
		// Sets a single PWM channel
		if (verbose) {
			System.out.printf("ON:0x%02x, OFF:0x%02x\n", on, off);
			System.out.printf("09 - Writing 0x%02x to register 0x%02x\n", (on & 0xFF), LED0_ON_L + 4 * channel);
			System.out.printf("10 - Writing 0x%02x to register 0x%02x\n", (on >> 8) & 0xFF, LED0_ON_H + 4 * channel);
			System.out.printf("11 - Writing 0x%02x to register 0x%02x\n", (off & 0xFF), LED0_OFF_L + 4 * channel);
			System.out.printf("12 - Writing 0x%02x to register 0x%02x\n", (off >> 8) & 0xFF, LED0_OFF_H + 4 * channel);
		}
		this.servoDriver.write(LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
		this.servoDriver.write(LED0_ON_H + 4 * channel, (byte) ((on >> 8) & 0xFF));
		this.servoDriver.write(LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
		this.servoDriver.write(LED0_OFF_H + 4 * channel, (byte) ((off >> 8) & 0xFF));
	}

	public void setPWMFreq(int freq) throws IOException {
		// Sets the PWM frequency
		double preScaleVal = 25000000.0; // 25MHz
		preScaleVal /= 4096.0; // 12-bit
		preScaleVal /= (float) freq;
		preScaleVal -= 1.0;
		if (verbose) {
			System.out.println("Setting PWM frequency to " + freq + " Hz");
			System.out.println("Estimated pre-scale:" + preScaleVal);
		}
		double preScale = Math.floor(preScaleVal + 0.5);
		if (verbose)
			System.out.println("Final pre-scale: " + preScale);

		int oldMode = this.servoDriver.read(MODE1);
		byte newMode = (byte) ((oldMode & 0x7F) | 0x10); // sleep
		if (verbose) {
			System.out.printf("13 - Writing 0x%02x to register 0x%02x\n", newMode, MODE1);
			System.out.printf("14 - Writing 0x%02x to register 0x%02x\n", (byte) (Math.floor(preScale)), PRESCALE);
			System.out.printf("15 - Writing 0x%02x to register 0x%02x\n", oldMode, MODE1);
		}
		this.servoDriver.write(MODE1, newMode); // go to sleep
		this.servoDriver.write(PRESCALE, (byte) (Math.floor(preScale)));
		this.servoDriver.write(MODE1, (byte) oldMode);
		delay(5);
		if (verbose)
			System.out.printf("16 - Writing 0x%02x to register 0x%02x\n", (oldMode | 0x80), MODE1);
		this.servoDriver.write(MODE1, (byte) (oldMode | 0x80));
	}

	public void close() throws IOException {
		if(bus != null) {
			bus.close();
		}
		
	}
}
