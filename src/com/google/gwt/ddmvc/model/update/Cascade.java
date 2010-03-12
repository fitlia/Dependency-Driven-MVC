package com.google.gwt.ddmvc.model.update;

/**
 * Update sent as a notification when a data change is cascaded
 * from a computed model.  The target key here refers to the computed model
 * that was already changed.
 * 
 * If this model update were actually passed to a model, it
 * would have no effect.
 * 
 * @author Kevin Dolan
 */
public class Cascade extends ModelUpdate {
	
	/**
	 * The default Cascade field, used for comparison
	 */
	public static final Cascade DEFAULT = new Cascade(null);
	
	/**
	 * @param target
	 */
	public Cascade(String target) {
		super(target);
	}

	@Override
	protected Object performUpdate(Object value) {
		return null;
	}
	
}