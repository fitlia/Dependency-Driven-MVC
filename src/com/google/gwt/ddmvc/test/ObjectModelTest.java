package com.google.gwt.ddmvc.test;

import com.google.gwt.ddmvc.model.Field;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ObjectModel;
import com.google.gwt.ddmvc.model.Property;
import com.google.gwt.ddmvc.model.SubModel;

public class ObjectModelTest {

	private static class PersonModel extends ObjectModel {
		
		public static final Property<String> NAME = property("person");
		public static final Property<String> RACE = property("race");
		public static final Property<Integer> AGE = property("age");
		public static final SubModel<Model> CHARACTER = subModel("character");
		
		private static final Field[] fields = new Field[] {
				NAME, RACE, AGE, CHARACTER
		};
		
		public PersonModel() {
			super(fields);
		}
		
	}
	
}
