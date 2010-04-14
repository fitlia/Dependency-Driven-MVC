package com.google.gwt.ddmvc.test.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ObjectModel;
import com.google.gwt.ddmvc.model.path.Field;
import com.google.gwt.ddmvc.model.path.Property;
import com.google.gwt.ddmvc.model.path.SubModel;

public class ObjectModelTest {

	private static class Person extends ObjectModel {
		
		public static final Property<String> 
			NAME = property(String.class, "name"),
			RACE = property(String.class, "race");
		public static final Property<Integer> 
			AGE = property("age", 1);
		public static final SubModel<Model> 
			CHARACTER = subModel(Model.class, "character");
		
		private static Field<?,?,?>[] fields = new Field[] { 
			NAME, RACE, AGE, CHARACTER 
		};
		
		public Person() {
			super(fields);
		}
		
		public Person(String name) {
			super(fields);
			setValue(NAME, "ronald");
		}
		
	}
	
	@Before
	public void setUp() {
		DDMVC.reset();
	}

	@Test
	public void basicModelFeatures() {
		Person person = new Person();
		assertTrue(person.get(Person.NAME) == null);
		assertTrue(person.get(Person.RACE) == null);
		assertTrue(person.get(Person.AGE) == 1);
		assertTrue(person.get(Person.CHARACTER) == null);
	}
	
}
