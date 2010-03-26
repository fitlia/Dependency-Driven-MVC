package com.google.gwt.ddmvc.model;


/**
 * Field is used by ObjectModel to define certain fields which enforce a level
 * of type-safety on models.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the type of value held by this field
 * @param <ModelType> the type of model used by this field
 * @param <ReferenceType> the type actually referred to be this field (should be
 * 				one of the above)
 */
public abstract class Field<ValueType, ModelType extends Model, ReferenceType> {

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
	protected Class<ValueType> valueType;
	protected Class<ModelType> modelType;
	protected Class<ReferenceType> referenceType;
	protected FieldType fieldType;
	
	/**
	 * Instantiate a new Field
	 * @param valueType - the type of value held by this field
	 * @param modelType - the type of model used by this field
	 * @param referenceType - the type actually referred to be this field (should be
	 * 				one of the above)
	 * @param fieldType - the type of field this field is
	 * @param key - they key to assign to the model
	 */
	public Field(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, FieldType fieldType, String key) {
		Path.validateKey(key);
		
		if(valueType.isInterface()
				|| modelType.isInterface()
				|| referenceType.isInterface())
			throw new IllegalArgumentException("Types cannot be an interface.");
		
		if(!referenceType.equals(valueType)
				&& !referenceType.equals(modelType))
			throw new IllegalArgumentException("ReferenceType must be either" +
					" ValueType or ModelType.");
		
		this.key = key;
		this.fieldType = fieldType;
		this.valueType = valueType;
		this.modelType = modelType;
		this.referenceType = referenceType;
	}
	

	/**
	 * @return the key of this model
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the type of value held by this field
	 */
	public Class<ValueType> getValueType() {
		return valueType;
	}

	/**
	 * @return the type of model used by this field
	 */
	public Class<ModelType> getModelType() {
		return modelType;
	}

	/**
	 * @return the type actually referred to be this field
	 */
	public Class<ReferenceType> getReferenceType() {
		return referenceType;
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
	
	/**
	 * @return true if the model is an acceptable model for this field
	 */
	public abstract boolean isValidModel(Model model);
}
