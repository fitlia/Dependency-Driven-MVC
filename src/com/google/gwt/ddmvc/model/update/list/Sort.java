package com.google.gwt.ddmvc.model.update.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Update to sort a list.
 * The default constructor utilizes the object's comparTo() methods, so it
 * better be the case that they implement Comparable.
 * Optionally, you may specify a comparator.
 * 
 * @author Kevin Dolan
 */
public class Sort extends ModelUpdate {
	
	/**
	 * The default Sort field, used for comparison
	 */
	public static final Sort DEFAULT = new Sort("");
	
	private Comparator<Object> comparator;
	
	/**
	 * Sort the list by the object's .compateTo(...) method.
	 * @param target
	 */
	public Sort(String target) {
		super(target);
	}
	
	/**
	 * Sort the list by the object's .compateTo(...) method.
	 * @param target
	 */
	public Sort(Path<?> target) {
		super(target);
	}
	
	/**
	 * Sort the list with the specified comparator
	 * @param target
	 * @param comparator - the comparator object to use to sort the list
	 */
	@SuppressWarnings("unchecked")
	public Sort(String target, Comparator<?> comparator) {
		super(target);
		this.comparator = (Comparator<Object>) comparator;
	}
	
	/**
	 * Sort the list with the specified comparator
	 * @param target
	 * @param comparator - the comparator object to use to sort the list
	 */
	@SuppressWarnings("unchecked")
	public Sort(Path target, Comparator<?> comparator) {
		super(target);
		this.comparator = (Comparator<Object>) comparator;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object performUpdate(Object value) {
		List<? extends Comparable> list = (List<? extends Comparable>) value;
		
		if(comparator == null)
			Collections.sort(list);
		else
			Collections.sort(list, comparator);
		
		return list;
	}
	
}