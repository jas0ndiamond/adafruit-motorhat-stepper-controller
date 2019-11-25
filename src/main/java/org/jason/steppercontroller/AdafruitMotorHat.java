package org.jason.steppercontroller;

import java.io.IOException;
import java.util.Arrays;

public class AdafruitMotorHat
{
	public enum Style
	{  
		SINGLE, DOUBLE, INTERLEAVE, MICROSTEP
	}

	public enum Motor
	{
		M1, M2, M3, M4
	}

	public enum ServoCommand
	{
		FORWARD, BACKWARD, BRAKE, RELEASE
	}

	private final static int HAT_ADDR = 0x60;
	private final static int DEFAULT_FREQ = 1600;
	private int freq = 1600;
	private int i2cAddr = HAT_ADDR;

	//  private AdafruitDCMotor      motors[];
	private AdafruitStepperMotor steppers[];
	private PWM pwm;

	public AdafruitMotorHat() throws Exception
	{
		this(HAT_ADDR, DEFAULT_FREQ);
	}

	public AdafruitMotorHat(int addr, int freq) throws Exception
	{
		this.i2cAddr = addr;
		this.freq    = freq;
		//    motors = new AdafruitDCMotor[4];
		int i = 0;
		//    for (Motor motor : Motor.values())
		//      motors[i++] = new AdafruitDCMotor(this, motor);
		steppers = new AdafruitStepperMotor[2];
		steppers[0] = new AdafruitStepperMotor(this, 1);
		steppers[1] = new AdafruitStepperMotor(this, 2);
		pwm = new PWM(addr);
		try
		{
			pwm.setPWMFreq(freq);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void setPin(int pin, int value) throws IOException
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

	public AdafruitStepperMotor getStepper(int num)
	{
		if (num < 1 || num > 2)
			throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
		return steppers[num-1];
	}



	public static class AdafruitStepperMotor
	{
		public final static int PORT_M1_M2 = 1; // Port #1
		public final static int PORT_M3_M4 = 2; // Port #2

		private AdafruitMotorHat mc;
		private int MICROSTEPS = 8;
		private int[] MICROSTEP_CURVE = new int[] {0, 50, 98, 142, 180, 212, 236, 250, 255};

		private static int DEFAULT_NB_STEPS = 200; // between 35 & 200

		private int PWMA = 8;
		private int AIN2 = 9;
		private int AIN1 = 10;
		private int PWMB = 13;
		private int BIN2 = 12;
		private int BIN1 = 11;

		private int revSteps;
		private int motorNum;
		private double secPerStep = 0.1;
		private long stepDelay = (long) (secPerStep * 1000L);
		private int steppingCounter = 0;
		private int currentStep = 0;

		// MICROSTEPS = 16
		// a sinusoidal curve NOT LINEAR!
		// MICROSTEP_CURVE = [0, 25, 50, 74, 98, 120, 141, 162, 180, 197, 212, 225, 236, 244, 250, 253, 255]

		public AdafruitStepperMotor(AdafruitMotorHat controller, int num)
		{
			this(controller, num, DEFAULT_NB_STEPS);
		}

		public AdafruitStepperMotor(AdafruitMotorHat controller, int num, int steps)
		{
			this.mc = controller;
			this.revSteps = steps;
			this.motorNum = num;
			this.secPerStep = 0.1;
			this.steppingCounter = 0;
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
				throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
			}
		}   

		public void setSpeed(double rpm)
		{
			this.secPerStep = 60.0 / (this.revSteps * rpm);
			this.stepDelay = (long) (secPerStep * 1000L);

			
			this.steppingCounter = 0;
			//TODO: recalc step delay
		}

		public void shutdown() throws IOException
		{
			this.mc.setPin(this.AIN2, 0);
			this.mc.setPin(this.BIN1, 0);
			this.mc.setPin(this.AIN1, 0);
			this.mc.setPin(this.BIN2, 0);
		}


		private int oneStep(ServoCommand dir, Style style) throws Exception
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
				throw new Exception("Unknown step style");
			}

			//System.out.println("PWM_A: " + pwmA);
			//System.out.println("PWM_B: " + pwmB);
			
			// go to next 'step' and wrap around
			this.currentStep += this.MICROSTEPS * 4;
			this.currentStep %= this.MICROSTEPS * 4;

			// only really used for microstepping, otherwise always on!
			this.mc.pwm.setPWM(this.PWMA, (short)0, (short)(pwmA*16));
			this.mc.pwm.setPWM(this.PWMB, (short)0, (short)(pwmB*16));

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

			this.mc.setPin(this.AIN2, coils[0]);
			this.mc.setPin(this.BIN1, coils[1]);
			this.mc.setPin(this.AIN1, coils[2]);
			this.mc.setPin(this.BIN2, coils[3]);

			return this.currentStep;
		}

		public void step(int steps, ServoCommand direction, Style stepStyle) throws Exception
		{
			double sPerS = this.secPerStep;
			long stepDelay = (long) (sPerS * 1000L);
			
			int latestStep = 0;

			if (stepStyle == Style.INTERLEAVE) {
				System.out.println("Interleave stepping");
				
				sPerS = sPerS / 2.0;
				stepDelay = (long) (sPerS * 1000L);
			}
			else if (stepStyle == Style.MICROSTEP)
			{
				System.out.println("Micro stepping");
				sPerS /= this.MICROSTEPS;
				stepDelay = (long) (sPerS * 1000L);
				
				steps *= this.MICROSTEPS;
			}
			//System.out.println(sPerS + " sec per step");

			//execute the steps
			for (int s=0; s<steps; s++)
			{

				latestStep = this.oneStep(direction, stepStyle);
				Thread.sleep(stepDelay);
			}
			
			if (stepStyle == Style.MICROSTEP)
			{
				// this is an edge case, if we are in between full steps, lets just keep going
				// so we end on a full step
				while (latestStep != 0 && latestStep != this.MICROSTEPS)
				{
					latestStep = this.oneStep(direction, stepStyle);
					Thread.sleep(stepDelay);
				}
			}
		}
	}
}
