package com.google.gwt.ddmvc.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.AppendAll;
import com.google.gwt.ddmvc.model.update.list.Prepend;
import com.google.gwt.ddmvc.model.update.list.PrependAll;

public class ModelUpdateListInsertions {

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
	public void applyListAppendUpdate() {
		Append update = new Append("nam", 33);
		DDMVC.applyUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		Append update2 = new Append("nam", 505);
		DDMVC.applyUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(33));
		assertTrue(list.get(1).equals(505));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListAppendAllUpdate() {
		
		AppendAll update = new AppendAll("nam", testList);
		DDMVC.applyUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		AppendAll update2 = new AppendAll("nam", testList);
		DDMVC.applyUpdate(update2);
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
		DDMVC.applyUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(33));
		
		Prepend update2 = new Prepend("nam", 505);
		DDMVC.applyUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).equals(505));
		assertTrue(list.get(1).equals(33));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void applyListPrependAllUpdate() {
		
		PrependAll update = new PrependAll("nam", testList);
		DDMVC.applyUpdate(update);
		List<Integer> list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(5));
		assertTrue(list.get(1).equals(10));
		
		List<Integer> testList2 = new ArrayList<Integer>();
		testList2.add(20);
		testList2.add(60);
		
		PrependAll update2 = 
			new PrependAll("nam", testList2);
		DDMVC.applyUpdate(update2);
		list = (List<Integer>) DDMVC.getValue("nam");
		assertTrue(list.size() == 6);
		assertTrue(list.get(0).equals(20));
		assertTrue(list.get(1).equals(60));
		assertTrue(list.get(2).equals(5));
		assertTrue(list.get(3).equals(10));
	}
	
}
