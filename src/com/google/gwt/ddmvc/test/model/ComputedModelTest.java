package com.google.gwt.ddmvc.test.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.RunLoopException;
import com.google.gwt.ddmvc.model.ComputedModel;
import com.google.gwt.ddmvc.model.exception.ModelDoesNotExistException;

/**
 * This class tests the observation methods for computed models, with a
 * simple incrementer computed model.  Some effort is made to cause
 * exceptions by the removal of dependencies.
 * 
 * @author Kevin Dolan
 */
public class ComputedModelTest {
	
	@Before
	public void setUp() {
		DDMVC.reset();
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
		
		DDMVC.deleteModel("C");
		
		DDMVC.runLoop();
		
		try{
			DDMVC.getValue("F");
			fail();
		}
		catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void dependencyRemovedNoLoop() {
		chainSetup(false, false);
		DDMVC.getValue("F");
		
		DDMVC.deleteModel("C");
		
		try{
			DDMVC.getValue("F");
			fail();
		}
		catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void exceptionHandling() {		
		chainSetup(true, true);
		DDMVC.getValue("F");
		
		DDMVC.setValue("A", 1);
		DDMVC.deleteModel("C");
		
		List<RunLoopException> exceptions = DDMVC.runLoop();
		assertTrue(exceptions.size() == 3);
		for(RunLoopException e : exceptions)
			assertTrue(e.getException().getClass()
					.equals(ModelDoesNotExistException.class));
	}
}
