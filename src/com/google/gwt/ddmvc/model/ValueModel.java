package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.Utility;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * A value model is a model which can only contain a value of a particular
 * type.  It is not allowed to have any child models.  It adds a layer of
 * optional type-safety to your models.
 * 
 * The parameterized type must not be an interface. This is a workaround to the
 * fact that GWT emulation does not provide as much run-time class reflection
 * as native Java, and thus does not persist interface implementation
 * information.
 * 
 * In general, this is most useful as a utility class for ObjectModel, and not
 * necessarily created directly.
 * 
 * @author Kevin Dolan
 */
public class ValueModel extends Model {
	
	private Class<?> cls;
	
	/**
	 * Instantiate a new ValueModel with the given value, where the class is
	 * determined by the type of value given
	 * @param value - the value of the model
	 */
	public ValueModel(Object value) {
		super();		
		
		this.cls = value.getClass();
		this.value = value;
	}
	
	/**
	 * Instantiate a new ValueModel with the given class
	 * @param cls - the class to use to enforce type-safety
	 */
	public ValueModel(Class<?> cls) {
		super();		
		
		this.cls = cls;
		this.value = null;
	}
	
	/**
	 * Instantiate a new ValueModel with the given value
	 * @param cls - the class to use to enforce type-safety
	 * @param value - the value of the model
	 */
	public ValueModel(Class<?> cls, Object value) {
		super();
		
		if(cls.isInterface())
			throw new IllegalArgumentException("Class cannot be an interface.");
		
		this.cls = cls;
		this.value = value;
	}
	
	/**
	 * @return the class reference held by this ValueModel
	 */
	public Class<?> getValueClass() {
		return cls;
	}
	
	@Override
	protected void handleUpdateSafe(ModelUpdate update, Path<?,?,?> relative) {
		if(relative.getImmediate() == null)
			applyUpdate(update);
		else
			throw new InvalidPathException(getPath() + " is a value model.  It " +
					"cannot have any children.");
	}
	
	@Override
	protected void resetValue(Object value) {
		if(!Utility.aExtendsB(value.getClass(), cls))
			throw new ClassCastException(getPath() 
					+ " cannot be cast to " + cls.getName());
		super.resetValue(value);
	}
}
