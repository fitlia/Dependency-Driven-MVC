package com.google.gwt.ddmvc.model.update.list;

import java.util.List;

import com.google.gwt.ddmvc.model.path.DefaultPath;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Update to delete a single element from a list by its index. 
 * Note - if the index does not exist or the list has not been initialized, an
 * exception will be thrown.
 * 
 * @author Kevin Dolan
 */
public class RemoveIndex extends ModelUpdate {
	private Object objectRemoved;
	
	/**
	 * The default RemoveIndex field, used for comparison
	 */
	public static final RemoveIndex DEFAULT = 
		new RemoveIndex("", 0);
	
	private int index;
	
	/**
	 * @param target
	 * @param index - the index to remove
	 */
	public RemoveIndex(String target, int index) {
		super(target);
		this.index = index;
	}
	
	/**
	 * @param target
	 * @param index - the index to remove
	 */
	public RemoveIndex(DefaultPath<?,?,?> target, int index) {
		super(target);
		this.index = index;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object performUpdate(Object value) {
		List<Object> list = (List<Object>) value;
		
		if(index < list.size())
			objectRemoved = list.get(index);			
		
		list.remove(index);
		
		return list;
	}
	
	/**
	 * If this update has been performed, this will return the object
	 * removed.  If this has not been performed yet, it will return null.
	 * Note that one should call update.isComplete() to differentiate between
	 * being incomplete, and having removed a null value.
	 * @return the value removed from the list
	 */
	public Object getObjectRemoved() {
		return objectRemoved;
	}
	
}