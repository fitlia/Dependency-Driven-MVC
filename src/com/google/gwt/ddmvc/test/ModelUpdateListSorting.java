package com.google.gwt.ddmvc.test;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.update.list.Sort;

public class ModelUpdateListSorting {

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
	public void handleListSortUpdate() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(100);
		list.add(90);
		list.add(95);
		list.add(3);
		DDMVC.setValue("nam", list);
		
		DDMVC.handleUpdate(new Sort("nam"));
		list = (List<Integer>) DDMVC.getValue("nam");
				
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(3));
		assertTrue(list.get(1).equals(90));
		assertTrue(list.get(2).equals(95));
		assertTrue(list.get(3).equals(100));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void handleListSortComparatorUpdate() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(100);
		list.add(90);
		list.add(95);
		list.add(3);
		DDMVC.setValue("nam", list);
		
		Comparator<Integer> myComp = new Comparator<Integer>() {

			@Override
			public int compare(Integer a, Integer b) {
				return b.compareTo(a);
			}
			
		};
		
		DDMVC.handleUpdate(new Sort("nam", myComp));
		list = (List<Integer>) DDMVC.getValue("nam");
				
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(100));
		assertTrue(list.get(1).equals(95));
		assertTrue(list.get(2).equals(90));
		assertTrue(list.get(3).equals(3));
	}
	
}
