package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.*;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.update.*;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.Prepend;
/**
 * Tests for DDMVC handling of ModelUpdates 
 * 
 * @author Kevin Dolan
 */
public class ModelUpdateBasics {
	
	@Before
	public void setUp() {
		DDMVC.reset();
		
		DDMVC.setValue("frillo", "Hodgepodge");
		DDMVC.setValue("something", "text");
		DDMVC.setValue("lists.listA", new ArrayList<Integer>());
		DDMVC.setValue("lists.listB", new ArrayList<Integer>());
	}
	
	@Test
	public void applySetValueUpdate() {
		SetValue update = new SetValue("frillo", "hodgepodge");
		DDMVC.handleUpdate(update);
		assertTrue(DDMVC.getValue("frillo").equals("hodgepodge"));
		
		SetValue update2 = new SetValue("fresh", "bite");
		DDMVC.handleUpdate(update2);
		assertTrue(DDMVC.getValue("fresh").equals("bite"));
		
		SetValue update3 = new SetValue("my.color.is", "red");
		DDMVC.handleUpdate(update3);
		assertTrue(DDMVC.getValue("my.color.is").equals("red"));
	}
	
	@Test
	public void applySetModelUpdate() {
		Model bite = new Model("bite");
		SetModel update = new SetModel("fresh", bite);
		DDMVC.handleUpdate(update);
		assertTrue(DDMVC.getValue("fresh").equals("bite"));
		
		Model red = new Model("red");
		SetModel update2 = new SetModel("lists.listB", red);
		DDMVC.handleUpdate(update2);
		assertTrue(DDMVC.getValue("lists.listB").equals("red"));
	}
	
	@Test
	public void illegalUpdatePath() {
		SetValue update = new SetValue("frillo.$", "hodgepodge");
		try{
			DDMVC.handleUpdate(update);
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void illegalUpdatePath2() {
		SetValue update = new SetValue("frillo.*", "hodgepodge");
		try{
			DDMVC.handleUpdate(update);
		} catch(InvalidPathException e) {}
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void updateListProcessing() {
		List<ModelUpdate> updates = new ArrayList<ModelUpdate>();
		
		updates.add(new SetValue("cow", "moo"));
		updates.add(new SetValue("shawn", 32));
		updates.add(new SetValue("shawn", 33));
		
		updates.add(new Append("lists.listA", 1));
		updates.add(new Append("lists.listA", 2));
		updates.add(new Prepend("lists.listA", 3));
		
		updates.add(new Prepend("lists.listB", 0));
		
		DDMVC.handleUpdates(updates);
		
		assertTrue(DDMVC.getValue("cow").equals("moo"));
		assertTrue(DDMVC.getValue("shawn").equals(33));
		
		
		List<Integer> listA = (List<Integer>) DDMVC.getValue("lists.listA");
		assertTrue(listA.size() == 3);
		assertTrue(listA.get(0).equals(3));
		assertTrue(listA.get(2).equals(2));
		
		List<Integer> listB = (List<Integer>) DDMVC.getValue("lists.listB");
		assertTrue(listB.size() == 1);
		assertTrue(listB.get(0).equals(0));
	}
	
}
