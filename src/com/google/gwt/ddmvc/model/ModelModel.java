package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * A ModelModel holds a model as its value, and routes requests through
 * to the model as if that model were in this models place, but it allows
 * you to specify a model-type useful for compile-time type checking.
 * Like ValueModel, this just adds an extra layer of optional type-safety
 * which you can use to help you debug earlier.
 * 
 * In general, this is most useful as a utility class for ObjectModel, and not
 * necessarily created directly.
 * 
 * @author Kevin Dolan
 *
 * @param <ModelType> the type of model held by this model
 */
public class ModelModel<ModelType extends Model> extends Model {

	private ModelType model;
	
	/**
	 * Instantiate a new blank ModelModel
	 * Note - if the default constructor is used, it is effectively the equivalent
	 * of declaring a field as null in Java.  Attempts to modify this model
	 * without first setting the model to something else will fail.
	 */
	public ModelModel() {
		super();
		this.model = null;
	}
	
	/**
	 * Instantiate a new ModelModel with the given model
	 * @param model - the value of the model
	 */
	public ModelModel(ModelType model) {
		super();
		this.model = model;
		if(getKey() != null && getParent() != null) {
			model.setKey(getKey());
			model.setParent(getParent());
		}
	}
	
	/**
	 * @return a direct reference to the model being held
	 */
	public ModelType getModel() {
		return model;
	}
	
	@Override
	protected void setKey(String key) {
		super.setKey(key);
		if(model != null)
			this.model.setKey(key);
	}
	
	@Override
	protected void setParent(Model model) {
		super.setParent(model);
		if(this.model != null)
			this.model.setParent(model);
	}
	
	@Override
	public boolean hasChild(String key) {
		return model.hasChild(key); 
	}
	
	@Override
	protected Model getChild(String key) {
		return model.getChild(key);
	}
	
	@Override
	protected void setChild(String key, Model model) {
		this.model.setChild(key, model);
	}
	
	@Override
	protected Object myValue() {
		return model.getValue();
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
			model.handleUpdateSafe(update, relative);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void applyUpdate(ModelUpdate update) {		
		Object result = update.process(model.myValue());
		
		if(result.getClass().getName()
				.equals(ModelUpdate.SET_MODEL_TO.class.getName())) {
			
			notifyObservers(update, UpdateLevel.REFERENTIAL);
			model = (ModelType) ((ModelUpdate.SET_MODEL_TO) result).getModel();
			model.setKey(getKey());
			model.setParent(getParent());
		}
		else {
			notifyObservers(update, UpdateLevel.VALUE);
			model.value = result;
		}
	}
	
}
