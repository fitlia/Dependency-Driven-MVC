package com.google.gwt.ddmvc.model;

import java.util.List;

import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.view.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncResponse implements AsyncCallback<List<ModelUpdate>> {

	/**
	 * Instantiate a new response object waiting on a server response 
	 * @param controller the controller object to call on completion
	 * @param source	 the source view if applicable
	 */
	public AsyncResponse(Controller controller, View source) {
		
	}
		
	@Override
	public void onFailure(Throwable caught) {
		
	}

	@Override
	public void onSuccess(List<ModelUpdate> result) {
		
	}
	

}
