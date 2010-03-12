package com.google.gwt.ddmvc.model.update;

/**
 * This class is used to provide feedback about exceptions encountered during an
 * update.  It holds the exception returned and the ModelUpdate that caused it.
 * It takes the place of the model value in the event of an exception.
 * @author Kevin Dolan
 */
public class ExceptionEncountered {

	private ModelUpdate cause;
	private Exception exception;
	
	/**
	 * @param cause - the cause of the exception
	 * @param exception - the exception itself
	 */
	public ExceptionEncountered(ModelUpdate cause, Exception exception) {
		super();
		this.cause = cause;
		this.exception = exception;
	}

	/**
	 * @return the ModelUpdate that caused the exception to be thrown
	 */
	public ModelUpdate getCause() {
		return cause;
	}
	
	/**
	 * @return the exception that was thrown
	 */
	public Exception getException() {
		return exception;
	}
	
}
