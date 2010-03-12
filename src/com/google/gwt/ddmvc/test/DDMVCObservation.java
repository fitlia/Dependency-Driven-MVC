package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.ComputedModel;
import com.google.gwt.ddmvc.model.DependencyNotFoundException;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.view.View;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tests of the DDMVC observation patterns 
 * @author Kevin Dolan
 */
public class DDMVCObservation {

	@Before
	public void setUp() {
		DDMVC.reset();
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
			DDMVC.getModel("property2").addObserver(this);
			DDMVC.getModel("property3").addObserver(this);
			DDMVC.getModel("property4").addObserver(this);
			DDMVC.getModel("property5").addObserver(this);
			DDMVC.getModel("property6").addObserver(this);
			myInt = 0;
		}

		@Override
		public void render(Collection<ModelUpdate> updates) {
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
		public Object computeValue(Collection<ModelUpdate> updates) {
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
	
	@Test
	public void dependencyRemoved() {
		chainSetup(true, true);
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.deleteModel("C");
		
		DDMVC.runLoop();
		
		try{
			DDMVC.getValue("F");
			fail();
		}
		catch(DependencyNotFoundException e) {}
	}
	
}
