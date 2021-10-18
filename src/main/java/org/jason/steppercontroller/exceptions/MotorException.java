/**
 * 
 */
package org.jason.steppercontroller.exceptions;

/**
 * @author jason
 *
 */
public class MotorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2464149037356886826L;

	/**
	 * 
	 */
	public MotorException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public MotorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public MotorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MotorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MotorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
