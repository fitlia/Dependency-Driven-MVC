package com.google.gwt.ddmvc;

import java.util.Collection;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The Observer interface represents the relationship of depending on some model
 * value 
 * @author Kevin Dolan
 */
public interface Observer {

	/**
	 * Notify this observer that the model has changed.
	 * Initially, no optimizations need to take place allowing for rapid-
	 * prototyping, but you should eventually take care to pay attention to which
	 * updates were applied to optimize runtime.  Order of updates is guaranteed
	 * to be the order they were applied.
	 * @param updates the collection of updates applied
	 */
	public void modelChanged(Collection<ModelUpdate> updates);
	
	/**
	 * @return true if this class extends Model
	 */
	public boolean isModel();
}
