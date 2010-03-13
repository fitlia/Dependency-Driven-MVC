package com.google.gwt.ddmvc.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.update.ExceptionEncountered;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.KeepAllThatMatch;
import com.google.gwt.ddmvc.model.update.list.ListFilter;
import com.google.gwt.ddmvc.model.update.list.RemoveAllEqualTo;
import com.google.gwt.ddmvc.model.update.list.RemoveAllThatMatch;
import com.google.gwt.ddmvc.model.update.list.RemoveIndex;

public class ModelUpdateListFiltering {

	private List<Integer> testList;
	
	@Before
	public void setUp() {
		DDMVC.reset();
		DDMVC.setValue("frillo", "Hodgepodge");
		DDMVC.setValue("something", "text");
		
		testList = new ArrayList<Integer>();
		testList.add(5);
		testList.add(10);
		testList.add(15);
		testList.add(20);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveIndexUpdate() {		
		DDMVC.setValue("nam", testList);
		
		RemoveIndex update = new RemoveIndex("nam", 1);
		DDMVC.applyUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 3);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(15));
		assertTrue(list.get(2).equals(20));
		assertTrue(update.getObjectRemoved().equals(10));
		
		RemoveIndex update2 = new RemoveIndex("nam", 5);
		DDMVC.applyUpdate(update2);
		assertTrue(update2.getException() != null);
		ExceptionEncountered ee = (ExceptionEncountered) DDMVC.getValue("nam");
		assertTrue(IndexOutOfBoundsException.class
				.equals(ee.getException().getClass()));
		assertTrue(ee.getCause() == update2);
		
	}
	
	@Test
	public void exceptionHandling() {
		Append update3 = new Append("frillo", 5);
		DDMVC.applyUpdate(update3);
		assertTrue(update3.getException() != null);
		ExceptionEncountered ee = (ExceptionEncountered) DDMVC.getValue("frillo");
		assertTrue(ClassCastException.class
				.equals(ee.getException().getClass()));
		assertTrue(ee.getCause() == update3);
		
		RemoveIndex update4 = new RemoveIndex("f", 5);
		DDMVC.applyUpdate(update4);
		assertTrue(update4.getException() != null);
		ee = (ExceptionEncountered) DDMVC.getValue("f");
		assertTrue(NullPointerException.class
				.equals(ee.getException().getClass()));
		assertTrue(ee.getCause() == update4);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveAllThatMatchUpdate() {
		testList.add(25);
		DDMVC.setValue("nam", testList);
		
		DDMVC.applyUpdate(new RemoveAllThatMatch("nam", 
				new ListFilter() {
					public boolean accept(int index, Object o) {
						return (Integer) o % 2 == 0;
				} }));
			
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 3);
		assertTrue(list.get(0).equals(5));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void applyListKeepAllThatMatchUpdate() {		
		testList.add(25);
		DDMVC.setValue("nam", testList);
		
		DDMVC.applyUpdate(new KeepAllThatMatch("nam", 
				new ListFilter() {
					public boolean accept(int index, Object o) {
						return (Integer) o % 2 == 0;
				} }));
			
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveAllEqualToUpdate() {
		testList.add(10);
		testList.add(29);
		DDMVC.setValue("nam", testList);
		
		DDMVC.applyUpdate(new RemoveAllEqualTo("nam", 10));
			
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertFalse(list.get(1).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveAllThatMatchIndexUpdate() {
		DDMVC.setValue("nam", testList);
		
		DDMVC.applyUpdate(new RemoveAllThatMatch("nam", 
				new ListFilter() {
					public boolean accept(int index, Object o) {
						return index < 3;
				} }));
			
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(20));
	}
	
}
