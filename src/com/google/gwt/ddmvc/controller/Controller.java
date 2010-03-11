package com.google.gwt.ddmvc.controller;

import com.google.gwt.ddmvc.view.View;

/**
<<<<<<< HEAD:src/com/google/gwt/ddmvc/controller/Controller.java
 * Controllers represent business logic. 
=======
 * Controllers represent business logic.  
>>>>>>> testBranch3:src/com/google/gwt/ddmvc/controller/Controller.java
 * 
 * @author Kevin Dolan
 */
public abstract class Controller {
	
	/**
	 * Execute this controller's action
	 * @param source the view calling the execute
	 */
	public abstract void execute(View source);
	
	/**
	 * Determine whether or not the action is valid, optional
	 * @param source the view calling the execute
	 * @return		 null if the action is valid, otherwise some message
	 */
	public String validate(View source) {
		return null;
	}
	
	/**
	 * Call-back method for successful completion of remote call, optional
	 * @param source the view that called the execute
	 */
	public void onSuccess(View source) {};
	
	/**
	 * Call-back method for error returned by remote call, optional
	 * @param source the view that called the execute
	 */
	public void onError(View source) {};
	
}
