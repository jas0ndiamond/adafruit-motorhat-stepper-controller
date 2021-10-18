package org.jason.steppercontroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jason.steppercontroller.exceptions.HatException;
import org.jason.steppercontroller.exceptions.MotorException;
import org.jason.steppercontroller.exceptions.StepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorControl {

	private final static Logger LOGGER = LoggerFactory.getLogger(MotorControl.class);
	
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
	
	public  final static int DEFAULT_MOTOR_SPEED = 30;
	
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
		
		setMotorSpeed(name, speed);
	}
	
	public void setMotorSpeed(String name, int speed)
	{
		//speed in rpms
		if(motors.containsKey(name)) {
			motors.get(name).setSpeed(speed);
		}
		else
		{
			LOGGER.error("Could not set speed on unknown motor");
		}
	}
	
	public synchronized void stepMotor(String name, int steps, int direction, int stepStyle)
			throws StepException, IOException, InterruptedException {
		if (motors.containsKey(name)) {
			if (directions.containsKey(direction) && stepStyles.containsKey(stepStyle)) {
				
				motors.get(name).step(steps, directions.get(direction), stepStyles.get(stepStyle));
			} else {
				LOGGER.error("Invalid step parameters.");
			}
		} else {
			LOGGER.error("Invalid motor.");
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
		} else {
			LOGGER.warn("Motor {} not present for release", motorName);
		}
	}
	
	public void releaseMotors()
	{
		for( Entry<String, AdafruitStepperMotor> motor : motors.entrySet()) {
			try 
			{
				LOGGER.debug("Shutting down motor: {}", motor.getKey());
				releaseMotor(motor.getKey());
			} 
			catch (IOException e) 
			{
				LOGGER.warn("Exception releasing motor", e);
			}
		}
	}
	
	public void shutdown()
	{
		//do not release motors
		
		//shutdown the underlying hat
		if(hat != null) {
			hat.shutdown();
		} else {
			LOGGER.warn("Skipping shutdown of null motor hat");
		}
	}
}
