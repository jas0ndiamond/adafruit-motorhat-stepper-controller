package org.jason.steppercontroller.client;

import java.util.HashMap;

import org.jason.steppercontroller.AdafruitMotorHat;
import org.jason.steppercontroller.AdafruitMotorHat.AdafruitStepperMotor;
import org.jason.steppercontroller.AdafruitMotorHat.ServoCommand;
import org.jason.steppercontroller.AdafruitMotorHat.Style;

public class MotorCommand {

	public static void main(String[] args) throws Exception
	{
		HashMap<Integer, Style> stepStyles = new HashMap<>();
		stepStyles.put(1, AdafruitMotorHat.Style.SINGLE);
		stepStyles.put(2, AdafruitMotorHat.Style.DOUBLE);
		stepStyles.put(3, AdafruitMotorHat.Style.INTERLEAVE);
		stepStyles.put(4, AdafruitMotorHat.Style.MICROSTEP);
		
		HashMap<Integer, Integer> motors = new HashMap<>();
		motors.put(1, AdafruitMotorHat.AdafruitStepperMotor.PORT_M1_M2);		
		motors.put(2, AdafruitMotorHat.AdafruitStepperMotor.PORT_M3_M4);
		
		HashMap<String, ServoCommand> directions = new HashMap<>();
		directions.put("f", ServoCommand.FORWARD);
		directions.put("b", ServoCommand.BACKWARD);

		//StepperCommand s 1 200 f 30 3 3
		//[motor steps-per-rev direction steps style speed]
		
		int motor;
		AdafruitMotorHat mh;
		if(args.length == 7 && args[0].equals("s"))
		{
			motor = Integer.parseInt(args[1]);
			mh = new AdafruitMotorHat(0x60, 1600); // Default addr 0x60
			
			if(motors.containsKey(motor))
			{
				int stepsPerRev = Integer.parseInt(args[2]);
				String direction = args[3];
				int stepCount = Integer.parseInt(args[4]);
				int stepStyle = Integer.parseInt(args[5]);
				int speed = Integer.parseInt(args[6]);
				

				if(directions.containsKey(direction))
				{
					if(stepStyles.containsKey(stepStyle))
					{
						AdafruitStepperMotor stepperMotor = mh.getStepper(motors.get(motor));
						stepperMotor.setSpeed(speed);
						stepperMotor.step
						(
								stepCount, 
								directions.get(direction), 
								stepStyles.get(stepStyle)
						);
					}
					else
					{
						System.out.println("Could not move motor unknown step style");
					}
				}
				else
				{
					System.out.println("Could not move motor unknown direction");
				}
			}
			else
			{
				System.out.println("Could not move unknown motor");
			}

		}
		else if(args.length == 2 && args[0].equals("k"))
		{
			motor = Integer.parseInt(args[1]);
			mh = new AdafruitMotorHat(0x60, 1600); // Default addr 0x60

			if(motor == 1 || motor == 2)
			{
				mh.getStepper(motor).shutdown();
			}
			else
			{
				System.out.println("Could not shutdown unknown motor");
			}
		}
		else
		{
			System.out.println("Unknown command");
		}
	}
}
