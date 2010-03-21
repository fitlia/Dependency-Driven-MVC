package com.google.gwt.ddmvc.model;


/**
 * A Property is a Field that stores a native Java object (by means of a 
 * ValueModel)
 *  
 * @author Kevin Dolan
 * @param <Type> - the type of object to store
 */
public class Property<Type> extends Field {
	
	public Property(String key) {
		super(key);
	}

	@Override
	public Model getModel() {
		return new ValueModel<Type>();
	}
	
}
