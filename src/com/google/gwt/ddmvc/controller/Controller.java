package com.google.gwt.ddmvc.controller;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.event.AppEvent;
import com.google.gwt.ddmvc.event.EventSource;

/**
 * Controllers represent business logic. 
 * @author Kevin Dolan
 */
public abstract class Controller extends EventSource {
	
	/**
	 * Respond to an event, however you see fit.  Return a ServerRequest object
	 * if you would like to make a request to the server
	 * @param event - the event to respond to
	 * @return any server request that should occur, null if none
	 */
	public abstract ServerRequest respondToEvent(AppEvent event);
	
	/**
	 * Subscribe to a particular type of event.  Generally would be best to call
	 * in the constructor.  Convenient alternative to DDMVC.subscribeToEvent(...)
	 * @param event - the event class to subscribe to
	 */
	protected void subscribeToEvent(Class<? extends AppEvent> event) {
		DDMVC.subscribeToEvent(event, this);
	}
	
}
