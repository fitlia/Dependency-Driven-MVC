package com.google.gwt.ddmvc.model;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.ddmvc.CanHaveObservers;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.Observer;

/**
 * Model objects hold an element of a given type, and maintain a set of entities which
 * depend on it
 * @author Kevin Dolan
 */
public class Model implements CanHaveObservers {

	private Object data;
	private HashSet<Observer> observers;
	
	/**
	 * Instantiate a new blank model
	 */
	public Model() {
		this.data = null;
		this.observers = new HashSet<Observer>();
	}
	
	/**
	 * Instantiate a new model with associated data
	 * @param data the data to store
	 */
	public Model(Object data) {
		this.data = data;
		this.observers = new HashSet<Observer>();	
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
	 * Set the associated data, notify dependencies of the change
	 * @param data the data to set
	 */
	public void set(Object data) {
		this.data = data;
		update();
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
	 * Notify the dependencies of a data change
	 */
	public void update() {
		DDMVC.addNotify(observers);
	}
	
	/**
	 * @return the set of dependencies
	 */
	public Set<Observer> getObservers() {
		return observers;
	}
	
}
