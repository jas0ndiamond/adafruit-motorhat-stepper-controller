package org.jason.steppercontroller;

import java.io.IOException;

import org.jason.steppercontroller.MotorControl.ServoCommand;
import org.jason.steppercontroller.MotorControl.Style;
import org.jason.steppercontroller.exceptions.MotorException;
import org.jason.steppercontroller.exceptions.StepException;

public class AdafruitStepperMotor
{
	public final static int PORT_M1_M2 = 1; // Port #1
	public final static int PORT_M3_M4 = 2; // Port #2

	private AdafruitMotorHat motorHat;
	
	//from the adafruit python implementation
	private final int MICROSTEPS = 8;
	private final int[] MICROSTEP_CURVE = new int[] {0, 50, 98, 142, 180, 212, 236, 250, 255};

	private static int DEFAULT_NB_STEPS = 200; // between 35 & 200. get from your motor specs

	private int PWMA = 8;
	private int AIN2 = 9;
	private int AIN1 = 10;
	private int PWMB = 13;
	private int BIN2 = 12;
	private int BIN1 = 11;

	private int stepsPerRevolution;

	private double secondsPerStep = 0.1;

	private long stepDelay = (long) (secondsPerStep * 1000L);
	private int currentStep = 0;

	private final static int[] SHUTDOWN_COIL_VALUES =  new int[] {0,0,0,0};
	
	public AdafruitStepperMotor(AdafruitMotorHat controller, int num) throws MotorException
	{
		this(controller, num, DEFAULT_NB_STEPS);
	}

	public AdafruitStepperMotor(AdafruitMotorHat motorHat, int num, int stepsPerRevolution) throws MotorException
	{
		this.motorHat = motorHat;
		this.stepsPerRevolution = stepsPerRevolution;

		this.secondsPerStep = 0.1;
		//this.steppingCounter = 0;
		this.currentStep = 0;

		if ((num - 1) == 0)
		{
			this.PWMA =  8;
			this.AIN2 =  9;
			this.AIN1 = 10;
			this.PWMB = 13;
			this.BIN2 = 12;
			this.BIN1 = 11;
		}
		else if ((num - 1) == 1)
		{
			this.PWMA = 2;
			this.AIN2 = 3;
			this.AIN1 = 4;
			this.PWMB = 7;
			this.BIN2 = 6;
			this.BIN1 = 5;
		}
		else
		{
			throw new MotorException("MotorHAT Stepper must be between 1 and 2 inclusive");
		}
	}   

	public void setSpeed(double rpm)
	{
		this.secondsPerStep = 60.0 / (this.stepsPerRevolution * rpm);
		this.stepDelay = (long) (secondsPerStep * 1000L);

		
		//this.steppingCounter = 0;
		//TODO: recalc step delay
	}

	public void shutdown() throws IOException
	{
//		this.mc.setPin(this.AIN2, 0);
//		this.mc.setPin(this.BIN1, 0);
//		this.mc.setPin(this.AIN1, 0);
//		this.mc.setPin(this.BIN2, 0);
		writeToCoils( SHUTDOWN_COIL_VALUES );
	}


