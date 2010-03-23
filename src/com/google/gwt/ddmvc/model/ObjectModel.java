package com.google.gwt.ddmvc.model;

/**
 * An ObjectModel attempts to emulate a native Java object in a manner
 * more amenable to key-value observing.  It operates by defining several
 * fields and limiting updates made to the model to those particular fields.
 * It supports fields being model by the SubModel field.
 * 
 * Note that the class must not be an interface, @see ValueModel for 
 * explanation.
 * 
 * @author Kevin Dolan
 */
public abstract class ObjectModel extends Model {
	
	/**
	 * Create a new ObjectModel with the fields packed in an array.
	 * Once created, the fields cannot be changed.
	 * @param fields - the fields to create in an array.
	 */
	public ObjectModel(Field[] fields) {
		for(Field field : fields)
			super.setChild(field.getKey(), field.getModel());
	}
	
	/**
	 * Get the value of a child by the property field
	 * @param <Type> - the type of object to be returned (packed into 
	 * the property)
	 * @param property - the property to access (should be owned by the model)
	 * @return the value stored in that property
	 */
	@SuppressWarnings("unchecked")
	public <Type> Type get(Property<Type> property) {
		return (Type) getValue(property.getKey());
	}
	
	/**
	 * Get a child model by a subModel field
	 * @param <ModelType> - the type of model to be returned (packed into 
	 * the sub-model)
	 * @param subModel - the subModel to access (should be owned by the model)
	 * @return the model stored in that child model
	 */	
	@SuppressWarnings("unchecked")
	public <ModelType extends Model> ModelType 
			get(SubModel<ModelType> subModel) {
		
		return (ModelType) getChild(subModel.getKey());
	}
	
	/**
	 * Create a new Property 
	 * @param <T> - the Type to be stored in the model
	 * @param cls - the class of the property
	 * @param key - the key to represent the model
	 * @return the new property
	 */
	protected static <T> Property<T> property(Class<T> cls, String key) {
		return Property.create(cls, key);
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
		return Property.create(cls, key, defaultValue);
	}
	
	/**
	 * Create a new Property, with the class packed into the default value
	 * @param <T> - the Type to be stored in the model
	 * @param key - the key to represent the model
	 * @param defaultValue - the value to default to
	 * @return the new property
	 */
	protected static <T> Property<T> property(String key, T defaultValue) {
		return Property.create(key, defaultValue);
	}
	
	/**
	 * Create a new SubModel 
	 * @param <ModelType extends Model> - the Type to be stored in the model
	 * @param key - the key to represent the model
	 * @return the new property
	 */
	protected static <ModelType extends Model> SubModel<ModelType> 
			subModel(Class<ModelType> cls, String key) {
		
		return SubModel.create(cls, key);
	}
	
}
