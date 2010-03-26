package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.Utility;


/**
 * A Property is a Field that stores a native Java object (by means of a 
 * ValueModel).  Fields can optionally define a default value, which the
 * field will default to if no value is set.  Note that this default value
 * will be referentially linked, so for example, if you were to set the
 * default value to an empty list and then add an element to the list for
 * any model with this property, it will change for all models with this
 * property, however, setting the value to something else will not affect
 * the default value, because that would merely be changing the reference.
 *  
 * @author Kevin Dolan
 * @param <Type> - the type of object to store
 */
public class Property<Type> extends Field<Type, ValueModel, Type> {
	
	/**
	 * Instantiate a new property with a null default value
	 * @param cls - the class of the parameterized type
	 * @param key - the key of this property
	 */
	public static <Type> Property<Type> make(Class<Type> cls, String key) {
		return new Property<Type>(cls, key, null);
	}
	
	/**
	 * Instantiate a new property with the class packed in the defaultValue
	 * @param key - the key of this property
	 * @param defaultValue - the default value to set
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Property<Type> make(String key, Type defaultValue) {
		return new Property<Type>((Class<Type>) defaultValue.getClass(),
				key, defaultValue);
	}
	
	/**
	 * Instantiate a new property
	 * @param cls - the class of the parameterized type
	 * @param key - the key of this property
	 * @param defaultValue - the default value to set
	 */
	public static <Type> Property<Type> 
			make(Class<Type> cls, String key, Type defaultValue) {
		
		return new Property<Type>(cls, key, defaultValue);
	}
	private Type defaultValue;

	/**
	 * Instantiate a new property
	 * @param cls - the class of the parameterized type
	 * @param key - the key of this property
	 */
	private Property(Class<Type> cls, String key, Type defaultValue) {
		super(cls, ValueModel.class, cls, FieldType.VALUE, key);
		
		this.defaultValue = defaultValue;
	}

	@Override
	public Model getModel() {
		return ValueModel.make(getValueType(), defaultValue);
	}

	/**
	 * @return the default value for this property
	 */
	public Type getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public String getPathString() {
		return key + ".$";
	}

	@Override
	public boolean isValidModel(Model model) {
		if(!Utility.aExtendsB(model.getClass(), ValueModel.class))
			return false;
		
		ValueModel<?> vm = (ValueModel<?>) model;
		return Utility.aExtendsB(vm.getValueClass(), getValueType());
	}
	
}
