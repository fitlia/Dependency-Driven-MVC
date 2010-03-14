package com.google.gwt.ddmvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.multimap.MultiHashListMap;
import org.multimap.MultiHashMap;
import org.multimap.MultiMap;
import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.model.ComputedModel;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ModelDoesNotExistException;
import com.google.gwt.ddmvc.model.update.Cascade;
import com.google.gwt.ddmvc.model.update.ExceptionComputed;
import com.google.gwt.ddmvc.model.update.ModelDeleted;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.SetValue;
import com.google.gwt.ddmvc.model.update.UnknownUpdate;

/**
 * The DDMVC object is the top-level object for managing the data and run-loop 
 * execution. 
 * It is a static singleton.
 * 
 * @author Kevin Dolan
 */
public class DDMVC {

	private static HashMap<String, Model> dataStore;
	private static MultiMap<Observer, ModelUpdate> pendingNotifies;
	
	//Static initialization
	static { init(); }
	
	private static class DefaultModel extends Model {
		public DefaultModel(String key) {
			super(key);
		}
	}
	
	/**
	 * Initialize all the DDMVC components
	 */
	private static void init() {
		dataStore = new HashMap<String, Model>();
		pendingNotifies = new MultiHashListMap<Observer, ModelUpdate>();
	}
	
	/**
	 * Add a controller bound to a name
	 * @param name - the name of the controller
	 * @param controller - the controller itself
	 */
	public static void addController(String name, Controller controller) {
		
	}
	
	/**
	 * Dispatch a call to a controller, with associated parameters
	 * @param name - the name of the controller to execute
	 * @param parameters - the parameters to pass the controller
	 */
	public static void dispatchController(String name, Object parameters) {
		
	}
	
	/**
	 * Determine whether or not a key exists in the data store.
	 * @param key - the key to check
	 * @return true if the key has a value (may be null!)
	 */
	public static boolean hasModel(String key) {
		return dataStore.containsKey(key);
	}
	
	/**
	 * Get the value associated with a model associated with a key.
	 * If the key does not exist, ModelDoesNotExistException will be thrown.
	 * @param key - the key to access
	 * @return the model's data object
	 */
	public static Object getValue(String key) {
		if(!hasModel(key))
			throw new ModelDoesNotExistException(key);
		return getModel(key).get();
	}
	
	/**
	 * Get the value associated with a model associated with a key
	 * and add the observer to the list of dependencies.
	 * If the key does not exist, ModelDoesNotExistException will be thrown.
	 * @param key - the key to access
	 * @param observer - the observer to add
	 * @return the model's data object
	 */
	public static Object getValue(String key, Observer observer) {
		if(!hasModel(key))
			throw new ModelDoesNotExistException(key);
		return getModel(key).get(observer);
	}
	
	/**
	 * Return the Model object associated with a given key
	 * Note - if there is no model associated with the key, it will be created,
	 * but its value will be null.
	 * @param key - the key of the Model
	 * @return the Model associated with the Key
	 */
	private static Model getModel(String key) {
		//Stop people from being able to use the view key
		if(key.equals("view"))
			throw new ReservedWordException("view");
		
		Model model = dataStore.get(key);
		if(model == null) {
			model = new DefaultModel(key);
			dataStore.put(key, model);
		}
		return model;
	}
	
	/**
	 * Subscribe an observer to a model's changes
	 * @param key - the model to subscribe to
	 * @param observer - the observer to notify when changes occur
	 */
	public static void subscribeToModel(String key, Observer observer) {
		getModel(key).addObserver(observer);
	}
	
	/**
	 * Put an association between a key and a Model into the data store,
	 * and notify all dependencies on next run loop
	 * Note - if there is no model associated with the key, it will be created
	 * @param key - the key to reference this Model
	 * @param value - the value to put into the model
	 */
	public static void setValue(String key, Object value) {
		getModel(key).set(value);
	}
	
	/**
	 * Put a computed model into the data store, and notify all dependencies
	 * on the, next run loop, if applicable
	 * Note - this will make an attempt to merge the dependencies if applicable
	 * also, it will reset the name of model to key
	 */
	public static void setComputedModel(String key, ComputedModel model) {
		Model current = getModel(key);
		model.setKey(key);
		
		addNotify(current.getObservers(), new SetValue(key, model.get()));
		
		for(Observer observer : current.getObservers())
			model.addObserver(observer);
		
		dataStore.put(key, model);
	}
	
	/**
	 * Notify a model's observers of an UnknownUpdate at the next run-loop
	 * This is useful for making changes not covered by any ModelUpdate and when
	 * re-rendering would be ideal.
	 * @param key - the key of the model to update
	 */
	public static void update(String key) {
		Model model = getModel(key);
		addNotify(model.getObservers(), new UnknownUpdate(key));
	}
	
	/**
	 * Delete a value from the data store, and notify all dependencies on the
	 * next run loop
	 * @param key - the key to removeMatches
	 */
	public static void deleteModel(String key) {
		if(hasModel(key)) {
			Model model = getModel(key);
			Set<Observer> observers = model.getObservers();
			addNotify(observers, new ModelDeleted(key));
		}
		dataStore.remove(key);
	}
	
	/**
	 * Apply a single update to a model
	 * Note - a run-loop will not be enacted.
	 * @param update - the update to apply.
	 */
	public static void applyUpdate(ModelUpdate update) {
		Model model = getModel(update.getTarget());
		model.handleUpdate(update);	
	}
	
	/**
	 * Apply a list of ModelUpdate's
	 * Note - a run-loop will be enacted upon completion
	 * @param updateList - the list of updates to apply
	 */
	public static void applyUpdates(List<ModelUpdate> updateList) {
		for(ModelUpdate update : updateList)
			applyUpdate(update);
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
	 * Perform the run-loop, should generally not be called explicitly
	 * @return the list of all exceptions encountered during the run-loop
	 */
	public static List<RunLoopException> runLoop() {
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
						addNotify(observers, new Cascade( observer.getKey()) );
					}
					catch(Exception e) {
						//Cascade the exception update to its dependents (next loop)
						Set<Observer> observers = observer.getObservers();
						addNotify(observers, new ExceptionComputed( observer.getKey(), e) );
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
			}
			catch(Exception e) {
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
