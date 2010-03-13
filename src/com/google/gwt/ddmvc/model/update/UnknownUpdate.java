package com.google.gwt.ddmvc.model.update;

/**
 * Update sent as a notification when the type of update 
 * performed is not known.
 * @author Kevin Dolan
 */
public class UnknownUpdate extends ModelUpdate {
	
	/**
	 * The default UnknownUpdate field, used for comparison
	 */
	public static final UnknownUpdate DEFAULT = new UnknownUpdate(null);
	
	/**
	 * @param target
	 */
	public UnknownUpdate(String target) {
		super(target);
		this.isComplete = true;
	}

	@Override
	protected Object performUpdate(Object value) {
		return null;
	}
}