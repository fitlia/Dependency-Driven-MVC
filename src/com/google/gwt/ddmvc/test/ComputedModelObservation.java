package com.google.gwt.ddmvc.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.RunLoopException;
import com.google.gwt.ddmvc.model.ComputedModel;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ModelDoesNotExistException;

public class ComputedModelObservation {

	private Model root;
	
	@Before
	public void setUp() {
		DDMVC.reset();
		root = DDMVC.getDataRoot();
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
			return (Integer) DDMVC.getDataRoot().getValue(dependent, this) + 1;
		}
		
	}

	@Test
	public void simpleComputedModel() {
		root.setValue("A", 0);
		root.setModel("B", new Increment("A", true, true));
		assertTrue(root.getValue("B").equals(1));
	}
	
	@Test
	public void computedModelObserving() {
		root.setValue("A", 0);
		root.setModel("B", new Increment("A", true, true));
		root.getValue("B");
		
		root.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(root.getValue("B").equals(2));
	}
	
	private void chainSetup(boolean cache, boolean imm) {
		root.setValue("A", 0);
		root.setModel("B", new Increment("A", cache, imm));
		root.setModel("C", new Increment("A", cache, imm));
		root.setModel("D", new Increment("C", cache, imm));
		root.setModel("E", new Increment("C", cache, imm));
		root.setModel("F", new Increment("E", cache, imm));
	}
	
	@Test
	public void computedModelChaining() {
		chainSetup(true, true);
		
		assertTrue(root.getValue("B").equals(1));
		assertTrue(root.getValue("C").equals(1));		
		assertTrue(root.getValue("D").equals(2));
		assertTrue(root.getValue("E").equals(2));
		assertTrue(root.getValue("F").equals(3));
	}
	
	@Test
	public void computedModelDistalChaining() {
		chainSetup(true, true);
		
		assertTrue(root.getValue("F").equals(3));
	}
	
	@Test
	public void computedModelChainingObserving() {
		chainSetup(true, true);
		
		root.getValue("F");
		
		root.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(root.getValue("B").equals(2));
		assertTrue(root.getValue("C").equals(2));		
		assertTrue(root.getValue("D").equals(3));
		assertTrue(root.getValue("E").equals(3));
		assertTrue(root.getValue("F").equals(4));
	}
	
	@Test
	public void computedModelDistalChainingObserving() {
		chainSetup(true, true);
		
		root.getValue("F");
		
		root.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(root.getValue("F").equals(4));
	}

	@Test
	public void computedModelNoCache() {
		chainSetup(false, true);
		
		root.getValue("F");
		
		root.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(root.getValue("F").equals(4));
	}
	
	@Test
	public void computedModelNoImmediate() {
		chainSetup(true, false);
		
		root.getValue("F");
		
		root.setValue("A", 1);
		DDMVC.runLoop();
		assertTrue(root.getValue("F").equals(4));
	}
	
	@Test
	public void dependencyRemoved() {
		chainSetup(true, true);
		root.getValue("F");
		
		root.deleteModel("C");
		
		DDMVC.runLoop();
		
		try{
			root.getValue("F");
			fail();
		}
		catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void dependencyRemovedNoLoop() {
		chainSetup(false, false);
		root.getValue("F");
		
		root.deleteModel("C");
		
		try{
			root.getValue("F");
			fail();
		}
		catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void exceptionHandling() {		
		chainSetup(true, true);
		root.getValue("F");
		
		root.setValue("A", 1);
		root.deleteModel("C");
		
		List<RunLoopException> exceptions = DDMVC.runLoop();
		assertTrue(exceptions.size() == 3);
		for(RunLoopException e : exceptions)
			assertTrue(e.getException().getClass()
					.equals(ModelDoesNotExistException.class));
	}
	
}
