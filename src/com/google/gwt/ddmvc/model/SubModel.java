package com.google.gwt.ddmvc.model;

/**
 * A SubModel is a Field that stores some type of Model (by means of a 
 * ModelModel)
 *  
 * @author Kevin Dolan
 * @param <Type> - the type of object to store
 */
public class SubModel<ModelType extends Model> extends Field {
	
	public SubModel(String key) {
		super(key);
	}

	@Override
	public Model getModel() {
		return new ModelModel<ModelType>();
	}
	
}
