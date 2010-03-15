package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.controller.ServerRequest;
import com.google.gwt.ddmvc.controller.StandardController;
import com.google.gwt.ddmvc.controller.ValidationError;
import com.google.gwt.ddmvc.event.AppEvent;
import com.google.gwt.ddmvc.view.View;

public class StandardControllerTest {

	private class MyController extends StandardController {

		public int validationErrorCount = 0;
		public int executionCount = 0;
		public int event1Count = 0;
		public int event2Count = 0;
		public int event3Count = 0;
		public AppEvent lastEvent;
		
		public MyController() {
			subscribeToEvent(Event1.class);
			subscribeToEvent(Event2.class);
		}
		
		@Override
		protected List<ValidationError> validate(AppEvent event) {
			lastEvent = event;
			
			boolean isValid = (Boolean) DDMVC.getValue("isValid");
			if(isValid)
				return null;
			
			List<ValidationError> errors = new ArrayList<ValidationError>();
			errors.add(new NotValidError());
			return errors;
		}
		
		@Override
		protected void onValidationFailure(AppEvent eventList, 
				List<ValidationError> errors) {
			
			validationErrorCount++;
		}
		
		@Override
		protected ServerRequest execute(AppEvent event) {
			executionCount++;
			if(event.getClass().equals(Event1.class))
				event1Count++;
			else if(event.getClass().equals(Event2.class)) {
				event2Count++;
				DDMVC.setValue("someProperty", 5);
			}
			else if(event.getClass().equals(Event3.class))
				event3Count++;

			return null;
		}
		
		@Override
		protected void onRequestSuccess(AppEvent event) {
			
		}
		
		@Override
		protected void onRequestFailure(AppEvent event) {
			
		}
		
	}
	
	private class MyView extends View {

		public int renderCount;
		
		@Override
		protected void initialize() {
			observe("someProperty");
		}

		@Override
		protected void render() {
			renderCount++;
		}
		
		/**
		 * Simulate an event occurring.
		 * @param event - the event to simulate
		 */
		public void proxyEvent(AppEvent event) {
			fireEvent(event);
		}
		
	}
	
	private class Event1 extends AppEvent {}
	private class Event2 extends AppEvent {}
	private class Event3 extends AppEvent {}
	
	private class NotValidError extends ValidationError {}
	
	private MyController controller;
	private MyView view;
	
	@Before
	public void setUp() throws Exception {
		controller = new MyController();
		view = new MyView();
	}
	
	@Test
	public void basicEventFiring() {
		DDMVC.setValue("isValid", true);
		view.proxyEvent(new Event1());
		DDMVC.runLoop();
		
		assertTrue(controller.validationErrorCount == 0);
		assertTrue(controller.event1Count == 1);
		assertTrue(controller.event2Count == 0);
		assertTrue(controller.event3Count == 0);
		assertTrue(controller.executionCount == 1);
		assertTrue(controller.lastEvent.getSource() == view);
	}
	
	@Test
	public void unobserved() {
		view.proxyEvent(new Event3());
		DDMVC.runLoop();
		assertTrue(controller.executionCount == 0);
	}
	
	@Test
	public void multipleEvents() {
		DDMVC.setValue("isValid", true);
		view.proxyEvent(new Event1());
		view.proxyEvent(new Event2());
		DDMVC.runLoop();
		
		assertTrue(controller.validationErrorCount == 0);
		assertTrue(controller.event1Count == 1);
		assertTrue(controller.event2Count == 1);
		assertTrue(controller.event3Count == 0);
		assertTrue(controller.executionCount == 2);
		assertTrue(controller.lastEvent.getClass().equals(Event2.class));
	}
	
	@Test
	public void modelObservation() {
		DDMVC.setValue("isValid", true);
		view.proxyEvent(new Event2());
		DDMVC.runLoop();
		assertTrue(view.renderCount == 2);
	}
	
	@Test
	public void validationFailure() {
		DDMVC.setValue("isValid", false);
		view.proxyEvent(new Event1());
		DDMVC.runLoop();

		assertTrue(controller.validationErrorCount == 1);
		assertTrue(controller.event1Count == 0);
		assertTrue(controller.event2Count == 0);
		assertTrue(controller.event3Count == 0);
		assertTrue(controller.executionCount == 0);
		assertTrue(controller.lastEvent.getClass().equals(Event1.class));
	}
}
