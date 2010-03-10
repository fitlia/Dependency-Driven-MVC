package com.google.gwt.ddmvc;

/**
 * The Observer interface represents the relationship of depending on some model value
 * @author Kevin Dolan
 */
public interface Observer {

	/**
	 * Notify this observer that the model has changed
	 */
	public void modelChanged();
	
	/**
	 * @return true if this class implements CanHaveObservers
	 */
	public boolean canHaveObservers();
}
