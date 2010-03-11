package com.google.gwt.ddmvc.model.update;

import java.util.Collection;
import java.util.List;

/**
 * Update to append a collection of elements to a list. 
 * Assumes that the target model is a list.
 * Assumes that the data is a list of objects that can go into the list.
 * 
 * If the target model is null, a new list is created and used.
 * By default, ArrayList is used, but if the second constructor is used,
 * LinkedList can be specified.
 * 
 * @author Kevin Dolan
 */
public class ListAppendAllUpdate extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data   the collection of objects to append
	 */
	public ListAppendAllUpdate(String target, 
			Collection<? extends Object> data) {
		
		super(target, data);
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param data   		the collection of objects to append
	 * @param useLinkedList true if you want to use linked-list, if new list
	 * 						creation is necessary
	 */
	public ListAppendAllUpdate(String target, Collection<? extends Object> data, 
			boolean useLinkedList) {
		super(target, data);
		this.useLinkedList = useLinkedList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object performUpdate(Object value) {
		List<Object> list = 
			ListAppendUpdate.listInitHelper(value, useLinkedList);
		
		list.addAll((Collection<? extends Object>) data);
		return list;
	}
}