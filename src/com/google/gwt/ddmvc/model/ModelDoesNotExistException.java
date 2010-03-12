package com.google.gwt.ddmvc.model;

/**
 * Thrown when an attempt is made to access a model which does not exist.
 * @author Kevin Dolan
 */
public class ModelDoesNotExistException extends RuntimeException {

	private static final long serialVersionUID = 6676614531981476205L;

	private String key;
	
	public ModelDoesNotExistException(String key) {
		super("The model key " + key + " does not exist.");
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
}
