package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.*;
import com.google.gwt.ddmvc.model.update.*;

/**
 * Tests for DDMVC handling of ModelUpdates
 * 
 * @author Kevin Dolan
 */
public class DDMVCModelUpdate {
	
	@Before
	public void setUp() {
		DDMVC.reset();
		DDMVC.setValue("frillo", "Hodgepodge");
		DDMVC.setValue("something", "text");
	}
	
	@Test
	public void applySetUpdate() {
		SetUpdate update = new SetUpdate("frillo", "hodgepodge");
		DDMVC.processUpdate(update);
		assertTrue(DDMVC.getValue("frillo").equals("hodgepodge"));
		
		SetUpdate update2 = new SetUpdate("fresh", "bite");
		DDMVC.processUpdate(update2);
		assertTrue(DDMVC.getValue("fresh").equals("bite"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListAppendUpdate() {
		ListAppendUpdate update = new ListAppendUpdate("nam", 33);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		ListAppendUpdate update2 = new ListAppendUpdate("nam", 505);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(33));
		assertTrue(list.get(1).equals(505));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListAppendAllUpdate() {
		List<Integer> testList = new ArrayList<Integer>();
		testList.add(5);
		testList.add(10);
		
		ListAppendAllUpdate update = new ListAppendAllUpdate("nam", testList);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		ListAppendAllUpdate update2 = new ListAppendAllUpdate("nam", testList);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		assertTrue(list.get(2).equals(5));
		assertTrue(list.get(3).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListPrependUpdate() {
		ListPrependUpdate update = new ListPrependUpdate("nam", 33);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		ListPrependUpdate update2 = new ListPrependUpdate("nam", 505);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(505));
		assertTrue(list.get(1).equals(33));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListPrependAllUpdate() {
		List<Integer> testList = new ArrayList<Integer>();
		testList.add(5);
		testList.add(10);
		
		ListPrependAllUpdate update = new ListPrependAllUpdate("nam", testList);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		List<Integer> testList2 = new ArrayList<Integer>();
		testList2.add(20);
		testList2.add(60);
		
		ListPrependAllUpdate update2 = 
			new ListPrependAllUpdate("nam", testList2);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(20));
		assertTrue(list.get(1).equals(60));
		assertTrue(list.get(2).equals(5));
		assertTrue(list.get(3).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveIndexUpdate() {
		List<Integer> testList = new ArrayList<Integer>();
		testList.add(5);
		testList.add(10);
		testList.add(15);
		testList.add(20);
		
		DDMVC.setValue("nam", testList);
		
		ListRemoveIndexUpdate update = new ListRemoveIndexUpdate("nam", 1);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 3);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(15));
		assertTrue(list.get(2).equals(20));
		
		try {
			ListRemoveIndexUpdate update2 = new ListRemoveIndexUpdate("nam", 5);
			DDMVC.processUpdate(update2);
			fail();
		}
		catch(Exception e) {}
		
		try {
			ListRemoveIndexUpdate update2 = new ListRemoveIndexUpdate("f", 5);
			DDMVC.processUpdate(update2);
			fail();
		}
		catch(Exception e) {}
	}
	
	@Test
	public void updateListProcessing() {
		fail("not implemented");
	}
	
	
	
}
