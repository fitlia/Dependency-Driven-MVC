package com.google.gwt.ddmvc.model.update.list;

import java.util.List;

import com.google.gwt.ddmvc.model.path.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Update to delete all elements from a list matching a certain filter
 * criterion.
 * The constructor accepts a ListFilter object, which could be implemented as
 * a class of its own, or simply created as an anonymous class.
 * 
 * @author Kevin Dolan
 */
public class RemoveAllThatMatch extends ModelUpdate {
	
	protected Integer numRemoved;
	protected boolean removeMatches;
	
	/**
	 * The default RemoveAllThatMatch field, used for comparison
	 */
	public static final RemoveAllThatMatch DEFAULT = 
		new RemoveAllThatMatch("", null);
	
	protected ListFilter filter;
	
	/**
	 * @param target
	 * @param filter - the filter that will determine which elements are removed
	 */
	public RemoveAllThatMatch(String target, ListFilter filter) {
		super(target);
		removeMatches = true;
		this.filter = filter;
	}
	
	/**
	 * @param target
	 * @param filter - the filter that will determine which elements are removed
	 */
	public RemoveAllThatMatch(Path<?,?,?> target, ListFilter filter) {
		super(target);
		removeMatches = true;
		this.filter = filter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object performUpdate(Object value) {
		List<Object> list = (List<Object>) value;
		numRemoved = 0;

		//The original index, before deletes.
		int oi = 0;
		for(int i = 0; i < list.size(); i++) {
			if(removeMatches == filter.accept(oi, list.get(i))) {
				list.remove(i);
				numRemoved++;
				i--;
			}
			oi++;
		}
		
		return list;
	}
	
	/**
	 * If this update has been performed, this will return the number of items
	 * removed.  If this has not been performed yet, it will return null.
	 * @return the number of values removed from the list
	 */
	public Integer getNumRemoved() {
		return numRemoved;
	}
	
}