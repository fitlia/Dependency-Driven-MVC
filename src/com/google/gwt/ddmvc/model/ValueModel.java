package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * A value model is a model which can only contain a value of a particular
 * type.  It is not allowed to have any child models.  It adds a layer of
 * optional type-safety to your models.
 * 
 * Note - the parameterized type allows for some compile-time type checking,
 * but is effectively disregarded at run-time.  Unfortunately, there is 
 * nothing to stop you from sending some other type of value to be set
 * through the usual means.  However, you can use the set(...) method
 * instead to enforce type-safety on yourself.
 * 
 * In general, this is most useful as a utility class for ObjectModel, and not
 * necessarily created directly.
 * 
 * @author Kevin Dolan
 *
 * @param <Type> the type of value held by this model
 */
public class ValueModel<Type> extends Model {

	/**
	 * Instantiate a new blank ValueModel
	 */
	public ValueModel() {
		super();
	}
	
	/**
	 * Instantiate a new ValueModel with the given value
	 * @param value - the value of the model
	 */
	public ValueModel(Type value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Type getValue() {
		return (Type) super.getValue();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Type getValue(Observer observer) {
		return (Type) super.getValue(observer);
	}
	
	/**
	 * Type-safe value setting method.
	 * Use this instead of setValue(...) to enforce type-safety in your own code.
	 * @param value - the value to set
	 */
	public void set(Type value) {
		super.setValue(value);
	}
	
	@Override
	protected void handleUpdateSafe(ModelUpdate update, Path relative) {
		if(relative.getImmediate() == null)
			applyUpdate(update);
		else if(relative.getImmediate().equals("$"))
			throw new InvalidPathException("Update path cannot end with '$'.");
		else if(relative.getImmediate().equals("*"))
			throw new InvalidPathException("Update path cannot end with '*'.");
		else
			throw new InvalidPathException(getPath() + " is a value model.  It " +
					"cannot have any children.");
	}
	
}
