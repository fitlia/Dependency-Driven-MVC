package com.google.gwt.ddmvc.model.update;

/**
 * Update to initialize or reset a model's associated value
 * @author Kevin Dolan
 */
public class SetUpdate extends ModelUpdate {
	
	/**
	 * @param target
	 * @param data
	 */
	public SetUpdate(String target, Object data) {
		super(target, data);
	}

	@Override
	public Object performUpdate(Object value) {
		return data;
	}
}