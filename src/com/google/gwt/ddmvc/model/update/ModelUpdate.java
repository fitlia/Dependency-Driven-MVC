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
	protected Object data;

	/**
	 * @return the data associated with this update
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * @return the name of the model this update's target model
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Instantiate a new model update with the specified key and data
	 * @param target the name of the model being targeted
	 * @param data	 the data associated with this update
	 */
	public ModelUpdate(String target, Object data) {
		this.target = target;
		this.data = data;
	}
	
	/**
	 * Returns true if this model is the same type as other.  This is useful for
	 * render-optimizations where what type of update this is matters.
	 * @param other the other ModelUpdate object to check
	 * @returns		true if this model is the same type of update
	 */
	public boolean isSame(ModelUpdate other) {
		return getClass().equals(other.getClass());
	}
	
	/**
	 * Perform whatever update this update requests.  
	 * If the request would cause
	 * the model's data to be replaced, return the object you would like to replace
	 * it with.  Otherwise, you should just return the object itself.
	 * @param value	the model's current data reference
	 * @return		the model's end data reference
	 */
	public abstract Object performUpdate(Object value);
	
}