	private int oneStep(ServoCommand dir, Style style) throws StepException, IOException
	{
		int pwmA = 255, pwmB = 255;

		//System.out.println("Initial Current Step: " + this.currentStep);


		
		// first determine what sort of stepping procedure we're up to
		if (style == Style.SINGLE)
		{
			if ((this.currentStep /(this.MICROSTEPS/2)) % 2 == 1)
			{
				// we're at an odd step, weird
				if (dir == ServoCommand.FORWARD)
					this.currentStep += this.MICROSTEPS / 2;
				else
					this.currentStep -= this.MICROSTEPS / 2;
			}
			else
			{
				if (dir == ServoCommand.FORWARD)
				{
					this.currentStep += this.MICROSTEPS;
				}
				else
				{
					this.currentStep -= this.MICROSTEPS;
				}
			}
			
			//System.out.println("Current Step: " + this.currentStep);
		}
		else if (style == Style.DOUBLE)
		{
			if (this.currentStep /(this.MICROSTEPS/2) % 2 == 0)
			{
				// we're at an even step, weird
				if (dir == ServoCommand.FORWARD)
					this.currentStep += this.MICROSTEPS/2;
				else
					this.currentStep -= this.MICROSTEPS/2;
			}
			else
			{
				// go to next odd step
				if (dir == ServoCommand.FORWARD)
					this.currentStep += this.MICROSTEPS;
				else
					this.currentStep -= this.MICROSTEPS;
			}
		}
		else if (style == Style.INTERLEAVE)
		{
			if (dir == ServoCommand.FORWARD)
				this.currentStep += (this.MICROSTEPS/2);
			else
				this.currentStep -= (this.MICROSTEPS/2);
		}
		else if (style == Style.MICROSTEP)
		{
			if (dir == ServoCommand.FORWARD)
				this.currentStep += 1;
			else
				this.currentStep -= 1;
		
			// go to next 'step' and wrap around
			this.currentStep += this.MICROSTEPS * 4;
			this.currentStep %= this.MICROSTEPS * 4;

			pwmA = 0;
			pwmB = 0;
			if (this.currentStep >= 0 && this.currentStep < this.MICROSTEPS)
			{
				pwmA = this.MICROSTEP_CURVE[this.MICROSTEPS - this.currentStep];
				pwmB = this.MICROSTEP_CURVE[this.currentStep];
			}
			else if (this.currentStep >= this.MICROSTEPS && this.currentStep < this.MICROSTEPS*2)
			{
				pwmA = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS];
				pwmB = this.MICROSTEP_CURVE[this.MICROSTEPS*2 - this.currentStep];
			}
			else if (this.currentStep >= this.MICROSTEPS*2 && this.currentStep < this.MICROSTEPS*3)
			{
				pwmA = this.MICROSTEP_CURVE[this.MICROSTEPS*3 - this.currentStep];
				pwmB = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS*2];
			}
			else if (this.currentStep >= this.MICROSTEPS*3 && this.currentStep < this.MICROSTEPS*4)
			{
				pwmA = this.MICROSTEP_CURVE[this.currentStep - this.MICROSTEPS*3];
				pwmB = this.MICROSTEP_CURVE[this.MICROSTEPS*4 - this.currentStep];
			}
		}
		else {
			throw new StepException("Unknown step style");
		}
		
		// go to next 'step' and wrap around
		this.currentStep += this.MICROSTEPS * 4;
		this.currentStep %= this.MICROSTEPS * 4;

		// only really used for microstepping, otherwise always on!	
		setPWM( this.PWMA, (short)0, (short)(pwmA*16) );
		setPWM( this.PWMB, (short)0, (short)(pwmB*16) );

		// set up coil energizing!
		int coils[] = new int[] {0, 0, 0, 0};

		if (style == Style.MICROSTEP)
		{
			if (this.currentStep >= 0 && this.currentStep < this.MICROSTEPS)
				coils = new int[] {1, 1, 0, 0};
			else if (this.currentStep >= this.MICROSTEPS && this.currentStep < this.MICROSTEPS*2)
				coils = new int[] {0, 1, 1, 0};
			else if (this.currentStep >= this.MICROSTEPS*2 && this.currentStep < this.MICROSTEPS*3)
				coils = new int[] {0, 0, 1, 1};
			else if (this.currentStep >= this.MICROSTEPS*3 && this.currentStep < this.MICROSTEPS*4)
				coils = new int[] {1, 0, 0, 1};
		}
		else
		{
			int[][] step2coils = new int[][] { {1, 0, 0, 0},
					{1, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 1},
					{0, 0, 0, 1},
					{1, 0, 0, 1} };


			coils = step2coils[this.currentStep / (this.MICROSTEPS / 2)];


		} 
		//System.out.println("Final Current Step: " + currentStep);
		//System.out.println( "coils state = " + Arrays.toString(coils)  );

		writeToCoils(coils);

		return this.currentStep;
	}
	
	void step(int steps, ServoCommand direction, Style stepStyle) throws StepException, InterruptedException, IOException
	{
		//validation done by MotorControl.stepMotor
		
		//local to the step command. based on the speed set by the motor.
		double mySecondsPerStep = this.secondsPerStep;
		long myStepDelay = (long) (mySecondsPerStep * 1000L);
		
		int latestStep = 0;

		if (stepStyle == Style.INTERLEAVE) {
			System.out.println("Interleave stepping");
			
			mySecondsPerStep = mySecondsPerStep / 2.0;
			myStepDelay = (long) (mySecondsPerStep * 1000L);
		}
		else if (stepStyle == Style.MICROSTEP)
		{
			System.out.println("Micro stepping");
			mySecondsPerStep /= this.MICROSTEPS;
			myStepDelay = (long) (mySecondsPerStep * 1000L);
			
			steps *= this.MICROSTEPS;
		}
		//System.out.println(sPerS + " sec per step");

		//execute the steps
		for (int s=0; s<steps; s++)
		{

			latestStep = this.oneStep(direction, stepStyle);
			Thread.sleep(myStepDelay);
		}
		
		//TODO: what happens when we microstep for a step count of 0?
		if (stepStyle == Style.MICROSTEP)
		{
			// this is an edge case, if we are in between full steps, lets just keep going
			// so we end on a full step
			while (latestStep != 0 && latestStep != this.MICROSTEPS)
			{
				latestStep = this.oneStep(direction, stepStyle);
				Thread.sleep(myStepDelay);
			}
		}
	}
	
	private void setPWM(int channel, short on, short off) throws IOException {
		
		if(this.motorHat != null ) {
			//non-debug. write coil values to motorHat pins
			
			this.motorHat.getPWM().setPWM(channel, on, off);
		}
		else
		{
			//debug. write coil values to file
		}
		
	}

	
	private synchronized void writeToCoils(int[] values) throws IOException {
		//4 coils expected. enforce here.
		
		if(values.length == 4) {
			if(this.motorHat != null ) {
				//non-debug. write coil values to motorHat pins
				
				this.motorHat.setPin(this.AIN2, values[0]);
				this.motorHat.setPin(this.BIN1, values[1]);
				this.motorHat.setPin(this.AIN1, values[2]);
				this.motorHat.setPin(this.BIN2, values[3]);
			}
			else
			{
				//debug. write coil values to file
			}
		}
		else
		{
			throw new RuntimeException("Malformed coils parameter");
		}
	}
}