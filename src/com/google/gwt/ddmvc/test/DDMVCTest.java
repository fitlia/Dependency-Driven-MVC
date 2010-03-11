package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.*;
import com.google.gwt.ddmvc.model.ComputedModel;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.view.View;
import com.google.gwt.user.client.ui.Widget;

/**
 * Unit tests for the DDMVC library 
 * 
 * @author Kevin Dolan
 */
public class DDMVCTest {
	
	@Before
	public void setUp() {
		DDMVC.reset();
		DDMVC.setValue("frillo", "Hodgepodge");
		DDMVC.setValue("something", "text");
	}
	
	@Test
	public void modelExistence() {
		assertFalse(DDMVC.hasModel("anything"));
		assertTrue(DDMVC.hasModel("frillo"));
		assertTrue(DDMVC.hasModel("something"));
		assertFalse(DDMVC.hasModel("anything"));
	}
	
	@Test
	public void modelValue() {
		Model model = DDMVC.getModel("something");
		assertTrue(DDMVC.getValue("something").equals("text"));
		assertTrue(DDMVC.getModel("something").get().equals("text"));
		assertTrue(model.get().equals("text"));
	}
	
	@Test
	public void modelSetMethod() {
		Model model = DDMVC.getModel("something");
		model.set("forty-five");
		assertTrue(DDMVC.getValue("something").equals("forty-five"));
		assertTrue(DDMVC.getModel("something").get().equals("forty-five"));
		assertTrue(model.get().equals("forty-five"));
	}
	
	@Test
	public void setValueMethod() {
		DDMVC.setValue("something", "fifty-four");
		assertTrue(DDMVC.getValue("something").equals("fifty-four"));
		assertTrue(DDMVC.getModel("something").get().equals("fifty-four"));
	}
	
	@Test
	public void modelDeleting() {
		DDMVC.deleteModel("something");
		assertTrue(DDMVC.hasModel("frillo"));
		assertFalse(DDMVC.hasModel("something"));
		assertFalse(DDMVC.hasModel("anything"));
	}
	
	@Test
	public void explicitSetModel() {
		DDMVC.setModel("model", new Model("fresh"));
		assertTrue(DDMVC.getValue("model").equals("fresh"));
		assertTrue(DDMVC.getModel("model").get().equals("fresh"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateListProcessing() {
		DDMVC.setValue("something", "text");
		DDMVC.setValue("anything", "text");
		DDMVC.setValue("aList", new ArrayList<String>());
		Model aList = DDMVC.getModel("aList");
		
		List<String> list = (List<String>) aList.get();
		list.add("french");
		list.add("kiss");
		list.add("french");
		list.add("fries");
		
		List<ModelUpdate> updates = new ArrayList<ModelUpdate>();
		updates.add(new ModelUpdate("something","dog"));
		updates.add(new ModelUpdate("nothing", com.google.gwt.ddmvc.model.update.SET, "cat"));
		
		updates.add(new ModelUpdate("aList", com.google.gwt.ddmvc.model.update.LIST_ADD, "toy"));
		List<String> strings = new ArrayList<String>();
		strings.add("sog");
		strings.add("bog");
		updates.add(new ModelUpdate("aList", com.google.gwt.ddmvc.model.update.LIST_ADD_ALL, strings));
		updates.add(new ModelUpdate("aList", com.google.gwt.ddmvc.model.update.LIST_REMOVE, "french"));
		updates.add(new ModelUpdate("aList", com.google.gwt.ddmvc.model.update.LIST_REMOVE_INDEX, 0));
		
		DDMVC.processUpdates(updates);
		
		assertTrue(DDMVC.getValue("something").equals("dog"));
		assertTrue(DDMVC.getValue("anything").equals("text"));
		assertTrue(DDMVC.getValue("nothing").equals("cat"));
		
		list = (List<String>) DDMVC.getValue("aList");
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).equals("fries"));
		assertTrue(list.get(1).equals("toy"));
		assertTrue(list.get(2).equals("sog"));
		assertTrue(list.get(3).equals("bog"));
	}
	
	/**
	 * This class is simply used to count the number of times render() is called
	 * @author Kevin Dolan
	 */
	private class CountView extends View {

		private Integer myInt;

		@Override
		public Widget getWidget() {
			return null;
		}

		@Override
		public void initialize() {
			DDMVC.getModel("property1").addObserver(this);
			DDMVC.getModel("property2").get(this);
			DDMVC.getValue("property3", this);
			DDMVC.getModel("property4").addObserver(this);
			DDMVC.getModel("property5").get(this);
			DDMVC.getValue("property6", this);
			myInt = 0;
		}

		@Override
		public void render() {
			myInt++;
		}
		
		public Integer getMyInt() {
			return myInt;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleViewObserving() {		
		DDMVC.setValue("property1", "foo");
		DDMVC.setValue("property2", "bar");
		DDMVC.setValue("property3", new ArrayList<String>());
		
		CountView cv = new CountView();
		assertTrue(cv.getMyInt() == 1);
		
		DDMVC.setValue("property1", "pam");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 2);
		
		DDMVC.getModel("property2").set("wow");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 3);
		
		DDMVC.getModel("property3").update();
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 4);
		
		((List<String>) DDMVC.getValue("property3")).add("bie");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 4);
		
