package com.google.gwt.ddmvc.model;

/**
 * Thrown when a dependency of a computed model is not found
 * @author Kevin Dolan
 */
public class DependencyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 3516034865405907676L;

	private String key;
	
	public DependencyNotFoundException(String key) {
		super("A model depended on " + key + ", but key was not found.");
	}
	
	public String getKey() {
		return key;
	}
	
}
