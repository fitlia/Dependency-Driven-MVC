package com.google.gwt.ddmvc.view;

import com.google.gwt.ddmvc.Observer;
import com.google.gwt.user.client.ui.Widget;

/**
 * A View is the direct connection with the end-user.  It creates the actual UI widget and
 * dispatches relevant user-generated events
 * 
 * @author Kevin Dolan
 */
public abstract class View implements Observer {
	
	/**
	 * Instantiate a new View with the given executive object
	 * @param executive the executive DDMVC object to reference
	 */
	public View() {
		initialize();
		render();
	}
	
	/**
	 * @return this View's widget to be displayed
	 */
	public abstract Widget getWidget();
	
	@Override
	public void modelChanged() {
		render();
	}
	
	@Override
	public boolean canHaveObservers() {
		return false;
	}
	
	/**
	 * Initialize the top-level widget.
	 * Note: take care to maximize the rendering of everything that will not change,
	 * but this method should usually make a call to render to render anything that will.
	 */
	public abstract void initialize();
	
	/**
	 * Render the already intantiated components.  Should not change the Widget reference.
	 */
	public abstract void render();
	
	
}
