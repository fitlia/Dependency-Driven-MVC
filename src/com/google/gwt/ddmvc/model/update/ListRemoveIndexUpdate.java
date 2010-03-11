package com.google.gwt.ddmvc.model.update;

import java.util.List;

/**
 * Update to delete a single element from a list by its index.
 * Note - if the index does not exist or the list has not been initialized, an
 * exception will be thrown.
 * Assumes model value is a list.
 * 
 * @author Kevin Dolan
 */
public class ListRemoveIndexUpdate extends ModelUpdate {
	
	/**
	 * @param target
	 * @param data   the index to remove
	 */
	public ListRemoveIndexUpdate(String target, int data) {
		super(target, data);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object performUpdate(Object value) {
		List<Object> list = (List<Object>) value;
			
		list.remove((int) (Integer) data);
		
		return list;
	}
	
}