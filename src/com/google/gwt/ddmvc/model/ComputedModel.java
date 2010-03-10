package com.google.gwt.ddmvc.model;

import com.google.gwt.ddmvc.Observer;

/**
 * Represents a value which depends on some other model(s).
 * @author Kevin Dolan
 *
 * @param <Type> the type of data returned by this model
 */
public abstract class ComputedModel extends Model implements Observer {
	
	private boolean inSync;
	private Object cache;
	
	public ComputedModel() {
		inSync = false;
		cache = null;
	}
	
	/**
	 * @return the computed value of this model
	 */
	public abstract Object computeValue();
	
	/**
	 * If necessary, perform any initial dependency-binding or processing
	 */
	public void init() {}
	
	/**
	 * Set if the value should be cached when computed
	 * @return true by default
	 */
	public boolean isCacheable() {
		return true;
	}
	
	/**
	 * Set if the value should be computed immediately when its dependencies change,
	 * This property is ignored if isCacheable() returns false
	 * @return true by default
	 */
	public boolean isImmediate() {
		return true;
	}
	
	@Override
	public Object get() {
		if(inSync)
			return cache;
		
		if(isCacheable()) {
			cache = computeValue();
			inSync = true;
			return cache;
		}
		
		return computeValue();
	}
	
	@Override
	public void modelChanged() {
		inSync = false;
		if(isCacheable() && isImmediate()) {
			cache = computeValue();
			inSync = true;
		}
	}
	
	@Override
	public boolean canHaveObservers() {
		return true;
	}
}
