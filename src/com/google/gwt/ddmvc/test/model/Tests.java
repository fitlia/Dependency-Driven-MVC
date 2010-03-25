package com.google.gwt.ddmvc.test.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
	com.google.gwt.ddmvc.test.model.update.Tests.class,
	ComputedModelTest.class,
	ModelModelTest.class,
	ModelTest.class,
	ObjectModelTest.class,
	PathTest.class,
	ValueModelTest.class
	
})

public class Tests {}