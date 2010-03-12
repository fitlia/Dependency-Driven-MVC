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
	 * @param o - the object to check
	 * @return true if the object matches.
	 */
	public boolean accept(Object o);
	
}
