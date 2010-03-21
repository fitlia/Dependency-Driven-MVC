package com.google.gwt.ddmvc.model.exception;

import com.google.gwt.ddmvc.model.Path;

/**
 * Thrown when an attempt is made to access a model which does not exist.
 * @author Kevin Dolan
 */
public class ModelDoesNotExistException extends RuntimeException {

	private static final long serialVersionUID = 6676614531981476205L;

	private Path path;
	
	public ModelDoesNotExistException(Path path) {
		super("The model path " + path + " does not exist.");
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}
	
}
