package com.google.gwt.ddmvc.model;

import java.util.Collection;

import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Represents a value which depends on some other model(s). 
 * @author Kevin Dolan
 */
public abstract class ComputedModel extends Model implements Observer {
	
	private boolean inSync;
	private Object cache;
	
	public ComputedModel() {
		inSync = false;
		cache = null;
	}
	
	public ComputedModel(String key) {
		super(key);
		inSync = false;
		cache = null;
	}
	
	/**
	 * Perform the computation.
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
	 * Set if the value should be computed immediately when its dependencies
	 * change.
	 * This property is ignored if isCacheable() returns false
	 * @return true by default
	 */
	public boolean isImmediate() {
		return true;
	}
	
	@Override
	public Object myValue() {
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
	public void modelChanged(Collection<ModelUpdate> updates) {
		inSync = false;
		if(isCacheable() && isImmediate()) {
			cache = computeValue();
			inSync = true;
		}
	}
	
}
