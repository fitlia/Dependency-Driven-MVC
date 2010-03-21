package com.google.gwt.ddmvc.model;

import java.util.Set;
import com.google.gwt.ddmvc.event.Observer;
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
	 * Note - that if the default ModelModel, that a default Model is put in its
	 * place temporarily to keep track of observers and make sure no errors are
	 * encountered.  In general, you should take care to set the model before
	 * it's too late, though.
	 */
	@SuppressWarnings("unchecked")
	public ModelModel() {
		super();
		this.model = (ModelType) new Model();
		model.setKey(getKey());
		model.setParent(getParent());
	}
	
	/**
	 * Instantiate a new ModelModel with the given model
	 * @param model - the value of the model
	 */
	public ModelModel(ModelType model) {
		super(null, null, null);
		this.model = model;
		model.setKey(getKey());
		model.setParent(getParent());
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
		this.model.setKey(key);
	}
	
	@Override
	protected void setParent(Model model) {
		super.setParent(model);
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
	public Set<Observer> getReferentialObservers() {
		return model.getReferentialObservers();
	}
	
	@Override
	public Set<Observer> getValueObservers() {
		return model.getValueObservers();
	}
	
	@Override
	public Set<Observer> getFieldObservers() {
		return model.getFieldObservers();
	}
	
	@Override
	public void addReferentialObserver(Observer observer) {
		model.addReferentialObserver(observer);
	}
	
	@Override
	public void addValueObserver(Observer observer) {
		model.addValueObserver(observer);
	}

	@Override
	public void addFieldObserver(Observer observer) {
		model.addFieldObserver(observer);
	}
	
	@Override
	public void removeReferentialObserver(Observer observer) {
		model.removeReferentialObserver(observer);
	}
	
	@Override
	public void removeValueObserver(Observer observer) {
		model.removeValueObserver(observer);
	}
	
	@Override
	public void removeFieldObserver(Observer observer) {
		model.removeFieldObserver(observer);
	}
	
	@Override
	protected Object myValue() {
		return model.getValue();
	}
	
	@Override
	protected void handleUpdateSafe(ModelUpdate update, Path relative) {
		if(relative.getImmediate() == null)
			model.applyUpdate(update);
		else if(relative.getImmediate().equals("$"))
			throw new InvalidPathException("Update path cannot end with '$'.");
		else if(relative.getImmediate().equals("*"))
			throw new InvalidPathException("Update path cannot end with '*'.");
		else
			model.handleUpdateSafe(update, relative);
	}
	
	@Override
	protected void applyUpdate(ModelUpdate update) {
		model.applyUpdate(update);
	}
	
}
