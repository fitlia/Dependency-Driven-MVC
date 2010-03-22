package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.model.Model;
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

	private Model root;
	
	@Before
	public void setUp() {
		DDMVC.reset();
		root = DDMVC.getDataRoot();
	}	
	
	//
	//
	// SIMPLE OBSERVATION TESTS
	//
	//
	
	private class CountView extends View {

		public int myInt;

		@Override
		public void initialize() {
			observe("property1.$");
			observe("property2.$");
			observe("property3.$");
			observe("property4.$");
			observe("property5.$");
			observe("property6.$");
			myInt = 0;
		}

		@Override
		public void render() {
			myInt++;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleViewObserving() {		
		root.setValue("property1", "foo");
		root.setValue("property2", "bar");
		root.setValue("property3", new ArrayList<String>());
		
		CountView cv = new CountView();
		assertTrue(cv.myInt == 1);
		
		root.setValue("property1", "pam");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 2);
		
		root.setValue("property2", "wow");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 3);
		
		root.update("property3");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 4);
		
		((List<String>) root.getValue("property3")).add("bie");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 4);
		
		root.setValue("property4", "nish");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 5);
		
		root.update("property5");
		DDMVC.runLoop();
		assertTrue(cv.myInt == 6);
	}	
	
	//
	//
	// RENDERING OPTIMIZATIONS
	//
	//
	
	private class CountView2 extends View {

		public int render;
		public int setValue2;
		public int append3;
		public int prepend3;
		public int excepted;
		public int any4;
		
		@Override
		public void initialize() {
			observe("property1.$");
			observe("property2.$");
			observe("property3.$");
			observe("property4.$");
			
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
	
	}
	
	private CountView2 cv2;
	
	private void setup2() {
		root.setValue("property1", 10);
		root.setValue("property2", 10);
		root.setValue("property3", new ArrayList<Integer>());
		root.setValue("property4", 15);
		
		cv2 = new CountView2();
	}
	
	@Test
	public void testRenderOnly() {
		setup2();
		
		root.setValue("property1", 12);
		DDMVC.runLoop();
		assertTrue(cv2.render == 2);
		assertTrue(cv2.setValue2 == 0);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);		
	}
	
	@Test
	public void setValueOnly() {
		setup2();
		
		root.setValue("property2", 45);
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.setValue2 == 1);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);		
	}
	
	@Test
	public void appendOnly() {
		setup2();
		
		root.handleUpdate(new Append("property3", 33));
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.setValue2 == 0);
		assertTrue(cv2.append3 == 1);
		assertTrue(cv2.prepend3 == 0);		
	}
	
	@Test
	public void prependOnly() {
		setup2();
		
		root.handleUpdate(new Prepend("property3", 33));
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.setValue2 == 0);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 1);
	}
	
	@Test
	public void appendUnobservedOnly() {
		setup2();
		root.setValue("property2", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.setValue2 == 1);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);
		
		root.handleUpdate(new Append("property2", 33));
		DDMVC.runLoop();
		assertTrue(cv2.render == 2);
		assertTrue(cv2.setValue2 == 1);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);
	}
	
	@Test
	public void allAtOnce() {
		setup2();
		
		root.setValue("property2", new ArrayList<Integer>());
		root.handleUpdate(new Append("property3", 33));
		root.handleUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.setValue2 == 1);
		assertTrue(cv2.append3 == 1);
		assertTrue(cv2.prepend3 == 1);
	}
	
	@Test
	public void severalWithOneUnobserver() {
		setup2();
		
		root.setValue("property1", 1);
		root.setValue("property2", 1);
		root.handleUpdate(new Append("property3", 33));
		root.handleUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(cv2.render == 2);
		assertTrue(cv2.setValue2 == 0);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);
	}
	
	@Test
	public void propertyUpdateObserveration() {
		setup2();
		
		root.setValue("property4", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.any4 == 1);
		
		Append app = new Append("property4", 15);
		root.handleUpdate(app);
		DDMVC.runLoop();
		assertTrue(cv2.render == 1);
		assertTrue(cv2.any4 == 2);
	}
	
	@Test
	public void withError() {
		setup2();
		
		root.setValue("property3", 1);
		DDMVC.runLoop();
		assertTrue(cv2.render == 2);
		assertTrue(cv2.setValue2 == 0);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);
		
		root.setValue("property2", 1);
		root.handleUpdate(new Append("property3", 33));
		
		DDMVC.runLoop();
		assertTrue(cv2.render == 2);
		assertTrue(cv2.setValue2 == 1);
		assertTrue(cv2.append3 == 0);
		assertTrue(cv2.prepend3 == 0);
		assertTrue(cv2.excepted == 1);
		assertTrue(root.getValue("property3").getClass()
				.equals(ExceptionEncountered.class));
	}
	
	//
	//                          
	// HIERARCHY-SPECIFIC TESTS 
	//                          
	//

	private class CountView3 extends View {

		public int render;
		
		@Override
		public void initialize() {
			observe("dog");
			observe("dog.cat");
			observe("person.english.$");
			observe("frog.*");
			
			render = 0;
		}
	
		@Override
		public void render() {			
			render++;
		}
	
	}
	
	private CountView3 cv3;
	
	public void setup3() {		
		root.setValue("dog", "bark");
		root.setValue("dog.cat", "meow");
		root.setValue("person.english", "hello");
		root.setValue("frog", "ribbit");
		root.setValue("frog.toad", "croak");
		root.setValue("frog.toad.green", "crooak");

		cv3 = new CountView3();
	}
	
	@Test
	public void referentialObserving() {
		setup3();	
		
		root.setValue("dog", "roof");
		DDMVC.runLoop();
		assertTrue(cv3.render == 1);
		
		root.setModel("dog", new Model("roof"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
		
		root.setValue("dog.cat", "purr");
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
		
		root.setModel("dog.cat.tabby", new Model("purr"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
		
		root.setModel("dog.cat", new Model("purr"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 3);
	}
	
	@Test
	public void valueObserving() {
		setup3();
		
		root.setValue("person.english", "sup, foo");
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
		
		root.setModel("person.english", new Model("sup, foo"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 3);
	}
	
	@Test
	public void fieldObserving() {
		setup3();
		
		root.setValue("frog", "smash");
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
		
		root.setValue("frog.toad", "smash");
		DDMVC.runLoop();
		assertTrue(cv3.render == 3);
		
		root.setModel("frog.toad", new Model("smash"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 4);
		
		root.setModel("frog", new Model("smash"));
		DDMVC.runLoop();
		assertTrue(cv3.render == 5);
	}
	
	@Test
	public void observerRemoval() {
		setup3();
		CountView3 cv3_2 = new CountView3();
		
		DDMVC.addObserver(cv3, "bog.$");
		DDMVC.addObserver(cv3_2, "bog.fish.car.$");
		
		root.setValue("bog", 1);
		root.setValue("bog.fish.car", 2);
		
		DDMVC.runLoop();
		assertTrue(cv3.render == 2);
	}
}