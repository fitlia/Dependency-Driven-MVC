package com.google.gwt.ddmvc.model.update;

import com.google.gwt.ddmvc.model.path.DefaultPath;

/**
 * Update to initialize or reset a model's associated value 
 * @author Kevin Dolan
 */
public class SetValue extends ModelUpdate {
	
	/**
	 * The default SetValue field, used for comparison
	 */
	public static final SetValue DEFAULT = new SetValue("", null);
	
	private Object data;
	
	/**
	 * @param target
	 * @param data
	 */
	public SetValue(String target, Object data) {
		super(target);
		this.data = data;
	}
	
	/**
	 * @param target
	 * @param data
	 */
	public SetValue(DefaultPath<?,?,?> target, Object data) {
		super(target);
		this.data = data;
	}

	@Override
	protected Object performUpdate(Object value) {
		return data;
	}
}