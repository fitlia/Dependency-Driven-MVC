package com.google.gwt.ddmvc.model.update.list;

import java.util.Collection;
import java.util.List;

import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

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
public class AppendAll extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * The default AppendAll field, used for comparison
	 */
	public static final AppendAll DEFAULT =
		new AppendAll("", null);
	
	private Collection<? extends Object> collection;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param collection - the collection of objects to append
	 */
	public AppendAll(String target, 
			Collection<? extends Object> collection) {
		
		super(target);
		this.collection = collection;
		useLinkedList = false;
	}
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param collection - the collection of objects to append
	 */
	public AppendAll(Path<?,?,?> target, 
			Collection<? extends Object> collection) {
		
		super(target);
		this.collection = collection;
		useLinkedList = false;
	}
	
	/**
	 * Specify what type of list to use
	 * @param target
	 * @param collection - the collection of objects to append
	 * @param useLinkedList - true if you want to use linked-list, if new list
	 * 						creation is necessary
	 */
	public AppendAll(String target, Collection<? extends Object> collection, 
			boolean useLinkedList) {
		super(target);
		this.collection = collection;
		this.useLinkedList = useLinkedList;
	}

	/**
	 * Specify what type of list to use
	 * @param target
	 * @param collection - the collection of objects to append
	 * @param useLinkedList - true if you want to use linked-list, if new list
	 * 						creation is necessary
	 */
	public AppendAll(Path<?,?,?> target, Collection<? extends Object> collection, 
			boolean useLinkedList) {
		super(target);
		this.collection = collection;
		this.useLinkedList = useLinkedList;
	}
	
	@Override
	protected Object performUpdate(Object value) {
		List<Object> list = 
			Append.listInitHelper(value, useLinkedList);
		
		list.addAll(collection);
		return list;
	}
}