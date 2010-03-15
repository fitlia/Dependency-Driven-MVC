package com.google.gwt.ddmvc.model.update;

import com.google.gwt.ddmvc.model.Path;

/**
 * ModelUpdate to cascade when an exception is encountered by a Computed Model.
 * @author Kevin Dolan
 */
public class ExceptionComputed extends ModelUpdate {

	private Exception exception;
	
	public ExceptionComputed(Path target, Exception exception) {
		super(target);
		this.exception = exception;
		this.isComplete = true;
	}
	
	/**
	 * Get the exception encountered by the ComputedModel
	 * @return the exception encountered
	 */
	public Exception getException() {
		return exception;
	}
	
	@Override
	protected Object performUpdate(Object value) {
		return null;
	}

}
