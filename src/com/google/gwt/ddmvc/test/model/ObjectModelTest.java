package com.google.gwt.ddmvc.test.model;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.Field;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ObjectModel;
import com.google.gwt.ddmvc.model.Property;
import com.google.gwt.ddmvc.model.SubModel;

public class ObjectModelTest {

	private static class PersonModel extends ObjectModel {
		
		public static final Property<String> 
			NAME = property(String.class, "name"),
			RACE = property(String.class, "race");
		public static final Property<Integer> 
			AGE = property("age", 0);
		public static final SubModel<Model> 
			CHARACTER = subModel(Model.class, "character");
		
		private static Field<?,?,?>[] fields = new Field[] { 
			NAME, RACE, AGE, CHARACTER 
		};
		
		public PersonModel() {
			super(fields);
		}
		
		public PersonModel(String name) {
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
		PersonModel pm = new PersonModel();
	}
	
}
