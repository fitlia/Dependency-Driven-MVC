package com.google.gwt.ddmvc.model.update;

/**
<<<<<<< HEAD:src/com/google/gwt/ddmvc/model/update/CascadeUpdate.java
 * Update sent as a notification when a data change is cascaded 
=======
 * Update sent as a notification when a data change is cascaded  
>>>>>>> testBranch3:src/com/google/gwt/ddmvc/model/update/CascadeUpdate.java
 * to a computed model.  At this time, there is no way to know
 * the source of a cascaded data change.
 * If this model update were actually passed to a model, it
 * would have no effect.
 * @author Kevin Dolan
 */
public class CascadeUpdate extends ModelUpdate {
	
	/**
	 * @param target
	 */
	public CascadeUpdate(String target) {
		super(target, null);
	}

	@Override
	public Object performUpdate(Object value) {
		return null;
	}
}