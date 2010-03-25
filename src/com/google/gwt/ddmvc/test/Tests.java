package com.google.gwt.ddmvc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Contains all the relevant tests for the DDMVC library
 * @author Kevin Dolan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
	com.google.gwt.ddmvc.test.controller.Tests.class,
	com.google.gwt.ddmvc.test.model.Tests.class,
	com.google.gwt.ddmvc.test.view.Tests.class,
})
public class Tests {}
