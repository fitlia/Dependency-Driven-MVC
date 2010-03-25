package com.google.gwt.ddmvc.model.update.list;

import java.util.List;

import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Update to prepend a single element to a list. 
 * Assumes that the target model is a list.
 * Assumes that the data is an object that can go into the list.
 * 
 * If the target model is null, a new list is created and used.
 * By default, ArrayList is used, but if the second constructor is used,
 * LinkedList can be specified.
 * 
 * If the target model is not a list, an exception will be returned.
 * 
 * @author Kevin Dolan
 */
public class Prepend extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * The default CaListPrependUpdate field, used for comparison
	 */
	public static final Prepend DEFAULT = 
		new Prepend("", null);
	
	private Object data;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data - the item to be prepended to the list
	 */
	public Prepend(String target, Object data) {	
		super(target);
		this.data = data;
		useLinkedList = false;
	}
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data - the item to be prepended to the list
	 */
	public Prepend(Path<?> target, Object data) {	
		super(target);
		this.data = data;
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param data - the item to be prepended to the list
	 * @param useLinkedList - true if you want to use linked-list, if new list
	 * 				creation is necessary
	 */
	protected Prepend(Path<?> target, Object data, boolean useLinkedList) {
		super(target);
		this.data = data;
		this.useLinkedList = useLinkedList;
	}
	
	/**
	 * Specify what type of list to use
	 * @param target
	 * @param data - the item to be prepended to the list
	 * @param useLinkedList - true if you want to use linked-list, if new list
	 * 				creation is necessary
	 */
	protected Prepend(String target, Object data, boolean useLinkedList) {
		super(target);
		this.data = data;
		this.useLinkedList = useLinkedList;
	}
	
	@Override
	public Object performUpdate(Object value) {
		List<Object> list = Append.listInitHelper(value, useLinkedList);
			
		list.add(0, data);		
		return list;
	}
}