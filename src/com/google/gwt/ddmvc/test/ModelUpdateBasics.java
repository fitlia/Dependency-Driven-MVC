package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.*;
import com.google.gwt.ddmvc.model.update.*;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.Prepend;
/**
 * Tests for DDMVC handling of ModelUpdates 
 * 
 * @author Kevin Dolan
 */
public class ModelUpdateBasics {
	
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
		DDMVC.applyUpdate(update);
		assertTrue(DDMVC.getValue("frillo").equals("hodgepodge"));
		
		SetValue update2 = new SetValue("fresh", "bite");
		DDMVC.applyUpdate(update2);
		assertTrue(DDMVC.getValue("fresh").equals("bite"));
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
		
		DDMVC.applyUpdates(updates);
		
		assertTrue(DDMVC.getValue("cow").equals("moo"));
		assertTrue(DDMVC.getValue("shawn").equals(33));
		
		
		List<Integer> list = (List<Integer>) DDMVC.getValue("aList");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals(0));
		assertTrue(list.get(3).equals(3));
	}
	
}
