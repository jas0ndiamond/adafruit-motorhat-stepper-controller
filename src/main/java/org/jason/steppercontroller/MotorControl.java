package org.jason.steppercontroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jason.steppercontroller.exceptions.HatException;
import org.jason.steppercontroller.exceptions.MotorException;
import org.jason.steppercontroller.exceptions.StepException;

public class MotorControl {

	public enum Style
	{  
		SINGLE, DOUBLE, INTERLEAVE, MICROSTEP
	}

	public enum ServoCommand
	{
		FORWARD, BACKWARD, BRAKE, RELEASE
	}
	
	private final static int DEFAULT_HAT_ADDR = 0x60;
	private final static int DEFAULT_FREQ = 1600;
	private final static int DEFAULT_MOTOR_SPEED = 30;
	
	public final static int DIRECTION_FORWARD = 0;
	public final static int DIRECTION_BACKWARD = 1;
	
	public final static int STEP_SINGLE = 0;
	public final static int STEP_DOUBLE = 1;
	public final static int STEP_INTERLEAVE = 2;
	public final static int STEP_MICROSTEP = 3;
	
	public final static int MOTOR_M1_M2 = 0;
	public final static int MOTOR_M3_M4 = 1;
		
	private final static HashMap<Integer, Integer> ports = new HashMap<Integer, Integer>() 
	{
		private static final long serialVersionUID = 4739823974683355020L;

		{
			put(MOTOR_M1_M2, AdafruitStepperMotor.PORT_M1_M2);
			put(MOTOR_M3_M4, AdafruitStepperMotor.PORT_M3_M4);
		}
	};
	
	private final static HashMap<Integer, Style> stepStyles = new HashMap<Integer, Style>()
	{
		private static final long serialVersionUID = 7754490000321920041L;
		{
			put(STEP_SINGLE, Style.SINGLE);
			put(STEP_DOUBLE, Style.DOUBLE);
			put(STEP_INTERLEAVE, Style.INTERLEAVE);
			put(STEP_MICROSTEP, Style.MICROSTEP);
		}
	};
	
	private final static HashMap<Integer, ServoCommand> directions = new HashMap<Integer, ServoCommand>() 
	{
		private static final long serialVersionUID = 7754490000321920041L;
		{
			put(DIRECTION_FORWARD, ServoCommand.FORWARD);
			put(DIRECTION_BACKWARD, ServoCommand.BACKWARD);
		}
	};
	
	private HashMap<String, AdafruitStepperMotor> motors;
	private AdafruitMotorHat hat;

	public MotorControl() throws IOException, HatException  
	{
		this(DEFAULT_HAT_ADDR, DEFAULT_FREQ, true);
	}
	
	public MotorControl(int hatAddress, int hatFreq) throws IOException, HatException
	{
		this(DEFAULT_HAT_ADDR, DEFAULT_FREQ, true);
	}
	
	public MotorControl(int hatAddress, int hatFreq, boolean resetPWM) throws IOException, HatException 
	{
		hat = new AdafruitMotorHat(hatAddress, hatFreq, resetPWM);
		motors = new HashMap<String, AdafruitStepperMotor>();
	}
	
	public void addMotor(String name, int port) throws MotorException {
		addMotor(name, port, DEFAULT_MOTOR_SPEED);
	}
	
	public void addMotor(String name, int port, int speed) throws MotorException
	{
		//create the motor object
		hat.addStepper(port);
		
		motors.put(name, hat.getStepper( ports.get(port) ) );
		
		motors.get(name).setSpeed(speed);
	}
	
	public synchronized void stepMotor(String name, int steps, int direction, int stepStyle)
			throws StepException, IOException, InterruptedException {
		if (motors.containsKey(name)) {
			if (directions.containsKey(direction) && stepStyles.containsKey(stepStyle)) {
				
				motors.get(name).step(steps, directions.get(direction), stepStyles.get(stepStyle));
			} else {
				System.out.println("Invalid step paramters.");
			}
		} else {
			System.out.println("Invalid motor.");
		}
	}
	
	public void stepMotorSingle(String name, int steps, int direction) throws IOException, StepException, InterruptedException
	{
		stepMotor(name, steps, direction, STEP_SINGLE);
	}
	
	public void stepMotorDouble(String name, int steps, int direction) throws IOException, StepException, InterruptedException
	{
		stepMotor(name, steps, direction, STEP_DOUBLE);
	}
	
	public void stepMotorInterleave(String name, int steps, int direction) throws IOException, StepException, InterruptedException
	{
		stepMotor(name, steps, direction, STEP_INTERLEAVE);		
	}
	
	public void stepMotorMicrostep(String name, int steps, int direction) throws IOException, StepException, InterruptedException
	{
		stepMotor(name, steps, direction, STEP_MICROSTEP);
	}
	
	public void releaseMotor(String motorName) throws IOException {
		if(motors.containsKey(motorName) ) {
			motors.get(motorName).shutdown();
		}
	}
	
	public void releaseMotors()
	{
		for( Entry<String, AdafruitStepperMotor> motor : motors.entrySet()) {
			
			try 
			{
				System.out.println("Shutting down motor: " + motor.getKey());
				releaseMotor(motor.getKey());
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void shutdown()
	{
		//do not release motors
		
		//shutdown the underlying hat
		hat.shutdown();
	}
}
