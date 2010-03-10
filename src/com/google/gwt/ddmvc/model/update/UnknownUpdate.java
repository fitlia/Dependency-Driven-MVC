package com.google.gwt.ddmvc.model.update;

/**
 * Update sent as a notification when the type of update
 * performed is not known.  This should really only be
 * caused by call to Model.update(), and shouldn't be used
 * except to respond to.
 * If this model update were actually passed to a model, it
 * would have no effect.
 * @author Kevin Dolan
 */
public class UnknownUpdate extends ModelUpdate {
	
	/**
	 * @param target
	 */
	public UnknownUpdate(String target) {
		super(target, null);
	}

	@Override
	public Object performUpdate(Object value) {
		return null;
	}
}