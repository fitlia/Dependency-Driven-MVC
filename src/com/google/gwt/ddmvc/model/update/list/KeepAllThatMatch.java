package com.google.gwt.ddmvc.model.update.list;

/**
 * Update to filter out all elements from a list not matching a certain filter
 * criterion.
 * The constructor accepts a ListFilter object, which could be implemented as
 * a class of its own, or simply created as an anonymous class.
 * 
 * @author Kevin Dolan
 */
public class KeepAllThatMatch extends RemoveAllThatMatch {
	
	/**
	 * The default KeepAllThatMatch field, used for comparison
	 */
	public static final KeepAllThatMatch DEFAULT = 
		new KeepAllThatMatch(null, null);
	
	/**
	 * @param target
	 * @param filter - the filter that will determine which elements are removed
	 */
	public KeepAllThatMatch(String target, ListFilter filter) {
		super(target, filter);
		this.removeMatches = false;
	}
	
}