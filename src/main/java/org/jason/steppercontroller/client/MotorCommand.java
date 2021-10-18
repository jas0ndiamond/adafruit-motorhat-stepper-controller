package org.jason.steppercontroller.client;

import org.jason.steppercontroller.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorCommand {

    private final static Logger LOGGER = LoggerFactory.getLogger(MotorCommand.class);
    
    private final static int HAT_ADDR = 0x60;
    private final static int HAT_FREQ = 1600;
    
    public final static String DIRECTION_FORWARD = "f";
    public final static String DIRECTION_BACKWARD = "b";
    
    private final static String COMMAND_STEP = "s";
    private final static String COMMAND_KILL = "k";
    
    private final static String MOTOR_X_NAME = "motorX";
    private final static String MOTOR_Y_NAME = "motorY";

    public static void main(String[] args) throws Exception {
        // StepperCommand s 1 200 f 30 3 3
        // [motor steps-per-rev direction steps style speed]

        int motor;
        if (args.length == 7 && args[0].equalsIgnoreCase(COMMAND_STEP)) 
        {
            MotorControl mc = null;
            
            try {
               mc = new MotorControl(HAT_ADDR, HAT_FREQ, false);
   
               motor = Integer.parseInt(args[1]);
   
               //Reference implementation may not supply this to the motor. how embarrassing.
               //TODO: set on per-motor basis
               int stepsPerRev = Integer.parseInt(args[2]);
               
               int stepCount = Integer.parseInt(args[4]);
               int stepStyle = Integer.parseInt(args[5]);
               int speed = Integer.parseInt(args[6]);
   
               if (speed <= 0) {
                   speed = 1;
               }
   
               int direction = -99;
               if (args[3].equalsIgnoreCase(DIRECTION_FORWARD)) {
                   direction = MotorControl.DIRECTION_FORWARD;
               } else if (args[3].equalsIgnoreCase(DIRECTION_BACKWARD)) {
                   direction = MotorControl.DIRECTION_BACKWARD;
               } else {
                   LOGGER.error("Invalid direction");
                   System.exit(-1);
               }
               
               // add our 2 motors
               mc.addMotor(MOTOR_X_NAME, MotorControl.MOTOR_M1_M2, speed);
               mc.addMotor(MOTOR_Y_NAME, MotorControl.MOTOR_M3_M4, speed);
   
               if(motor == MotorControl.MOTOR_M1_M2) 
               {
                   mc.stepMotor(MOTOR_X_NAME, stepCount, direction, stepStyle);
               }
               else if (motor == MotorControl.MOTOR_M3_M4)
               {
                   mc.stepMotor(MOTOR_Y_NAME, stepCount, direction, stepStyle);
               }
               else
               {
                   LOGGER.error("Invalid motor");
               }
            
            }
            finally {
               if(mc != null) {
                  mc.shutdown();
               }
            }
        } 
        else if (args.length == 2 && args[0].equalsIgnoreCase(COMMAND_KILL)) 
        {
            motor = Integer.parseInt(args[1]);

            MotorControl mc = null; 
            
            try {
               mc = new MotorControl(HAT_ADDR, HAT_FREQ);
            
               // add our 2 motors. have to do add these or else there's nothing to shut down
               mc.addMotor(MOTOR_X_NAME, MotorControl.MOTOR_M1_M2, MotorControl.DEFAULT_MOTOR_SPEED);
               mc.addMotor(MOTOR_Y_NAME, MotorControl.MOTOR_M3_M4, MotorControl.DEFAULT_MOTOR_SPEED);
               
               if (motor == MotorControl.MOTOR_M1_M2) 
               {
                   mc.releaseMotor(MOTOR_X_NAME);
               } 
               else if (motor == MotorControl.MOTOR_M3_M4) 
               {
                   mc.releaseMotor(MOTOR_Y_NAME);
               }
               else
               {
                   LOGGER.error("Invalid motor");
               }
            }
            finally {
               if(mc != null) {
                  mc.shutdown();
               }
            }
        } 
        else {
            LOGGER.error("Unknown command");
        }
    }
}
