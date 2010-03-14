package com.google.gwt.ddmvc.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.multimap.MultiHashMap;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.Observer;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * A View is the direct connection with the end-user.  It creates the actual UI
 * widget and dispatches relevant user-generated events 
 * 
 * @author Kevin Dolan
 */
public abstract class View implements Observer {
	
	private MultiHashMap<String, Class<? extends ModelUpdate>> subscriptions;
	
	/**
	 * Instantiate a new View with the given executive object
	 * @param executive the executive DDMVC object to reference
	 */
	public View() {
		subscriptions = new MultiHashMap<String, Class<? extends ModelUpdate>>();
		initialize();
		render();
	}
	
	@Override
	public void modelChanged(Collection<ModelUpdate> updates) {		
		boolean containsAll = true;
		
		for(ModelUpdate update : updates) {
			Class<? extends ModelUpdate> updateType = update.getClass();
			Collection<Class<? extends ModelUpdate>> subscriptionTypes = 
				subscriptions.get(update.getTarget());
			
			boolean contains = false;
			for(Class<? extends ModelUpdate> subscriptionType : subscriptionTypes)
				if(subscriptionType.isAssignableFrom(updateType)) {
					contains = true;
					break;
				}
			
			if(!contains) {
				containsAll = false;
				break;
			}
		}
		
		if(!containsAll)
			render();
		else
			for(ModelUpdate update : updates)
				respondToModelUpdate(update);
	}
	
	@Override
	public Set<Observer> getObservers() {
		return Collections.emptySet();
	}
	
	/**
	 * Add this view to a model's list of observers
	 * @param modelKey the key of the observer to observe
	 */
	public void observe(String modelKey) {
		DDMVC.subscribeToModel(modelKey, this);
	}
	
	/**
	 * Views will all have the same key, view, so that we can ensure nothing will
	 * ever be able to depend on them
	 */
	public String getKey() {
		return "view";
	}
	
	/**
	 * Override this method if you intend to implement any view rendering
	 * optimizations.  The updates are guaranteed to be passed to this method
	 * in the order they were applied to the model.
	 * @param update - the update to respond to.
	 */
	protected void respondToModelUpdate(ModelUpdate update) {}
	
	/**
	 * Set this view to respond to a particular type of model update, instead of
	 * calling render().  This means that whenever the view receives a collection
	 * of updates, if all the updates are subscribed updates, it will call the
	 * view's respondToModelUpdate(...) method for each update instead of render()
	 * Subscriptions take into consideration inheritance, so if for instance you
	 * just want to pay attention to any types of updates to a particular key, you
	 * could pass in ModelUpdate.class for cls.
	 * @param modelKey - the key for which a model update is subscribed
	 * @param cls - the update class to subscribe to.
	 */
	protected void subscribeToModelUpdate(String modelKey, 
			Class<? extends ModelUpdate> cls) {
		
		subscriptions.put(modelKey, cls);
	}
	
	/**
	 * Initialize the view.
	 * Note: take care to maximize the initialization of everything that will not
	 * change with the models.
	 * This will generally only be called by the constructor, followed by a call
	 * to render().
	 */
	protected abstract void initialize();
	
	/**
	 * Render the view.  Generally, this should involve setting the view to match
	 * the model, from scratch.  render() is called by the constructor after
	 * initialize, and also whenever the view receives an unhandled ModelUpdate.
	 */
	protected abstract void render();
	
	
}
