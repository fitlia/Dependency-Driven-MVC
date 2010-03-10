package com.google.gwt.ddmvc.model;

/**
 * The ModelUpdate class represents an individual update to some model
 * 
 * @author Kevin Dolan
 */
public class ModelUpdate {

	/**
	 * Used to specify what type of update is being performed
	 * SET : Set the value to data
	 * LIST_ADD : Add the data to the list
	 * LIST_ADD_ALL : Add all values from the data collection to the list
	 * LIST_REMOVE : Remove the specified value from the list
	 * LIST_REMOVE_INDEX : Remove the value at the specified index from the list
	 */
	public enum UpdateType {
		SET,
		LIST_ADD,
		LIST_ADD_ALL,
		LIST_REMOVE,
		LIST_REMOVE_INDEX
	}
	
	private String modelKey;
	private Object data;
	private UpdateType update;
	
	public String getModelKey() {
		return modelKey;
	}

	public Object getData() {
		return data;
	}

	public UpdateType getUpdate() {
		return update;
	}

	/**
	 * Instantiate a new model update with the specified key and data, implies type is SET
	 * @param modelKey the key of the model to set
	 * @param data	   the data to set the model to
	 */
	public ModelUpdate(String modelKey, Object data) {
		this.modelKey = modelKey;
		this.update = UpdateType.SET;
		this.data = data;
	}
	
	/**
	 * Instantiate a new model update with a specified update type
	 * @param modelKey the key of the model to set
	 * @param update the update type to enact
	 * @param data	 the data to update
	 */
	public ModelUpdate(String modelKey, UpdateType update, Object data) {
		this.modelKey = modelKey;
		this.update = update;
		this.data = data;
	}
	
}
