package com.google.gwt.ddmvc.model;


/**
 * Field is used by ObjectModel to define certain fields which enforce a level
 * of type-safety on models.
 * @author Kevin Dolan
 */
public abstract class Field<Type> {

	protected String key;
	protected Class<Type> cls;
	
	/**
	 * Instantiate a new Field
	 * @param key - they key to assign to the model
	 */
	protected Field(Class<Type> cls, String key) {
		this.key = key;
		this.cls = cls;
	}
	
	/**
	 * @return the key of this model
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the class referred to by this field
	 */
	public Class<?> getFieldClass() {
		return cls;
	}
	
	/**
	 * @return the model that is used to represent this Field
	 */
	public abstract Model getModel();
	
	/**
	 * @return the pathString to append for this field
	 */
	public abstract String getPathString();
}
