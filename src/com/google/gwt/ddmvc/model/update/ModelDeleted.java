package com.google.gwt.ddmvc.model.update;

/**
 * Update sent as a notification when a model value has been removed.
 * Here the target value is the model that was deleted.
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
	public ModelDeleted(String target) {
		super(target);
		this.isComplete = true;
	}

	@Override
	protected Object performUpdate(Object value) {
		return null;
	}
}