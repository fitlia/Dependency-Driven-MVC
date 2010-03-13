package com.google.gwt.ddmvc.model.update.list;

/**
 * The ListFilter interface is used by the RemoveAllThatMatch and also
 * KeepAllThatMatch ModelUpdate to choose which elements are kept, and which
 * are removed.  
 * @author Kevin Dolan
 */
public interface ListFilter {

	/**
	 * Return true if this object matches the filter.
	 * Note that the index sent to this method represents the index before any
	 * removals took place, which would allow you to do something like remove all
	 * indexes below 5, without accidentally removing all indexes
	 * @param int - the index in the list (before any removals)
	 * @param o - the object to check
	 * @return true if the object matches.
	 */
	public boolean accept(int index, Object o);
	
}
