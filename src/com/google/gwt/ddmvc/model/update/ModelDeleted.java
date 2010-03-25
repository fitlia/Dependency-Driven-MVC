package com.google.gwt.ddmvc.model.update;

import com.google.gwt.ddmvc.model.Path;

/**
 * Update sent as a notification when a model is deleted.
 * 
 * If this model update were actually passed to a model, it
 * would have no effect.
 * 
 * @author Kevin Dolan
 */
public class ModelDeleted extends ModelUpdate {
	
	/**
	 * The default ModelDeleted field, used for comparison
	 */
	public static final ModelDeleted DEFAULT = new ModelDeleted(null);
	
	/**
	 * @param target
	 */
	public ModelDeleted(Path<?> target) {
		super(target);
		this.isComplete = true;
	}

	@Override
	protected Object performUpdate(Object value) {
		return null;
	}
	
}