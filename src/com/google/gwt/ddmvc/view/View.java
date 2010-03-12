package com.google.gwt.ddmvc.view;

import java.util.Set;
import com.google.gwt.ddmvc.Observer;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.user.client.ui.Widget;

/**
 * A View is the direct connection with the end-user.  It creates the actual UI
 * widget and dispatches relevant user-generated events 
 * 
 * @author Kevin Dolan
 */
public abstract class View implements Observer {
	
	/**
	 * Instantiate a new View with the given executive object
	 * @param executive the executive DDMVC object to reference
	 */
	public View() {
		initialize();
		render(null);
	}
	
	/**
	 * @return this View's widget to be displayed
	 */
	public abstract Widget getWidget();
	
	@Override
	public void modelChanged(Set<ModelUpdate> updates) {
		render(updates);
	}
	
	@Override
	public boolean isModel() {
		return false;
	}
	
	/**
	 * Initialize the top-level widget.
	 * Note: take care to maximize the rendering of everything that will not
	 * change, but this method should usually make a call to render to render
	 * anything that will.
	 */
	public abstract void initialize();
	
	/**
	 * Render the already instantiated components.  Should not change the
	 * Widget reference.
	 * Also, may receive a list of updates, which can help for optimization.
	 * @param updates - the list of updates which caused this render to fire,
	 * 				may be null
	 */
	public abstract void render(Set<ModelUpdate> updates);
	
	
}
