package com.google.gwt.ddmvc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.update.CascadeUpdate;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.SetUpdate;

/**
 * The DDMVC object is the top-level object for managing the data and run-loop execution.
 * It is a static singleton.
 * 
 * @author Kevin Dolan
 */
public class DDMVC {

	private static HashMap<String, Model> dataStore;
	private static HashMap<Observer,List<ModelUpdate>> pendingNotifies;
	
	//Static initialization
	static { init(); }
	
	/**
	 * Initialize all the DDMVC components
	 */
	private static void init() {
		dataStore = new HashMap<String, Model>();
		pendingNotifies = new HashMap<Observer,List<ModelUpdate>>();
	}
	
	/**
	 * Add a controller bound to a name
	 * @param name		 the name of the controller
	 * @param controller the controller itself
	 */
	public static void addController(String name, Controller controller) {
		
	}
	
	/**
	 * Dispatch a call to a controller, with associated parameters
	 * @param name		 the name of the controller to execute
	 * @param parameters the parameters to pass the controller
	 */
	public static void dispatchController(String name, Object parameters) {
		
	}
	
	/**
	 * Determine whether or not a key exists in the data store.
	 * @param key the key to check
	 * @return	  true if the key has a value (may be null!)
	 */
	public static boolean hasModel(String key) {
		return dataStore.containsKey(key);
	}
	
	/**
	 * Get the value associated with a model associated with a key
	 * @param key the key to access
	 * @return	  the model's data object
	 */
	public static Object getValue(String key) {
		return getModel(key).get();
	}
	
	/**
	 * Get the value associated with a model associated with a key
	 * and add the observer to the list of dependencies
	 * @param key 	   the key to access
	 * @param observer the observer to add
	 * @return	  	   the model's data object
	 */
	public static Object getValue(String key, Observer observer) {
		return getModel(key).get(observer);
	}
	
	/**
	 * Return the Model object associated with a given key
	 * Note - if there is no model associated with the key, it will be created, but its value will be null
	 * @param key the key of the Model
	 * @return	  the Model associated with the Key
	 */
	public static Model getModel(String key) {
		Model model = dataStore.get(key);
		if(model == null) {
			model = new Model(key);
			dataStore.put(key, model);
		}
		return model;
	}
	
	/**
	 * Put an association between a key and a Model into the data store,
	 * and notify all dependencies on next run loop
	 * Note - if there is no model associated with the key, it will be created
	 * @param key	the key to reference this Model
	 * @param value the value to put into the model
	 */
	public static void setValue(String key, Object value) {
		getModel(key).set(value);
	}
	
	/**
	 * Put a model into the data store, and notify all dependencies on next run loop
	 * Note - this will make an attempt to merge the dependencies if applicable
	 * 		  also, it will reset the name of model to key
	 */
	public static void setModel(String key, Model model) {
		Model current = getModel(key);
		model.setKey(key);
		
		addNotify(current.getObservers(), new SetUpdate(key, model.get()));
		
		for(Observer observer : current.getObservers())
			model.addObserver(observer);
		
		dataStore.put(key, model);
	}
	
	/**
	 * Delete a value from the data store.
	 * @param key the key to remove
	 */
	public static void deleteModel(String key) {
		dataStore.remove(key);
	}
	
	/**
	 * Process an update to a model
	 * Note - a run-loop will not be enacted.
	 * @param update the update to apply.
	 */
	public static void processUpdate(ModelUpdate update) {
		Model model = getModel(update.getTarget());
		model.handleUpdate(update);	
	}
	
	/**
	 * Process a list of ModelUpdate's
	 * Note - a run-loop will be enacted upon completion
	 * @param updateList the list of updates to apply
	 */
	public static void processUpdates(List<ModelUpdate> updateList) {
		for(ModelUpdate update : updateList)
			processUpdate(update);
		runLoop();
	}
	
	/**
	 * Add an observer to be notified at the next run loop
	 * @param observer the observer to be notified
	 * @param update   the update that caused this notification
	 */
	public static void addNotify(Observer observer, ModelUpdate update) {
		List<ModelUpdate> updateList = pendingNotifies.get(observer);
		if(updateList == null) {
			updateList = new ArrayList<ModelUpdate>();
			pendingNotifies.put(observer, updateList);
		}
		updateList.add(update);
	}
	
	/**
	 * Add a set of observers to be notified at the next run loop
	 * @param observers the set of observers
	 * @param update    the update that caused this notification
	 */
	public static void addNotify(Set<Observer> observers, ModelUpdate update) {
		for(Observer observer : observers)
			addNotify(observer, update);
	}
	
	/**
	 * Perform the run-loop, should not be called explicitly unless you make changes
	 * to models outside of a controller, and want the changes to be reflected immediately
	 */
	public static void runLoop() {
		Set<Observer> freeNotifies = new HashSet<Observer>();
		
		while(pendingNotifies.size() > 0) {
			Set<Map.Entry<Observer,List<ModelUpdate>>> notifies = pendingNotifies.entrySet();
			pendingNotifies = new HashMap<Observer,List<ModelUpdate>>();
			
			for(Map.Entry<Observer,List<ModelUpdate>> entry : notifies) {
				Observer observer = entry.getKey();
				
				if(!observer.canHaveObservers())
					freeNotifies.add(observer);
				
				else if(((CanHaveObservers) observer).getObservers().size() == 0)
					freeNotifies.add(observer);
				
				else {
					observer.modelChanged(entry.getValue());
					Set<Observer> observers = ((CanHaveObservers)observer).getObservers();
					addNotify(observers, new CascadeUpdate());
				}
			}
		}
		
		for(Observer observer : freeNotifies)
			observer.modelChanged();
	}
	
	/**
	 * Reset the state of the DDMVC to initialization
	 */
	public static void reset() {
		init();
	}
}
