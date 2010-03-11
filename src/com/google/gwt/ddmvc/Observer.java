package com.google.gwt.ddmvc;

import java.util.Set;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The Observer interface represents the relationship of depending on some model
 * value
 * @author Kevin Dolan
 */
public interface Observer {

	/**
	 * Notify this observer that the model has changed
	 * @param updates the set of updates applied, order is not guaranteed
	 */
	public void modelChanged(Set<ModelUpdate> updates);
	
	/**
	 * @return true if this class extends Model
	 */
	public boolean isModel();
}
