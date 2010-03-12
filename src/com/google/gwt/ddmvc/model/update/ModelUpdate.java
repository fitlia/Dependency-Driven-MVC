package com.google.gwt.ddmvc.model.update;

/**
 * The ModelUpdate class represents an individual update to some model. 
 * Several types of updates are defined by default, but the application
 * developer can create any number of his own, which perform any type of
 * reasonable business logic.
 * 
 * @author Kevin Dolan
 */
public abstract class ModelUpdate {
	
	protected String target;
	protected boolean isComplete;
	protected Exception exception;
	
	/**
	 * Instantiate a new model update with the specified key
	 * @param target - the name of the model being targeted
	 */
	public ModelUpdate(String target) {
		this.target = target;
		this.isComplete = false;
		this.exception = null;
	}
	
	/**
	 * Determine whether or not this update has run.
	 * Will return true even if an exception was encountered.
	 * @return true if the update has already run
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * Determine whether or not the update encountered an exception.
	 * If an exception was thrown inside .performUpdate(...) it will not 
	 * propagate upwards, but rather end up here.
	 * @return the exception encountered, or null if there was none
	 */
	public Exception getException() {
		return exception;
	}
	
	/**
	 * @return the name of the model this update's target model
	 */
	public String getTarget() {
		return target;
	}
	
	/**
	 * Returns true if this model is the same type as other.  This is useful for
	 * render-optimizations where what type of update this is matters.
	 * @param other - the other ModelUpdate object to check
	 * @returns true if this model is the same type of update
	 */
	public boolean isSame(ModelUpdate other) {
		return getClass().equals(other.getClass());
	}
	
	/**
	 * The publicly available call to perform this update.
	 * Ensures that updates are only run once and that exceptions are handled
	 * properly.  If an exception is encountered, it will not propagate upwards
	 * but rather be returned by a call to .getException().  
	 * Also, in this case, the value of the model will be set to an 
	 * ExceptionEncountered object, which contains this update and the exception
	 * encountered.
	 * @param value - the original value of the model
	 * @return the new value of the model
	 * @throws ModelUpdateAttemptedTwiceException if this has already been called
	 */
	public Object process(Object value) {
		if(isComplete())
			throw new ModelUpdateAttemptedTwiceException();
		
		try {
			return performUpdate(value);
		}
		catch(Exception e) {
			this.exception = e;
			return new ExceptionEncountered(this, e);
		}
	}
	
	/**
	 * Perform whatever update this update requests.  
	 * If the request would cause
	 * the model's data to be replaced, return the object you would like to
	 * replace it with.  Otherwise, you should just return the object itself.
	 * @param value - the model's current data reference
	 * @return the model's end data reference
	 */
	protected abstract Object performUpdate(Object value);
	
}
