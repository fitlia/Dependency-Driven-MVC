package com.google.gwt.ddmvc.model.exception;

import com.google.gwt.ddmvc.model.path.DefaultPath;

/**
 * Thrown when an attempt is made to access a model which does not exist.
 * @author Kevin Dolan
 */
public class ModelDoesNotExistException extends RuntimeException {

	private static final long serialVersionUID = 6676614531981476205L;

	private DefaultPath<?,?,?> path;
	
	public ModelDoesNotExistException(DefaultPath<?,?,?> path) {
		super("The model path " + path + " does not exist.");
		this.path = path;
	}
	
	public DefaultPath<?,?,?> getPath() {
		return path;
	}
	
}
