package com.google.gwt.ddmvc;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The Observer interface represents the relationship of depending on some model
 * value 
 * @author Kevin Dolan
 */
public interface Observer {

	/**
	 * Notify this observer that the model has changed.
	 * @param updates - the collection of updates applied
	 */
	public void modelChanged(Collection<ModelUpdate> updates);
	
	/**
	 * @return the list of dependents of this observer, if any, or a blank list,
	 *					if none
	 */
	public Set<Observer> getObservers();
	
	/**
	 * @return the key associated with this observer
	 */
	public String getKey();
}
