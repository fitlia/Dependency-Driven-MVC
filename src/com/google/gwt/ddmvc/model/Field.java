package com.google.gwt.ddmvc.model;


/**
 * Field is used by ObjectModel to define certain fields which enforce a level
 * of type-safety on models.
 * @author Kevin Dolan
 */
public abstract class Field {

	private String key;
	
	/**
	 * Instantiate a new Field
	 * @param key - they key to assign to the model
	 */
	public Field(String key) {
		this.key = key;
	}
	
	/**
	 * @return the key of this model
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the model that is used to represent this Field
	 */
	public abstract Model getModel();
}
