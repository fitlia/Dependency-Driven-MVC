package com.google.gwt.ddmvc.test.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	com.google.gwt.ddmvc.test.model.update.Tests.class,
	com.google.gwt.ddmvc.test.model.path.Tests.class,
	ComputedModelTest.class,
	ModelModelTest.class,
	ModelTest.class,
	ObjectModelTest.class,
	ValueModelTest.class
	
})

public class Tests {}