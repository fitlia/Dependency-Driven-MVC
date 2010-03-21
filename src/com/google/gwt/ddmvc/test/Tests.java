package com.google.gwt.ddmvc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Contains all the relevant tests for the DDMVC library
 * @author Kevin Dolan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	PathTest.class,
	ModelTest.class,
	ModelUpdateBasics.class,
	ModelUpdateListFiltering.class,
	ModelUpdateListInsertions.class,
	ModelUpdateListSorting.class,
	ValueModelTest.class,
	ModelModelTest.class,
	ObjectModelTest.class,
	ViewObservation.class,
	ComputedModelObservation.class,
	StandardControllerTest.class
})
public class Tests {}
