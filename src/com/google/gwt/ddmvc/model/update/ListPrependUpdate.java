package com.google.gwt.ddmvc.model.update;

import java.util.List;

/**
 * Update to prepend a single element to a list.
 * Assumes that the target model is a list.
 * Assumes that the data is an object that can go into the list.
 * 
 * If the target model is null, a new list is created and used.
 * By default, ArrayList is used, but if the second constructor is used,
 * LinkedList can be specified.
 * 
 * @author Kevin Dolan
 */
public class ListPrependUpdate extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data   the item to be prepended to the list
	 */
	public ListPrependUpdate(String target, Object data) {
		
		super(target, data);
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param data   the item to be prepended to the list
	 * @param useLinkedList true if you want to use linked-list, if new list
	 * 						creation is necessary
	 */
	public ListPrependUpdate(String target, Object data, 
			boolean useLinkedList) {
		
		super(target, data);
		this.useLinkedList = useLinkedList;
	}
	
	@Override
	public Object performUpdate(Object value) {
		List<Object> list = 
			ListAppendUpdate.listInitHelper(value, useLinkedList);
			
		list.add(0, data);		
		return list;
	}
}