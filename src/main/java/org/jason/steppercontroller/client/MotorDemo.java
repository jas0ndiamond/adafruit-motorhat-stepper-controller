package org.jason.steppercontroller.client;

import org.jason.steppercontroller.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorDemo {

	private final static Logger LOGGER = LoggerFactory.getLogger(MotorDemo.class);

	private final static int HAT_ADDR = 0x60;
	private final static int HAT_FREQ = 1600;

	private final static String MOTOR_X_NAME = "motorX";
	private final static String MOTOR_Y_NAME = "motorY";

	public static void main(String[] args) throws Exception {

		int stepsPerRev = 200;
		int speed = 50;

		int stepsPerSegment = 50;

		boolean useXMotor = true;
		boolean useYMotor = false;

		// for motors 1 and 2
		// 100 steps forward and backward single
		// kill
		// 100 steps forward and backward double
		// kill
		// 100 steps forward and backward interleave
		// kill
		// 100 steps forward and backward microstep
		// kill

		// StepperCommand s 1 200 f 30 3 3
		// [motor steps-per-rev direction steps style speed]

		MotorControl mc = null;

		try {
			
			//init motorcontrol and reset the pwm
			mc = new MotorControl(HAT_ADDR, HAT_FREQ, true);

			// add our 2 motors
			mc.addMotor(MOTOR_X_NAME, MotorControl.MOTOR_M1_M2, speed);
			mc.addMotor(MOTOR_Y_NAME, MotorControl.MOTOR_M3_M4, speed);

			if (useXMotor) {

				/////////////
				// Single

				LOGGER.info("============\nSingle Step - Motor X");
				
				// forward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_SINGLE);
				// backward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_SINGLE);

				// kill
				mc.releaseMotor(MOTOR_X_NAME);
				
				/////////////
				//double

				LOGGER.info("============\nDouble Step - Motor X");
				
				// forward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_DOUBLE);
				// backward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_DOUBLE);

				// kill
				mc.releaseMotor(MOTOR_X_NAME);
				
				/////////////
				//interleave
				
				LOGGER.info("============\nInterleave Step - Motor X");
				
				// forward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_INTERLEAVE);
				// backward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_INTERLEAVE);

				// kill
				mc.releaseMotor(MOTOR_X_NAME);
				
				/////////////
				//micro
				
				LOGGER.info("============\nMicro Step - Motor X");
				
				// forward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_MICROSTEP);
				// backward
				mc.stepMotor(MOTOR_X_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_MICROSTEP);

				// kill
				mc.releaseMotor(MOTOR_X_NAME);
			}

			if (useYMotor) {
				/////////////
				// Single

				LOGGER.info("============\nSingle Step - Motor Y");
				
				// forward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_SINGLE);
				// backward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_SINGLE);

				// kill
				mc.releaseMotor(MOTOR_Y_NAME);
				
				/////////////
				//double
				
				LOGGER.info("============\nDouble Step - Motor Y");
				
				// forward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_DOUBLE);
				// backward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_DOUBLE);

				// kill
				mc.releaseMotor(MOTOR_Y_NAME);
				
				/////////////
				//interleave
				
				LOGGER.info("============\nInterleave Step - Motor Y");
				
				// forward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_INTERLEAVE);
				// backward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_INTERLEAVE);

				// kill
				mc.releaseMotor(MOTOR_Y_NAME);
				
				/////////////
				//micro
				
				LOGGER.info("============\nMicro Step - Motor Y");
				
				// forward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_FORWARD, MotorControl.STEP_MICROSTEP);
				// backward
				mc.stepMotor(MOTOR_Y_NAME, stepsPerSegment, MotorControl.DIRECTION_BACKWARD, MotorControl.STEP_MICROSTEP);

				// kill
				mc.releaseMotor(MOTOR_Y_NAME);
			}

		} finally {
			if (mc != null) {
				mc.shutdown();
			}
		}

	}
}
