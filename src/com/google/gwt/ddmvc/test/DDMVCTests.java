package com.google.gwt.ddmvc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Contains all the relevant tests for the DDMVC library
 * @author Kevin Dolan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	DDMVCModelBasics.class,
	DDMVCModelUpdate.class,
	DDMVCObservation.class
})
public class DDMVCTests {}
