package org.jason.steppercontroller.client;

import org.jason.steppercontroller.MotorControl;

public class MotorCommand {

	private final static int HAT_ADDR = 0x60;
	private final static int HAT_FREQ = 1600;
	private final static String MOTOR_X_NAME = "motorX";
	private final static String MOTOR_Y_NAME = "motorY";

	public static void main(String[] args) throws Exception {
		// StepperCommand s 1 200 f 30 3 3
		// [motor steps-per-rev direction steps style speed]

		int motor;
		if (args.length == 7 && args[0].equalsIgnoreCase("s")) {

			MotorControl mc = new MotorControl(HAT_ADDR, HAT_FREQ, false);

			motor = Integer.parseInt(args[1]);

			int stepsPerRev = Integer.parseInt(args[2]);
			int stepCount = Integer.parseInt(args[4]);
			int stepStyle = Integer.parseInt(args[5]);
			int speed = Integer.parseInt(args[6]);

			if (speed <= 0) {
				speed = 1;
			}

			int direction = -99;
			if (args[3].equalsIgnoreCase("f")) {
				direction = MotorControl.DIRECTION_FORWARD;
			} else if (args[3].equalsIgnoreCase("b")) {
				direction = MotorControl.DIRECTION_BACKWARD;
			} else {
				System.out.println("Invalid direction");
				System.exit(-1);
			}
			
			// add our 2 motors
			mc.addMotor(MOTOR_X_NAME, MotorControl.MOTOR_M1_M2, speed);
			mc.addMotor(MOTOR_Y_NAME, MotorControl.MOTOR_M3_M4, speed);

			if(motor == MotorControl.MOTOR_M1_M2) {
				mc.stepMotor(MOTOR_X_NAME, stepCount, direction, stepStyle);
			}
			else if (motor == MotorControl.MOTOR_M3_M4)
			{
				mc.stepMotor(MOTOR_Y_NAME, stepCount, direction, stepStyle);
			}
			else
			{
				System.out.println("Invalid motor");
			}
			
			mc.shutdown();
			
			
		} else if (args.length == 2 && args[0].equalsIgnoreCase("k")) {

			motor = Integer.parseInt(args[1]);

			MotorControl mc = null;
			if (motor == MotorControl.MOTOR_M1_M2) {
				mc = new MotorControl(HAT_ADDR, HAT_FREQ);

				mc.releaseMotor(MOTOR_X_NAME);

			} else if (motor == MotorControl.MOTOR_M3_M4) {
				mc = new MotorControl(HAT_ADDR, HAT_FREQ);

				mc.releaseMotor(MOTOR_Y_NAME);
			}
			else
			{
				System.out.println("Invalid motor");
			}

		} else {
			System.out.println("Unknown command");
		}
	}
}
