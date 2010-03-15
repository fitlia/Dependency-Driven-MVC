package com.google.gwt.ddmvc.model.update;

import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.Path;

/**
 * Update to initialize or reset a model's reference
 * @author Kevin Dolan
 */
public class SetModel extends ModelUpdate {
	
	/**
	 * The default SetModel field, used for comparison
	 */
	public static final SetModel DEFAULT = new SetModel("", null);
	
	private Model model;
	
	/**
	 * @param target
	 * @param model
	 */
	public SetModel(String target, Model model) {
		super(target);
		this.model = model;
	}
	
	/**
	 * @param target
	 * @param model
	 */
	public SetModel(Path target, Model model) {
		super(target);
		this.model = model;
	}

	@Override
	protected Object performUpdate(Object value) {
		return new SET_MODEL_TO(model);
	}
}