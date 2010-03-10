package com.google.gwt.ddmvc;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ModelUpdate;

/**
 * The DDMVC object is the top-level object for managing the data and run-loop execution.
 * It is a static singleton.
 * 
 * @author Kevin Dolan
 */
public class DDMVC {

	private static HashMap<String, Model> dataStore;
	private static HashSet<Observer> pendingNotifies;
	
	//Static initialization
	static { init(); }
	
	/**
	 * Initialize all the DDMVC components
	 */
	public static void init() {
		dataStore = new HashMap<String, Model>();
		pendingNotifies = new HashSet<Observer>();
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
			model = new Model();
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
	 * Put a model into the datastore, and notify all dependencies on next run loop
	 * Note - this will make an attempt to merge the dependencies if applicable
	 */
	public static void setModel(String key, Model model) {
		Model current = getModel(key);
		addNotify(current.getObservers());
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
	 * Process a list of ModelUpdate's
	 * @param updateList the list of updates to apply
	 */
	@SuppressWarnings("unchecked")
	public static void processUpdates(List<ModelUpdate> updateList) {
		for(ModelUpdate update : updateList) {
			if(update.getUpdate() == ModelUpdate.UpdateType.SET) 
				setValue(update.getModelKey(), update.getData());
			else { 
				List<Object> list = (List<Object>) getValue(update.getModelKey());
				if(update.getUpdate() == ModelUpdate.UpdateType.LIST_ADD)
					list.add(update.getData());
				else if(update.getUpdate() == ModelUpdate.UpdateType.LIST_ADD_ALL)
					list.addAll((Collection<Object>) update.getData());
				else if(update.getUpdate() == ModelUpdate.UpdateType.LIST_REMOVE)
					while(list.contains(update.getData()))
						list.remove(update.getData());
				else if(update.getUpdate() == ModelUpdate.UpdateType.LIST_REMOVE_INDEX)
					list.remove((int) (Integer) update.getData());
				getModel(update.getModelKey()).update();
			}
		}
	}
	
	/**
	 * Add an observer to be notified at the next run loop
	 * @param observer the observer to be notified
	 */
	public static void addNotify(Observer observer) {
		pendingNotifies.add(observer);
	}
	
	/**
	 * Add a set of observers to be notified at the next run loop
	 * @param observers the set of observers
	 */
	public static void addNotify(Set<Observer> observers) {
		pendingNotifies.addAll(observers);
	}
	
	/**
	 * Perform the run-loop, should not be called explicitly unless you make changes
	 * to models outside of a controller, and want the changes to be reflected immediately
	 */
	public static void runLoop() {
		Set<Observer> freeNotifies = new HashSet<Observer>();
		
		while(pendingNotifies.size() > 0) {
			Set<Observer> notifies = pendingNotifies;
			pendingNotifies = new HashSet<Observer>();
			for(Observer observer : notifies) {
				if(!observer.canHaveObservers())
					freeNotifies.add(observer);
				else if(((CanHaveObservers) observer).getObservers().size() == 0)
					freeNotifies.add(observer);
				else {
					observer.modelChanged();
					pendingNotifies.addAll(((CanHaveObservers)observer).getObservers());
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
