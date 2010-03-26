package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.Utility;
import com.google.gwt.ddmvc.event.Observer;
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
 *
 * @param <Type> the type of value held by this model
 */
public class ValueModel<Type> extends Model {
	
	/**
	 * Instantiate a new Value Model of the given type
	 * @param <Type> the type (packed into the class)
	 * @param cls - the class of the value held by this model
	 * @return a ValueModel of the given type
	 */
	public static <Type> ValueModel<Type> make(Class<Type> cls) {
		return new ValueModel<Type>(cls, null);
	}
	
	/**
	 * Instantiate a new Value Model, whose type is defined by whatever class the
	 * initial value is
	 * @param <Type> the type (packed into the value)
	 * @param value - the value to set initially
	 * @return a ValueModel of the given type
	 */
	@SuppressWarnings("unchecked")
	public static <Type> ValueModel<Type> make(Type value) {
		return new ValueModel<Type>((Class<Type>) value.getClass(), value);
	}
	
	/**
	 * Instantiate a new Value Model of the given type, with a preset value
	 * @param <Type> the type (packed into the class)
	 * @param cls - the class of the value held by this model
	 * @param value - the value to set initially
	 * @return a ValueModel of the given type
	 */
	public static <Type> ValueModel<Type> make(Class<Type> cls, Type value) {
		return new ValueModel<Type>(cls, value);
	}
	
	private Class<Type> cls;
	
	/**
	 * Instantiate a new ValueModel with the given value
	 * @param value - the value of the model
	 */
	private ValueModel(Class<Type> cls, Type value) {
		super();
		
		if(cls.isInterface())
			throw new IllegalArgumentException("Class cannot be an interface.");
		this.cls = cls;
		this.value = value;
	}
	
	/**
	 * @return the class reference held by this ValueModel
	 */
	public Class<Type> getValueClass() {
		return cls;
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
	protected void handleUpdateSafe(ModelUpdate update, Path<?> relative) {
		if(relative.getImmediate() == null)
			applyUpdate(update);
		else
			throw new InvalidPathException(getPath() + " is a value model.  It " +
					"cannot have any children.");
	}
	
	@Override
	protected void applyUpdate(ModelUpdate update) {
		Object result = update.process(value);
		
		if(result.getClass().getName()
				.equals(ModelUpdate.SET_MODEL_TO.class.getName())) {
			
			notifyObservers(update, UpdateLevel.REFERENCE);
			getParent().setChild(getKey(), 
					(((ModelUpdate.SET_MODEL_TO) result).getModel()));
		}
		else {
			if(!Utility.aExtendsB(result.getClass(), cls))
				throw new ClassCastException(result.getClass().getName() 
						+ " cannot be cast to " + cls.getName());
			notifyObservers(update, UpdateLevel.VALUE);
			value = result;
		}
	}
}
