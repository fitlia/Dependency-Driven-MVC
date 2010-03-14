package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.update.ExceptionEncountered;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.SetValue;
import com.google.gwt.ddmvc.model.update.list.Append;
import com.google.gwt.ddmvc.model.update.list.Prepend;
import com.google.gwt.ddmvc.view.View;

/**
 * Tests of the DDMVC observation patterns 
 * @author Kevin Dolan
 */
public class ViewObservation {

	@Before
	public void setUp() {
		DDMVC.reset();
	}
	
	/**
	 * This class is simply used to count the number of times render() is called
	 */
	private class CountView extends View {

		private int myInt;

		@Override
		public void initialize() {
			subscribeToModel("property1");
			subscribeToModel("property2");
			subscribeToModel("property3");
			subscribeToModel("property4");
			subscribeToModel("property5");
			subscribeToModel("property6");
			myInt = 0;
		}

		@Override
		public void render() {
			myInt++;
		}
		
		public int getMyInt() {
			return myInt;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleViewObserving() {		
		DDMVC.setValue("property1", "foo");
		DDMVC.setValue("property2", "bar");
		DDMVC.setValue("property3", new ArrayList<String>());
		
		CountView cv = new CountView();
		assertTrue(cv.getMyInt() == 1);
		
		DDMVC.setValue("property1", "pam");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 2);
		
		DDMVC.setValue("property2", "wow");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 3);
		
		DDMVC.update("property3");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 4);
		
		((List<String>) DDMVC.getValue("property3")).add("bie");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 4);
		
		DDMVC.setValue("property4", "nish");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 5);
		
		DDMVC.update("property5");
		DDMVC.runLoop();
		assertTrue(cv.getMyInt() == 6);
	}	
	
	/**
	 * This class is simply used to count the number of times render() is called
	 * and also the number of times otherUpdate().
	 */
	private class CountView2 extends View {

		private int render;
		private int setValue2;
		private int append3;
		private int prepend3;
		private int excepted;
		private int any4;
		
		@Override
		public void initialize() {
			subscribeToModel("property1");
			subscribeToModel("property2");
			subscribeToModel("property3");
			subscribeToModel("property4");
			
			render = 0;
			setValue2 = 0;
			append3 = 0;
			prepend3 = 0;
			excepted = 0;
			any4 = 0;
			
			subscribeToModelUpdate("property2", SetValue.class);
			subscribeToModelUpdate("property3", Prepend.class);
			subscribeToModelUpdate("property3", Append.class);
			subscribeToModelUpdate("property4", ModelUpdate.class);
		}
	
		@Override
		public void render() {
			render++;
		}
		
		@Override
		public void respondToModelUpdate(ModelUpdate update) {
			if(update.getException() != null)
				excepted++;
			else if(update.getTarget().equals("property2") 
					&& update.isSame(SetValue.DEFAULT))
				setValue2++;
			else if (update.getTarget().equals("property3")) {
				if(update.isSame(Prepend.DEFAULT))
					prepend3++;
				else if(update.isSame(Append.DEFAULT))
					append3++;
			}
			else if(update.getTarget().equals("property4"))
				any4++;
		}
		
		public int getRenderCount() {
			return render;
		}
		
		public int getSetValue2Count() {
			return setValue2;
		}
		
		public int getAppend3Count() {
			return append3;
		}
		
		public int getPrepend3Count() {
			return prepend3;
		}
		
		public int getExcepted() {
			return excepted;
		}
		
		public int getAny4Count() {
			return any4;
		}
	}
	
	private CountView2 cv2;
	
	private void setup2() {
		DDMVC.setValue("property1", 10);
		DDMVC.setValue("property2", 10);
		DDMVC.setValue("property3", new ArrayList<Integer>());
		DDMVC.setValue("property4", 15);
		
		cv2 = new CountView2();
	}
	
	@Test
	public void testRenderOnly() {
		setup2();
		
		DDMVC.setValue("property1", 12);
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 2);
		assertTrue(cv2.getSetValue2Count() == 0);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);		
	}
	
	@Test
	public void testSetValueOnly() {
		setup2();
		
		DDMVC.setValue("property2", 45);
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getSetValue2Count() == 1);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);		
	}
	
	@Test
	public void testAppendOnly() {
		setup2();
		
		DDMVC.applyUpdate(new Append("property3", 33));
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getSetValue2Count() == 0);
		assertTrue(cv2.getAppend3Count() == 1);
		assertTrue(cv2.getPrepend3Count() == 0);		
	}
	
	@Test
	public void testPrependOnly() {
		setup2();
		
		DDMVC.applyUpdate(new Prepend("property3", 33));
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getSetValue2Count() == 0);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 1);
	}
	
	@Test
	public void testAppendUnobservedOnly() {
		setup2();
		DDMVC.setValue("property2", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getSetValue2Count() == 1);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);
		
		DDMVC.applyUpdate(new Append("property2", 33));
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 2);
		assertTrue(cv2.getSetValue2Count() == 1);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);
	}
	
	@Test
	public void testAllAtOnce() {
		setup2();
		
		DDMVC.setValue("property2", new ArrayList<Integer>());
		DDMVC.applyUpdate(new Append("property3", 33));
		DDMVC.applyUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getSetValue2Count() == 1);
		assertTrue(cv2.getAppend3Count() == 1);
		assertTrue(cv2.getPrepend3Count() == 1);
	}
	
	@Test
	public void testSeveralWithOneUnobserver() {
		setup2();
		
		DDMVC.setValue("property1", 1);
		DDMVC.setValue("property2", 1);
		DDMVC.applyUpdate(new Append("property3", 33));
		DDMVC.applyUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 2);
		assertTrue(cv2.getSetValue2Count() == 0);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);
	}
	
	@Test
	public void testPropertyUpdateObserveration() {
		setup2();
		
		DDMVC.setValue("property4", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getAny4Count() == 1);
		
		Append app = new Append("property4", 15);
		DDMVC.applyUpdate(app);
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 1);
		assertTrue(cv2.getAny4Count() == 2);
	}
	
	@Test
	public void testWithError() {
		setup2();
		
		DDMVC.setValue("property3", 1);
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 2);
		assertTrue(cv2.getSetValue2Count() == 0);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);
		
		DDMVC.setValue("property2", 1);
		DDMVC.applyUpdate(new Append("property3", 33));
		
		DDMVC.runLoop();
		assertTrue(cv2.getRenderCount() == 2);
		assertTrue(cv2.getSetValue2Count() == 1);
		assertTrue(cv2.getAppend3Count() == 0);
		assertTrue(cv2.getPrepend3Count() == 0);
		assertTrue(cv2.getExcepted() == 1);
		assertTrue(DDMVC.getValue("property3").getClass()
				.equals(ExceptionEncountered.class));
	}
}