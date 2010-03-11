package com.google.gwt.ddmvc.model.update;

import java.util.Collection;
import java.util.List;

/**
 * Update to prepend a collection of elements to a list.  The elements will be 
 * prepended in the order they are received, so that model.get(0) will be
 * equal to the first element returned by the iterator of the data collection.
 * Assumes that the target model is a list.
 * Assumes that the data is a list of objects that can go into the list.
 * 
 * If the target model is null, a new list is created and used.
 * By default, ArrayList is used, but if the second constructor is used,
 * LinkedList can be specified.
 * 
 * @author Kevin Dolan
 */
public class ListPrependAllUpdate extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target the collection of objects to prepend
	 * @param data   must be a list of objects
	 */
	public ListPrependAllUpdate(String target, 
			Collection<? extends Object> data) {
		
		super(target, data);
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param data   		the collection of objects to prepend
	 * @param useLinkedList true if you want to use linked-list, if new list
	 * 						creation is necessary
	 */
	public ListPrependAllUpdate(String target, Collection<? extends Object> data, 
			boolean useLinkedList) {
		super(target, data);
		this.useLinkedList = useLinkedList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object performUpdate(Object value) {
		List<Object> list = 
			ListAppendUpdate.listInitHelper(value, useLinkedList);
		
		Object[] dataArray = ((Collection<? extends Object>) data).toArray();
		
		for(int i = dataArray.length - 1; i >= 0; i--)
			list.add(0, dataArray[i]);
		
		return list;
	}
}