		DDMVC.setValue("property4", "nish");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 5);
		
		DDMVC.getModel("property5").update();
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 6);
		
		DDMVC.setModel("property6", new Model("naah"));
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 7);
	}
	
	private class Increment extends ComputedModel {

		private String dependent;
		private boolean isCache, imm;
		
		public Increment(String dependent, boolean isCache, boolean imm) {
			this.dependent = dependent;
			this.isCache = isCache;
			this.imm = imm;
		}
		
		@Override
		public boolean isCacheable() {
			return isCache;
		}
		
		@Override
		public boolean isImmediate() {
			return imm;
		}
		
		@Override
		public Object computeValue() {
			return (Integer) DDMVC.getValue(dependent, this) + 1;
		}
		
	}
	
	@Test
	public void simpleComputedModel() {
		DDMVC.setValue("A", 0);
		DDMVC.setModel("B", new Increment("A", true, true));
		assertTrue(DDMVC.getValue("B").equals(1));
	}
	
	@Test
	public void computedModelObserving() {
		DDMVC.setValue("A", 0);
		DDMVC.setModel("B", new Increment("A", true, true));
		DDMVC.getValue("B");
		
		DDMVC.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(DDMVC.getValue("B").equals(2));
	}
	
	private void chainSetup(boolean cache, boolean imm) {
		DDMVC.setValue("A", 0);
		DDMVC.setModel("B", new Increment("A", cache, imm));
		DDMVC.setModel("C", new Increment("A", cache, imm));
		DDMVC.setModel("D", new Increment("C", cache, imm));
		DDMVC.setModel("E", new Increment("C", cache, imm));
		DDMVC.setModel("F", new Increment("E", cache, imm));
	}
	
	@Test
	public void computedModelChaining() {
		chainSetup(true, true);
		
		assertTrue(DDMVC.getValue("B").equals(1));
		assertTrue(DDMVC.getValue("C").equals(1));		
		assertTrue(DDMVC.getValue("D").equals(2));
		assertTrue(DDMVC.getValue("E").equals(2));
		assertTrue(DDMVC.getValue("F").equals(3));
	}
	
	@Test
	public void computedModelDistalChaining() {
		chainSetup(true, true);
		
		assertTrue(DDMVC.getValue("F").equals(3));
	}
	
	@Test
	public void computedModelChainingObserving() {
		chainSetup(true, true);
		
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(DDMVC.getValue("B").equals(2));
		assertTrue(DDMVC.getValue("C").equals(2));		
		assertTrue(DDMVC.getValue("D").equals(3));
		assertTrue(DDMVC.getValue("E").equals(3));
		assertTrue(DDMVC.getValue("F").equals(4));
	}
	
	@Test
	public void computedModelDistalChainingObserving() {
		chainSetup(true, true);
		
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(DDMVC.getValue("F").equals(4));
	}

	@Test
	public void computedModelNoCache() {
		chainSetup(false, true);
		
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(DDMVC.getValue("F").equals(4));
	}
	
	@Test
	public void computedModelNoImmediate() {
		chainSetup(true, false);
		
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(DDMVC.getValue("F").equals(4));
	}
	
}
