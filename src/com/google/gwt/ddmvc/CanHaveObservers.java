package com.google.gwt.ddmvc;

import java.util.Set;

/**
 * Interface implemented by all objects which are capable of holding observers
 * @author Kevin Dolan
 */
public interface CanHaveObservers {

	public Set<Observer> getObservers();
	
}
