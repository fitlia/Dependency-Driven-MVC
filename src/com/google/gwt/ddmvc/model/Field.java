package com.google.gwt.ddmvc.model;


/**
 * Field is used by ObjectModel to define certain fields which enforce a level
 * of type-safety on models.
 * @author Kevin Dolan
 */
public abstract class Field<Type> {

	/**
	 * The referential depth of this field, determines what to be appended for
	 * the path.  
	 * 	MODEL -> nothing appended
	 *	VALUE -> .$ appended
	 *	FIELD -> .* appended
	 */
	public enum FieldType {
		MODEL,
		VALUE,
		FIELD
	}
	
	protected String key;
	protected Class<Type> cls;
	protected FieldType fieldType;
	
	/**
	 * Instantiate a new Field
	 * @param cls - the expected return type of this field
	 * @param fieldType - the type of field this field is
	 * @param key - they key to assign to the model
	 */
	protected Field(Class<Type> cls, FieldType fieldType, String key) {
		this.key = key;
		this.fieldType = fieldType;
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
	 * @return the field type of this field
	 */
	public FieldType getFieldType() {
		return fieldType;
	}
	
	/**
	 * @return the pathString to append for this field
	 */
	public String getPathString() {
		if(fieldType == FieldType.MODEL)
			return key;
		
		if(fieldType == FieldType.VALUE)
			return key + ".$";
		
		//if(fieldType == FieldType.FIELD)
		return key + ".*";
	}
	
	/**
	 * @return the model that is used to represent this Field
	 */
	public abstract Model getModel();
}
