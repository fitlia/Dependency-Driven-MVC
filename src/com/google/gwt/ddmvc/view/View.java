package com.google.gwt.ddmvc.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.multimap.MultiHashMap;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.event.EventSource;
import com.google.gwt.ddmvc.model.Observer;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * A View is the direct connection with the end-user.  It creates the actual UI
 * widget and dispatches relevant user-generated events 
 * 
 * @author Kevin Dolan
 */
public abstract class View extends EventSource implements Observer {
	
	//Maps Path.toString() -> ModelUpdate.getClass().getName()
	private MultiHashMap<String, String> subscriptions;
	
	/**
	 * Instantiate a new View
	 */
	public View() {
		subscriptions = new MultiHashMap<String, String>();
		initialize();
		render();
	}
	
	@Override
	public void modelChanged(Collection<ModelUpdate> updates) {		
		boolean containsAll = true;
		
		for(ModelUpdate update : updates) {
			
			Collection<String> subscriptionTypes = 
				subscriptions.get(update.getTarget().toString());
			
			String updateType = update.getClass().getName();
			
			if(!(subscriptionTypes.contains(ModelUpdate.class.getName())
				|| subscriptionTypes.contains(updateType))) {

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
	 * Note - if the path ends with a field name, it will be added as
	 * a referential observer.  If the path ends with a $, it will be added
	 * as a value observer.  If the path ends with a * it will be added as
	 * a field observer.
	 * @param pathString
	 */
	protected void observe(String pathString) {
		observe(new Path(pathString));
	}
	
	/**
	 * Add this view to a model's list of observers
	 * Note - if the path ends with a field name, it will be added as
	 * a referential observer.  If the path ends with a $, it will be added
	 * as a value observer.  If the path ends with a * it will be added as
	 * a field observer.
	 * @param modelKey the key of the observer to observe
	 */
	protected void observe(Path path) {
		DDMVC.getDataRoot().addObserver(this, path);
	}
	
	public Path getPath() {
		return null;
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
	 * Subscriptions do not take into consideration inheritance, however, if you
	 * would like to subscribe to any change on a particular key, you can pass
	 * ModelUpdate.class instead.
	 * @param pathString - the path to observe
	 * @param cls - the update class to subscribe to.
	 */
	protected void subscribeToModelUpdate(String pathString, 
			Class<? extends ModelUpdate> cls) {
		
		subscribeToModelUpdate(new Path(pathString), cls);
	}
	
	/**
	 * Set this view to respond to a particular type of model update, instead of
	 * calling render().  This means that whenever the view receives a collection
	 * of updates, if all the updates are subscribed updates, it will call the
	 * view's respondToModelUpdate(...) method for each update instead of render()
	 * Subscriptions do not take into consideration inheritance, however, if you
	 * would like to subscribe to any change on a particular key, you can pass
	 * ModelUpdate.class instead.
	 * @param path - the path to observe
	 * @param cls - the update class to subscribe to.
	 */
	protected void subscribeToModelUpdate(Path path, 
			Class<? extends ModelUpdate> cls) {
		
		subscriptions.put(path.toString(), cls.getName());
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
