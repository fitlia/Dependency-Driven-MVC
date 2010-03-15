package com.google.gwt.ddmvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.multimap.MultiHashListMap;
import org.multimap.MultiHashMap;
import org.multimap.MultiMap;
import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.controller.ServerRequest;
import com.google.gwt.ddmvc.event.AppEvent;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.Observer;
import com.google.gwt.ddmvc.model.update.Cascade;
import com.google.gwt.ddmvc.model.update.ExceptionComputed;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The DDMVC object is the top-level object for managing the data and run-loop 
 * execution. 
 * It is a static singleton.
 * 
 * @author Kevin Dolan
 */
public class DDMVC {

	private static MultiMap<Observer, ModelUpdate> pendingNotifies;
	private static List<AppEvent> pendingEvents;
	private static MultiMap<String, Controller> subscriptions;
	private static Model dataRoot;
	
	//Static initialization
	static { init(); }
	
	/**
	 * Initialize all the DDMVC components
	 */
	private static void init() {
		dataRoot = new Model();
		pendingNotifies = new MultiHashListMap<Observer, ModelUpdate>();
		pendingEvents = new ArrayList<AppEvent>();
		subscriptions = new MultiHashMap<String, Controller>();
	}
	
	/**
	 * Get the default data-store instance.
	 * 
	 * The data-store is just a model that has a static reference here in
	 * DDMVC, so that it can remain consistent throughout your application.
	 * 
	 * Generally, there is no need to have any other rooted models, though
	 * there's nothing stopping you from doing that.  It just won't behave
	 * as expected in all circumstanced.  Really, just don't do it!
	 * 
	 * @return the data root.
	 */
	public static Model getDataRoot() {
		return dataRoot;
	}
	
	/**
	 * Process a list of ModelUpdate objects
	 * @param updates - the updates to be applied
	 */
	public static void handleUpdates(List<ModelUpdate> updates) {
		for(ModelUpdate update : updates)
			dataRoot.handleUpdate(update);
	}
	
	/**
	 * Add an observer to be notified at the next run loop
	 * @param observer - the observer to be notified
	 * @param update - the update that caused this notification
	 */
	public static void addNotify(Observer observer, ModelUpdate update) {
		pendingNotifies.put(observer, update);
	}
	
	/**
	 * Add a set of observers to be notified at the next run loop
	 * @param observers - the set of observers
	 * @param update - the update that caused this notification
	 */
	public static void addNotify(Set<Observer> observers, ModelUpdate update) {
		for(Observer observer : observers)
			addNotify(observer, update);
	}
	
	/**
	 * Notify a particular controller whenever a particular type of event is
	 * fired.
	 * @param eventType - the class of the event to listen to
	 * @param controller - the controller to notify when the event is fired.
	 */
	public static void subscribeToEvent(Class<? extends AppEvent> eventType, 
			Controller controller) {
		
		subscriptions.put(eventType.getName(), controller);
	}
	
	/**
	 * Fire an event to any subscribed controllers, on the next run-loop.
	 * @param event - the event to fire
	 */
	public static void fireEvent(AppEvent event) {
		pendingEvents.add(event);
	}
	
	/**
	 * Execute all controllers subscribed to a particular event
	 * @param event - the event to respond to
	 * @param request - the list of all requests encountered
	 */
	private static List<ServerRequest> handleEvent(AppEvent event) {
		List<ServerRequest> requests = new ArrayList<ServerRequest>();
		Collection<Controller> controllers = 
			subscriptions.get(event.getClass().getName());
		for(Controller controller : controllers)
			requests.add(controller.respondToEvent(event));
		return requests;
	}
	
	/**
	 * Perform the run-loop, should generally not be called explicitly
	 * @return the list of all exceptions encountered during the run-loop
	 */
	public static List<RunLoopException> runLoop() {
		List<ServerRequest> requests = new ArrayList<ServerRequest>();
		while(pendingEvents.size() > 0) {
			List<AppEvent> events = pendingEvents;
			pendingEvents = new ArrayList<AppEvent>();
			
			for(AppEvent event : events)
				requests.addAll(handleEvent(event));
		}
		
		//TODO - send out the requests, please
		
		MultiHashMap<Observer, ModelUpdate> freeNotifies = 
			new MultiHashMap<Observer, ModelUpdate>();
		
		List<RunLoopException> exceptions = new ArrayList<RunLoopException>();
		
		//PendingNotifies will build up with notifications as we edit values.
		int iteration = 0;
		while(pendingNotifies.size() > 0) {
			//Extracts the notifications we will handle now, and reset pending
			Set<Map.Entry<Observer, Collection<ModelUpdate>>> notifies = 
				pendingNotifies.entrySet();
			pendingNotifies = new MultiHashListMap<Observer, ModelUpdate>();
			
			//Iterate through all of the current notifications
			for(Map.Entry<Observer, Collection<ModelUpdate>> entry : notifies) {
				Observer observer = entry.getKey();
				
				if(observer.getObservers().size() == 0)
					freeNotifies.put(entry.getKey(), entry.getValue());
				else {
					//Notify the model of a change
					try { 
						observer.modelChanged(entry.getValue());
						//Cascade the update to its dependents (next loop)
						Set<Observer> observers = observer.getObservers();
						addNotify(observers, new Cascade( observer.getPath()) );
					} catch(Exception e) {
						//Cascade the exception update to its dependents (next loop)
						Set<Observer> observers = observer.getObservers();
						addNotify(observers,
								new ExceptionComputed( observer.getPath(), e) );
						exceptions.add(new RunLoopException(e, observer, iteration));
					}
				}
			}
			iteration++;
		}
		
		//Now just tie up the loose ends!
		for(Map.Entry<Observer, Collection<ModelUpdate>> free 
				: freeNotifies.entrySet()) {
			
			try {
				free.getKey().modelChanged(free.getValue()); 
			} catch(Exception e) {
				exceptions.add(new RunLoopException(e, free.getKey(), iteration));
			}
		}
	
		return exceptions;
	}
	
	/**
	 * Reset the state of the DDMVC to initialization
	 */
	public static void reset() {
		init();
	}
}
