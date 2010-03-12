package com.google.gwt.ddmvc.model;

import java.util.Collection;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
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
	
	public ComputedModel(String key) {
		super(key);
		inSync = false;
		cache = null;
	}
	
	/**
	 * Perform the computation.
	 * Please note that the proper functioning of DependencyNotFoundException
	 * depends on ModelDoesNotExistException being thrown if a value is not
	 * found, so take care to use DDMVC.getValue() rather than .getModel().
	 * If you do use .getModel(), you should throw DependencyNotFoundException
	 * in the event of a missing dependency, if you want the best runtime
	 * feedback of errors.
	 * @param updates - the list of updates that caused this value to be computed,
	 * 				null if this was called from .get()
	 * @return the computed value of this model
	 */
	public abstract Object computeValue(Collection<ModelUpdate> updates);
	
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
	public Object get() {
		try {
			if(inSync)
				return cache;
			
			if(isCacheable()) {
				cache = computeValue(null);
				inSync = true;
				return cache;
			}
			
			return computeValue(null);
		}
		catch(ModelDoesNotExistException e) {
			throw new DependencyNotFoundException(e.getKey());
		}
	}
	
	@Override
	public void modelChanged(Collection<ModelUpdate> updates) {
		try {
			inSync = false;
			if(isCacheable() && isImmediate()) {
				cache = computeValue(updates);
				inSync = true;
			}
		}
		catch(ModelDoesNotExistException e) {
			throw new DependencyNotFoundException(e.getKey());
		}
	}
	
	@Override
	public boolean isModel() {
		return true;
	}
}
