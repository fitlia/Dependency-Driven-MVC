package com.google.gwt.ddmvc.controller;

import java.util.List;
import com.google.gwt.ddmvc.event.AppEvent;

/**
 * The Standard Controller abstracts away some of the complexities of a common
 * controller model: validate the inputs, respond to potential validation
 * failure, make immediate changes (if validation succeeds), send a server
 * request, and respond to either success of failure from the server request.
 * @author Kevin Dolan
 */
public abstract class StandardController extends Controller {

	@Override
	public ServerRequest respondToEvent(AppEvent event) {
		List<ValidationError> errors = validate(event);
		if(errors == null || errors.size() == 0)
			return execute(event);			
		else {
			onValidationFailure(event, errors);
			return null;
		}
	}
	
	/**
	 * Validate the inputs, and if there are any validation errors, return them.
	 * They will be handled by onValidationFailure() if implemented.  
	 * If validation errors are found, there will be no calls beyond 
	 * onValidationFailure().
	 * If an empty list or null is returned,  the process will continue
	 * @param event - the event to respond to
	 * @return null by default
	 */
	protected List<ValidationError> validate(AppEvent event) {
		return null;
	}
	
	/**
	 * Respond to a failure of validation.  This will be called if validate()
	 * returns a list with validation errors.  By default, the method does
	 * nothing, so its implementation is optional.
	 * @param event - the event that caused the validation failure
	 * @param errors - the list of errors returned by validate
	 */
	protected void onValidationFailure(AppEvent event, 
			List<ValidationError> errors) {}

	/**
	 * Execute whatever you can before sending a request to the server.  This
	 * might include setting some temporary values to be displayed, setting
	 * load-state parameters, etc.  
	 * Return the ServerRequest (if any) to make.  The request will be sent at
	 * the end of the method call and will invoke this controller's server
	 * response methods when it is received.
	 * This is the only method whose implementation is absolutely necessary
	 * @param event - the event the is being executed
	 * @return the request to be sent to the server, or null if none necessary
	 */
	protected abstract ServerRequest execute(AppEvent event);
	
	/**
	 * Respond to a successful server request.  This will be called when the
	 * message comes back from the server.  Implementation is optional, and
	 * the ModelUpdates returned by the ServerRequest will be applied
	 * automatically, regardless.
	 * @param event - the event that led to the request
	 */
	protected void onRequestSuccess(AppEvent event) {}
	
	/**
	 * Respond to a failure of a server request.  This will be called if the
	 * message coming back from the server was a failure.
	 * Again, the implementation of this method is optional; by default it
	 * does nothing. It should generally be overridden to display some
	 * message to the user and roll back any changes made in execute().
	 * @param event - the event that failed
	 */
	protected void onRequestFailure(AppEvent event) {}
	
}
