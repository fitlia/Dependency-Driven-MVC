package com.google.gwt.ddmvc.model.update.list;

import java.util.Collection;
import java.util.List;

import com.google.gwt.ddmvc.model.update.ModelUpdate;

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
public class PrependAll extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * The default PrependAll field, used for comparison
	 */
	public static final PrependAll DEFAULT =
		new PrependAll(null, null);
	
	private Collection<? extends Object> collection;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param collection - the collection of objects to prepend
	 */
	public PrependAll(String target, 
			Collection<? extends Object> collection) {
		
		super(target);
		this.collection = collection;
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param collection - the collection of objects to prepend
	 * @param useLinkedList - true if you want to use linked-list, if new list
	 * 				creation is necessary
	 */
	public PrependAll(String target, Collection<? extends Object> collection, 
			boolean useLinkedList) {
		
		super(target);
		this.collection = collection;
		this.useLinkedList = useLinkedList;
	}
	
	@Override
	protected Object performUpdate(Object value) {
		List<Object> list = 
			Append.listInitHelper(value, useLinkedList);
		
		Object[] dataArray = collection.toArray();
		
		for(int i = dataArray.length - 1; i >= 0; i--)
			list.add(0, dataArray[i]);
		
		return list;
	}
}