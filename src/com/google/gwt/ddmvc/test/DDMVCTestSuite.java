package com.google.gwt.ddmvc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	DDMVCModelBasics.class,
	DDMVCModelUpdate.class,
	DDMVCObservation.class
})
public class DDMVCTestSuite {}
