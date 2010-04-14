package com.google.gwt.ddmvc.model.path;

import com.google.gwt.ddmvc.Utility;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * Fields store information about a single unit of a path.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the type of value held by this field
 * @param <ModelType> the type of model used by this field
 * @param <ReferenceType> the type actually referred to be this field (should be
 * 				one of the above)
 */
public class Field<ValueType, ModelType extends Model, ReferenceType> 
		extends Path<ValueType, ModelType, ReferenceType> {
	
	//
	// Static factory methods for convenience
	//
	
	/**
	 * Create a new standard-parameterized model field
	 * @param key - the key to use
	 * @return the field
	 */
	public static Field<Object, Model, Model> modelField(String key) {
		return new Field<Object, Model, Model>(Object.class, Model.class, 
				Model.class, key, ReferenceDepth.MODEL);
	}
	
	/**
	 * Create a new custom-parameterized model field
	 * @param key - the key to use
	 * @param modelType - the expected model class
	 * @return the field
	 */
	public static <MT extends Model> Field<Object, MT, MT> 
			modelField(Class<MT> modelType, String key) {
		
		return new Field<Object, MT, MT>(Object.class, modelType, 
				modelType, key, ReferenceDepth.MODEL);
	}
	
	/**
	 * Create a new standard-parameterized field field
	 * @param key - the key to use
	 * @return the field
	 */
	public static Field<Object, Model, Model> fieldField(String key) {
		return new Field<Object, Model, Model>(Object.class, Model.class, 
				Model.class, key, ReferenceDepth.FIELD);
	}
	
	/**
	 * Create a new custom-parameterized field field
	 * @param key - the key to use
	 * @param modelType - the expected model class
	 * @return the field
	 */
	public static <MT extends Model> Field<Object, MT, MT> 
			fieldField(Class<MT> modelType, String key) {
		
		return new Field<Object, MT, MT>(Object.class, modelType, 
				modelType, key, ReferenceDepth.FIELD);
	}
	
	/**
	 * Create a new standard-parameterized value field
	 * @param key - the key to use
	 * @return the field
	 */
	public static Field<Object, Model, Object> valueField(String key) {
		return new Field<Object, Model, Object>(Object.class, Model.class, 
				Object.class, key, ReferenceDepth.VALUE);
	}
	
	/**
	 * Create a new custom-parameterized value field
	 * @param valueType - the expected value class
	 * @param key - the key to use
	 * @return the field
	 */
	public static <VT> Field<VT, Model, VT> valueField(Class<VT> valueType, 
			String key) {
		
		return new Field<VT, Model, VT>(valueType, Model.class, 
				valueType, key, ReferenceDepth.VALUE);
	}
	
	protected String key;
	protected Class<ValueType> valueType;
	protected Class<ModelType> modelType;
	protected Class<ReferenceType> referenceType;
	private ReferenceDepth referenceDepth;
	
	/**
	 * Create a new field with all the parameters specified,
	 * may throw InvalidPathException if something doesn't add up
	 * @param valueType - the expected value class
	 * @param modelType - the expected model class
	 * @param referenceType - the expected reference class
	 * @param key - the key of the field
	 * @param referenceDepth - the depth of the field's reference
	 */
	public Field(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, String key, 
			ReferenceDepth referenceDepth) {
		
		this.key = key;
		this.valueType = valueType;
		this.modelType = modelType;
		this.referenceType = referenceType;
		this.referenceDepth = referenceDepth;
		
		Path.validateKey(key);
		
		if(referenceDepth == ReferenceDepth.VALUE) {
			if(valueType != referenceType)
				throw new InvalidPathException("Value Fields must refer to ValueType.");
		}
		else {
			if(!Utility.aExtendsB(modelType, referenceType))
				throw new InvalidPathException("Model Fields must refer to a " +
						"valid type.");
		}
	}

	@Override
	public ReferenceDepth getReferenceDepth() {
		return referenceDepth;
	}
	
	@Override
	protected Field<?,?,?> getFieldByIndexSafe(int index) {
		return this;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public Path<ValueType, ModelType, ModelType> toFieldPath() {
		// TODO WRITE THIS
		return null;
	}

	@Override
	public Path<ValueType, ModelType, ModelType> toModelPath() {
		// TODO WRITE THIS
		return null;
	}

	@Override
	public Path<ValueType, ModelType, ValueType> toValuePath() {
		// TODO WRITE THIS
		return null;
	}
	
	@Override
	public String toString() {
		String s = key;
		if(referenceDepth == ReferenceDepth.VALUE)
			s += "$";
		else if(referenceDepth == ReferenceDepth.FIELD)
			s += "*";
		return s;
	}
	
}
