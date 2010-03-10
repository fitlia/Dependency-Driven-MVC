package com.google.gwt.ddmvc;

import java.util.List;

import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The Observer interface represents the relationship of depending on some model value
 * @author Kevin Dolan
 */
public interface Observer {

	/**
	 * Notify this observer that the model has changed
	 * @param updates the list of updates applied
	 */
	public void modelChanged(List<ModelUpdate> updates);
	
	/**
	 * @return true if this class implements CanHaveObservers
	 */
	public boolean canHaveObservers();
}
