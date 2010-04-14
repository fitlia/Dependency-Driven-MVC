package com.google.gwt.ddmvc.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.path.Field;
import com.google.gwt.ddmvc.model.path.Property;
import com.google.gwt.ddmvc.model.path.SubModel;

/**
 * An ObjectModel attempts to emulate a native Java object in a manner
 * more amenable to key-value observing.  It operates by defining several
 * fields and limiting updates made to the model to those particular fields.
 * 
 * The Object model has a number of static factory methods for convenience for
 * creating Properties and SubModels.  They should be utilized when defining
 * fields for an Object Model.
 * 
 * Attempts to set the model for a given field (from setChild) in an ObjectModel
 * will defer the decision to the Field's isValidModel method.
 * 
 * @author Kevin Dolan
 */
public abstract class ObjectModel extends Model {
	
	private Map<String, Field<?,?,?>> fields;
	
	//
	// Constructor
	//
	
	/**
	 * Create a new ObjectModel with the fields packed in an array.
	 * Once created, the fields cannot be changed.
	 * @param fields - the fields to create in an array.
	 */
	public ObjectModel(Field<?,?,?>[] fields) {
		this.fields = new HashMap<String, Field<?,?,?>>();
		for(Field<?,?,?> field : fields) {
			this.fields.put(field.getKey(), field);
			setChild(field.getKey(), field.getModel());
		}
	}

	//
	// Model Method Overriding
	//
	
	@Override
	protected void setChild(String key, Model model) {
		if(!fields.containsKey(key))
			throw new InvalidPathException("Key " + key + " is not a field in " +
					"ObjectModel at " + getPath());
		
		Field<?,?,?> field = fields.get(key);
		if(!field.isValidModel(model))
			throw new InvalidPathException("New Model is not compatible with " +
					"ObjectModel at " + getPath());
		
		super.setChild(key, model);
	}
	
	//
	// Factory Field Methods
	//
	
	/**
	 * Create a new Property 
	 * @param <T> - the Type to be stored in the model
	 * @param cls - the class of the property
	 * @param key - the key to represent the model
	 * @return the new property
	 */
	protected static <T> Property<T> property(Class<T> cls, String key) {
		return Property.make(cls, key);
	}
	
	/**
	 * Create a new Property 
	 * @param <T> - the Type to be stored in the model
	 * @param cls - the class of the property
	 * @param key - the key to represent the model
	 * @param defaultValue - the value to default to
	 * @return the new property
	 */
	protected static <T> Property<T> property(Class<T> cls, String key,
			T defaultValue) {
		return Property.make(cls, key, defaultValue);
	}
	
	/**
	 * Create a new Property, with the class packed into the default value
	 * @param <T> - the Type to be stored in the model
	 * @param key - the key to represent the model
	 * @param defaultValue - the value to default to
	 * @return the new property
	 */
	protected static <T> Property<T> property(String key, T defaultValue) {
		return Property.make(key, defaultValue);
	}
	
	/**
	 * Create a new SubModel 
	 * @param <ModelType extends Model> - the Type to be stored in the model
	 * @param key - the key to represent the model
	 * @return the new property
	 */
	protected static <ModelType extends Model> SubModel<ModelType> 
			subModel(Class<ModelType> cls, String key) {
		
		return SubModel.make(cls, key);
	}
	
}
