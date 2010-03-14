package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.ReservedWordException;
import com.google.gwt.ddmvc.model.ModelDoesNotExistException;

/**
 * Unit tests for the basic model methods of DDMVC 
 * @author Kevin Dolan
 */
public class ModelBasics {
	
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
	public void setValueMethod() {
		DDMVC.setValue("something", "fifty-four");
		assertTrue(DDMVC.getValue("something").equals("fifty-four"));
	}
	
	@Test
	public void modelDeleting() {
		DDMVC.deleteModel("something");
		assertTrue(DDMVC.hasModel("frillo"));
		assertFalse(DDMVC.hasModel("something"));
		assertFalse(DDMVC.hasModel("anything"));
	}
	
	@Test
	public void nonExistent() {
		try {
			DDMVC.getValue("hahaNotHere");
			fail();
		}
		catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void reservedWord() {
		try {
			DDMVC.setValue("view", 3);
			fail();
		} catch(ReservedWordException e) {}
	}
}
