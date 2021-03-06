package com.google.gwt.ddmvc.model.update.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * Update to append a single element to a list. 
 * Assumes that the target model is a list.
 * Assumes that the data is an object that can go into the list.
 * 
 * If the target model is null, a new list is created and used.
 * By default, ArrayList is used, but if the second constructor is used,
 * LinkedList can be specified.
 * 
 * @author Kevin Dolan
 */
public class Append extends ModelUpdate {
	
	private boolean useLinkedList;
	
	/**
	 * The default Cascade field, used for comparison
	 */
	public static final Append DEFAULT = 
		new Append("", null);
	
	private Object data;
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data - the item to be appended to the list
	 */
	public Append(String target, Object data) {
		super(target);
		this.data = data;
		useLinkedList = false;
	}
	
	/**
	 * NOTE - Assumes you want ArrayList if new list creation is necessary
	 * @param target
	 * @param data - the item to be appended to the list
	 */
	public Append(Path<?,?,?> target, Object data) {
		super(target);
		this.data = data;
		useLinkedList = false;
	}

	/**
	 * Specify what type of list to use
	 * @param target 
	 * @param data - the item to be appended to the list
	 * @param useLinkedList - true if you want to use linked list, if new list
	 * 				creation is necessary
	 */
	public Append(String target, Object data, boolean useLinkedList) {
		super(target);
		this.data = data;
		this.useLinkedList = useLinkedList;
	}
	
	/**
	 * Specify what type of list to use
	 * @param target 
	 * @param data - the item to be appended to the list
	 * @param useLinkedList - true if you want to use linked list, if new list
	 * 				creation is necessary
	 */
	public Append(Path<?,?,?> target, Object data, boolean useLinkedList) {
		super(target);
		this.data = data;
		this.useLinkedList = useLinkedList;
	}
	
	@Override
	protected Object performUpdate(Object value) {
		List<Object> list = listInitHelper(value, useLinkedList);
		list.add(data);
		return list;
	}
	
	/**
	 * Casts value to a List<Object>. If it is found that value
	 * is null, a new list will be created and returned.
	 * @param useLinkedList - true if you want to use LinkedList
	 * @return a new list
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> listInitHelper(Object value,
			boolean useLinkedList) {
		
		List<Object> list = (List<Object>) value;
		if(list == null) {
			if(useLinkedList)
				list = new LinkedList<Object>();
			else
				list = new ArrayList<Object>();
		}
		return list;
	}
}