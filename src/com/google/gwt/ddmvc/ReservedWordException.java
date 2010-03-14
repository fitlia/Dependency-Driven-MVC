package com.google.gwt.ddmvc;

public class ReservedWordException extends RuntimeException {

	private static final long serialVersionUID = 637110134066584618L;

	public ReservedWordException(String word) {
		super("The reserved word " + word + " was used, but is not allowed.");
	}
	
}
