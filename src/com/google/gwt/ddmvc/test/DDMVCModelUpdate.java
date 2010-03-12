package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.*;
import com.google.gwt.ddmvc.model.update.*;
import com.google.gwt.ddmvc.model.update.list.AppendAll;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.KeepAllThatMatch;
import com.google.gwt.ddmvc.model.update.list.ListFilter;
import com.google.gwt.ddmvc.model.update.list.PrependAll;
import com.google.gwt.ddmvc.model.update.list.Prepend;
import com.google.gwt.ddmvc.model.update.list.RemoveAllEqualTo;
import com.google.gwt.ddmvc.model.update.list.RemoveAllThatMatch;
import com.google.gwt.ddmvc.model.update.list.RemoveIndex;
import com.google.gwt.ddmvc.model.update.list.Sort;

/**
 * Tests for DDMVC handling of ModelUpdates 
 * 
 * @author Kevin Dolan
 */
public class DDMVCModelUpdate {
	
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
	
	@Test
	public void applySetUpdate() {
		SetValue update = new SetValue("frillo", "hodgepodge");
		DDMVC.processUpdate(update);
		assertTrue(DDMVC.getValue("frillo").equals("hodgepodge"));
		
		SetValue update2 = new SetValue("fresh", "bite");
		DDMVC.processUpdate(update2);
		assertTrue(DDMVC.getValue("fresh").equals("bite"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListAppendUpdate() {
		Append update = new Append("nam", 33);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		Append update2 = new Append("nam", 505);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(33));
		assertTrue(list.get(1).equals(505));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListAppendAllUpdate() {
		
		AppendAll update = new AppendAll("nam", testList);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		AppendAll update2 = new AppendAll("nam", testList);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 8);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		assertTrue(list.get(4).equals(5));
		assertTrue(list.get(5).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListPrependUpdate() {
		Prepend update = new Prepend("nam", 33);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		Prepend update2 = new Prepend("nam", 505);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(505));
		assertTrue(list.get(1).equals(33));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListPrependAllUpdate() {
		
		PrependAll update = new PrependAll("nam", testList);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		List<Integer> testList2 = new ArrayList<Integer>();
		testList2.add(20);
		testList2.add(60);
		
		PrependAll update2 = 
			new PrependAll("nam", testList2);
		DDMVC.processUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 6);
		assertTrue(list.get(0).equals(20));
		assertTrue(list.get(1).equals(60));
		assertTrue(list.get(2).equals(5));
		assertTrue(list.get(3).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListRemoveIndexUpdate() {		
		DDMVC.setValue("nam", testList);
		
		RemoveIndex update = new RemoveIndex("nam", 1);
		DDMVC.processUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 3);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(15));
		assertTrue(list.get(2).equals(20));
		assertTrue(update.getObjectRemoved().equals(10));
		
		RemoveIndex update2 = new RemoveIndex("nam", 5);
		DDMVC.processUpdate(update2);
		assertTrue(update2.getException() != null);
		ExceptionEncountered ee = (ExceptionEncountered) DDMVC.getValue("nam");
		assertTrue(IndexOutOfBoundsException.class
				.equals(ee.getException().getClass()));
		assertTrue(ee.getCause() == update2);
		
	}
	
	@Test
	public void exceptionHandling() {
		Append update3 = new Append("frillo", 5);
		DDMVC.processUpdate(update3);
		assertTrue(update3.getException() != null);
		ExceptionEncountered ee = (ExceptionEncountered) DDMVC.getValue("frillo");
		assertTrue(ClassCastException.class
				.equals(ee.getException().getClass()));
		assertTrue(ee.getCause() == update3);
		
		RemoveIndex update4 = new RemoveIndex("f", 5);
		DDMVC.processUpdate(update4);
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
		
		DDMVC.processUpdate(new RemoveAllThatMatch("nam", 
				new ListFilter() {
					public boolean accept(Object o) {
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
		
		DDMVC.processUpdate(new KeepAllThatMatch("nam", 
				new ListFilter() {
					public boolean accept(Object o) {
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
		
		DDMVC.processUpdate(new RemoveAllEqualTo("nam", 10));
			
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertFalse(list.get(1).equals(10));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListSortUpdate() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(100);
		list.add(90);
		list.add(95);
		list.add(3);
		DDMVC.setValue("nam", list);
		
		DDMVC.processUpdate(new Sort("nam"));
		list = (List<Integer>) DDMVC.getValue("nam");
				
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(3));
		assertTrue(list.get(1).equals(90));
		assertTrue(list.get(2).equals(95));
		assertTrue(list.get(3).equals(100));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListSortComparatorUpdate() {
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
		
		DDMVC.processUpdate(new Sort("nam", myComp));
		list = (List<Integer>) DDMVC.getValue("nam");
				
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(100));
		assertTrue(list.get(1).equals(95));
		assertTrue(list.get(2).equals(90));
		assertTrue(list.get(3).equals(3));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void updateListProcessing() {
		List<ModelUpdate> updates = new ArrayList<ModelUpdate>();
		
		updates.add(new SetValue("cow", "moo"));
		updates.add(new SetValue("shawn", 32));
		updates.add(new SetValue("shawn", 33));
		
		updates.add(new Append("aList", 1));
		updates.add(new Append("aList", 2));
		updates.add(new Append("aList", 3));
		
		updates.add(new Prepend("aList", 0));
		
		DDMVC.processUpdates(updates);
		
		assertTrue(DDMVC.getValue("cow").equals("moo"));
		assertTrue(DDMVC.getValue("shawn").equals(33));
		
		
		List<Integer> list = (List<Integer>) DDMVC.getValue("aList");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(0));
		assertTrue(list.get(3).equals(3));
	}
	
}
