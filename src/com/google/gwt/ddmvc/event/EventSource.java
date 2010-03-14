package com.google.gwt.ddmvc.event;

import com.google.gwt.ddmvc.DDMVC;

/**
 * This interface defines anything which has the ability to generate AppEvents
 * @author Kevin Dolan
 */
public abstract class EventSource {
	
	/**
	 * Fire an AppEvent on the next run-loop.
	 * @param event - the event to fire, does not need to have source set.
	 */
	protected void fireEvent(AppEvent event) {
		event.setSource(this);
		DDMVC.fireEvent(event);
	}
	
}
