package com.google.gwt.ddmvc.model;

import java.util.Collection;
import com.google.gwt.ddmvc.model.Model.UpdateLevel;
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
	 * Notify the observers of a given level of a change to this observer
	 */
	public void notifyObservers(ModelUpdate update, UpdateLevel level);
	
	/**
	 * @return the path associated with this observer
	 */
	public Path getPath();

	/**
	 * @return true if this model, or any of its child models has any type of
	 * observers
	 */
	boolean hasObservers();
}
