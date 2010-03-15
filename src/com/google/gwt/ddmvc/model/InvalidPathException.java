package com.google.gwt.ddmvc.model;

/**
 * Thrown when a path fails to parse properly
 * @author Kevin Dolan
 */
public class InvalidPathException extends RuntimeException {

	private static final long serialVersionUID = 1052814728601364227L;

	public InvalidPathException(String message) {
		super(message);
	}
	
}
