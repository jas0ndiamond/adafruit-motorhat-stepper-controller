package org.jason.steppercontroller;

import java.io.IOException;

import org.jason.steppercontroller.exceptions.HatException;
import org.jason.steppercontroller.exceptions.MotorException;

public class AdafruitMotorHat
{
	private final static int HAT_ADDR = 0x60;
	private final static int DEFAULT_FREQ = 1600;
	
	private int freq;
	private int i2cAddr;

	//TODO: someday...
	//  private AdafruitDCMotor      motors[];
	private AdafruitStepperMotor steppers[];
	private PWM pwm;

	public AdafruitMotorHat() throws IOException, HatException 
	{
		this(HAT_ADDR, DEFAULT_FREQ);
	}
	
	public AdafruitMotorHat(int addr, int freq) throws IOException, HatException 
	{
		this(HAT_ADDR, DEFAULT_FREQ, true);
	}

	public AdafruitMotorHat(int addr, int freq, boolean resetPWM) throws IOException, HatException 
	{
		this.i2cAddr = addr;
		this.freq    = freq;

		//as of 2020, a v1 motor hat supports 2 stepper motors
		steppers = new AdafruitStepperMotor[2];
		
		pwm = new PWM(this.i2cAddr, resetPWM);
		pwm.setPWMFreq(this.freq);
	}

	public PWM getPWM() {
		return pwm;
	}
	
	void setPin(int pin, int value) throws IOException
	{
		if (pin < 0 || pin > 15)
			throw new RuntimeException("PWM pin must be between 0 and 15 inclusive : " + pin);
		if (value != 0 && value != 1)
			throw new RuntimeException("Pin value must be 0 or 1! " + value);
		if (value == 0)
			this.pwm.setPWM(pin, (short)0, (short)4096);
		if (value == 1)
			this.pwm.setPWM(pin, (short)4096, (short)0);
	}

	public AdafruitStepperMotor getStepper(int num) throws MotorException
	{
		if (num < 1 || num > 2)
			throw new MotorException("MotorHAT Stepper must be between 1 and 2 inclusive");
		return steppers[num-1];
	}
	
	public void addStepper(int num) throws MotorException {
		//num is 1 or 2
		if( num == 0 ) {
			steppers[0] = new AdafruitStepperMotor(this, 1);
		} else if (num == 1) {
			steppers[1] = new AdafruitStepperMotor(this, 2);
		}
		else
		{
			System.out.println("hat can't add invalid stepper");
		}
	}
	
	public void shutdown() {
		try {
			pwm.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
