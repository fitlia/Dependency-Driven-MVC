package com.google.gwt.ddmvc.model;

import java.util.HashSet;
import java.util.Set;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.Observer;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.SetUpdate;
import com.google.gwt.ddmvc.model.update.UnknownUpdate;

/**
 * Model objects hold an element of a given type, and maintain a set of entities
 * which depend on it 
 * @author Kevin Dolan
 */
public class Model {

	private Object data;
	private HashSet<Observer> observers;
	private String key;

	/**
	 * Instantiate a new blank model, with no name
	 * For this to be useful, you should generally use the explicitly
	 * DDMVC.setModel(...) command with an unnamed model so that
	 * the name will be assigned...
	 */
	public Model() {
		this.data = null;
		this.observers = new HashSet<Observer>();
		this.key = "";
	}
	
	/**
	 * Instantiate a new blank model
	 * @param name the name of this model
	 */
	public Model(String name) {
		this.data = null;
		this.observers = new HashSet<Observer>();
		this.key = name;
	}
	
	/**
	 * @return this model's key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key associated with this model.
	 * Generally should not be called outside of DDMVC!
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Instantiate a new model with associated data
	 * @param data the data to store
	 */
	public Model(String name, Object data) {
		this.data = data;
		this.observers = new HashSet<Observer>();
		this.key = name;
	}
	
	/**
	 * Add an observer to the model's set of dependencies
	 * @param observer the observer to add
	 */
	public void addObserver(Observer observer) {
		observers.add(observer);
	}
	
	/**
	 * Remove an observer from the set of dependencies
	 * @param observer the observer to remove
	 */
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}
	
	/**
	 * Handle a ModelUpdate request, notify dependencies
	 * @param update the update request being processed
	 */
	public void handleUpdate(ModelUpdate update) {
		Object result = update.performUpdate(data);
		data = result;
		DDMVC.addNotify(observers, update);
	}
	
	/**
	 * Set the associated data, notify dependencies of the change
	 * @param data the data to set
	 */
	public void set(Object data) {
		handleUpdate(new SetUpdate(key, data));
	}
	
	/**
	 * Get the associated data
	 * @return the data
	 */
	public Object get() {
		return data;
	}
	
	/**
	 * Get the associated data, and add an observer to the list of dependencies
	 * @param observer the observer to add
	 * @return		   the data
	 */
	public Object get(Observer observer) {
		addObserver(observer);
		return get();
	}
	
	/**
	 * Notify the dependencies of a data change,
	 * where the update type is not known.  This
	 * causes the ModelUpdate UnknownUpdate to be
	 * passed along.
	 */
	public void update() {
		DDMVC.addNotify(observers, new UnknownUpdate(key));
	}
	
	/**
	 * @return the set of dependencies
	 */
	public Set<Observer> getObservers() {
		return observers;
	}
	
}
