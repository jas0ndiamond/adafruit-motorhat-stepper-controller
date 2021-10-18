package org.jason.steppercontroller.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jason.steppercontroller.MotorControl;

public class MotorShell {
    private final static String MOTOR_X_NAME = "X";
    private final static String MOTOR_Y_NAME = "Y";

    public final static String DIRECTION_FORWARD = "f";
    public final static String DIRECTION_BACKWARD = "b";

    public static void main(String[] args) throws Exception {
//        HashMap<Integer, Style> stepStyles = new HashMap<>();
//        stepStyles.put(1, Style.SINGLE);
//        stepStyles.put(2, Style.DOUBLE);
//        stepStyles.put(3, Style.INTERLEAVE);
//        stepStyles.put(4, Style.MICROSTEP);

        // Default addr 0x60
        MotorControl mc = new MotorControl(0x60, 1600);
        mc.addMotor(MOTOR_X_NAME, 0, MotorControl.DEFAULT_MOTOR_SPEED);
        mc.addMotor(MOTOR_Y_NAME, 1, MotorControl.DEFAULT_MOTOR_SPEED);

//        motor_y = mh.getStepper(AdafruitStepperMotor.PORT_M1_M2);
//        motor_y.setSpeed(3); // 30 RPM
//
//        motor_x = mh.getStepper(AdafruitStepperMotor.PORT_M3_M4);
//        motor_x.setSpeed(3); // 30 RPM

        boolean runShell = true;

        BufferedReader stdIn = null;

        try {
            stdIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.print(">");
            String command = null;

            // Style stepStyle;
            int direction;

            while (runShell && (command = stdIn.readLine()) != null) {
                command.replaceAll("\n", "");

                if (command.equals("exit") || command.equals("quit") || command.equals("q")
                        || command.equals("shutdown")) {
                    System.out.println("Take care...");
                    runShell = false;
                } else if (command.matches("^[xy]\\s+[fb]\\s+\\d+\\s+\\d\\s*")) {
                    String[] fields = command.split("\\s");

                    int steps = Integer.parseInt(fields[2]);
                    int stepStyle = Integer.parseInt(fields[3]);

                    // direction = null;
                    // stepStyle = null;

                    if (fields[1].equals(DIRECTION_FORWARD)) {
                        direction = MotorControl.DIRECTION_FORWARD;
                    } else if (fields[1].equals(DIRECTION_BACKWARD)) {
                        direction = MotorControl.DIRECTION_BACKWARD;
                    } else {
                        System.out.println("Unknown motor direction.");
                        continue;
                    }

                    // stepStyle = stepStyles.get(stepTypeInput);

                    System.out.println("Stepping with style: " + stepStyle + " and direction " + direction);

                    if (fields[0].equalsIgnoreCase(MOTOR_X_NAME)) {
                        // mc.step(steps, direction, stepStyle);
                        mc.stepMotor(MOTOR_X_NAME, steps, direction, stepStyle);
                    } else if (fields[0].equalsIgnoreCase(MOTOR_Y_NAME)) {
                        // motor_y.step(steps, direction, stepStyle);
                        mc.stepMotor(MOTOR_Y_NAME, steps, direction, stepStyle);
                    }
                } else {
                    System.out.println("Malformed move command");
                }

                System.out.print("\n>");
            }

            // mc.shutdownMotor(MOTOR_X_NAME);
            // mc.shutdownMotor(MOTOR_Y_NAME);

            mc.releaseMotors();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (stdIn != null) {
                stdIn.close();
            }
        }
    }
}
