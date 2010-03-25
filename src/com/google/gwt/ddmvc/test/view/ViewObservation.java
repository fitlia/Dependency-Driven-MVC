package com.google.gwt.ddmvc.test.view;

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

	private class SimpleRenderCounter extends View {

		public int render;

		@Override
		public void initialize() {		
			observe("property1.$");
			observe("property2.$");
			observe("property3.$");
			render = 0;
		}

		@Override
		public void render() {
			render++;
		}
		
	}
	
	private class RenderOptimizer extends View {

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
	
	private SimpleRenderCounter src;
	private RenderOptimizer ro;
	
	@Before
	public void setUp() {
		DDMVC.reset();
		
		DDMVC.setValue("property1", 10);
		DDMVC.setValue("property2", 10);
		DDMVC.setValue("property3", new ArrayList<Integer>());
		DDMVC.setValue("property4", 15);
		DDMVC.setValue("dog", "bark");
		DDMVC.setValue("dog.cat", "meow");
		DDMVC.setValue("person.english", "hello");
		DDMVC.setValue("frog", "ribbit");
		DDMVC.setValue("frog.toad", "croak");
		DDMVC.setValue("frog.toad.green", "crooak");
		
		src = new SimpleRenderCounter();
		DDMVC.addObserver(src, "property4.$");
		DDMVC.addObserver(src, "property5.$");
		DDMVC.addObserver(src, "property6.$");
		DDMVC.addObserver(src, "dog");
		DDMVC.addObserver(src, "dog.cat");
		DDMVC.addObserver(src, "person.english.$");
		DDMVC.addObserver(src, "frog.*");

		ro = new RenderOptimizer();
	}	
	
	//
	//
	// SIMPLE OBSERVATION TESTS
	//
	//
	
	@SuppressWarnings("unchecked")
	@Test
	public void simpleViewObserving() {		
		DDMVC.setValue("property1", "foo");
		DDMVC.setValue("property2", "bar");
		DDMVC.setValue("property3", new ArrayList<String>());
		
		assertTrue(src.render == 1);
		
		DDMVC.setValue("property1", "pam");
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setValue("property2", "wow");
		DDMVC.runLoop();
		assertTrue(src.render == 3);
		
		DDMVC.update("property3");
		DDMVC.runLoop();
		assertTrue(src.render == 4);
		
		((List<String>) DDMVC.getValue("property3")).add("bie");
		DDMVC.runLoop();
		assertTrue(src.render == 4);
		
		DDMVC.setValue("property4", "nish");
		DDMVC.runLoop();
		assertTrue(src.render == 5);
		
		DDMVC.update("property5");
		DDMVC.runLoop();
		assertTrue(src.render == 6);
	}	
	
	//
	//
	// RENDERING OPTIMIZATIONS
	//
	//
	
	@Test
	public void testRenderOnly() {
		DDMVC.setValue("property1", 12);
		DDMVC.runLoop();
		assertTrue(ro.render == 2);
		assertTrue(ro.setValue2 == 0);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);		
	}
	
	@Test
	public void setValueOnly() {
		DDMVC.setValue("property2", 45);
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.setValue2 == 1);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);		
	}
	
	@Test
	public void appendOnly() {
		DDMVC.handleUpdate(new Append("property3", 33));
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.setValue2 == 0);
		assertTrue(ro.append3 == 1);
		assertTrue(ro.prepend3 == 0);		
	}
	
	@Test
	public void prependOnly() {
		DDMVC.handleUpdate(new Prepend("property3", 33));
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.setValue2 == 0);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 1);
	}
	
	@Test
	public void appendUnobservedOnly() {
		DDMVC.setValue("property2", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.setValue2 == 1);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);
		
		DDMVC.handleUpdate(new Append("property2", 33));
		DDMVC.runLoop();
		assertTrue(ro.render == 2);
		assertTrue(ro.setValue2 == 1);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);
	}
	
	@Test
	public void allAtOnce() {
		DDMVC.setValue("property2", new ArrayList<Integer>());
		DDMVC.handleUpdate(new Append("property3", 33));
		DDMVC.handleUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.setValue2 == 1);
		assertTrue(ro.append3 == 1);
		assertTrue(ro.prepend3 == 1);
	}
	
	@Test
	public void severalWithOneUnobserver() {
		DDMVC.setValue("property1", 1);
		DDMVC.setValue("property2", 1);
		DDMVC.handleUpdate(new Append("property3", 33));
		DDMVC.handleUpdate(new Prepend("property3", 41));
		
		DDMVC.runLoop();
		assertTrue(ro.render == 2);
		assertTrue(ro.setValue2 == 0);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);
	}
	
	@Test
	public void propertyUpdateObserveration() {
		DDMVC.setValue("property4", new ArrayList<Integer>());
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.any4 == 1);
		
		Append app = new Append("property4", 15);
		DDMVC.handleUpdate(app);
		DDMVC.runLoop();
		assertTrue(ro.render == 1);
		assertTrue(ro.any4 == 2);
	}
	
	@Test
	public void withError() {
		DDMVC.setValue("property3", 1);
		DDMVC.runLoop();
		assertTrue(ro.render == 2);
		assertTrue(ro.setValue2 == 0);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);
		
		DDMVC.setValue("property2", 1);
		DDMVC.handleUpdate(new Append("property3", 33));
		
		DDMVC.runLoop();
		assertTrue(ro.render == 2);
		assertTrue(ro.setValue2 == 1);
		assertTrue(ro.append3 == 0);
		assertTrue(ro.prepend3 == 0);
		assertTrue(ro.excepted == 1);
		assertTrue(DDMVC.getValue("property3").getClass()
				.equals(ExceptionEncountered.class));
	}
	
	//
	//                          
	// HIERARCHY-SPECIFIC TESTS 
	//                          
	//
	
	@Test
	public void referentialObserving() {		
		DDMVC.setValue("dog", "roof");
		DDMVC.runLoop();
		assertTrue(src.render == 1);
		
		DDMVC.setModel("dog", new Model("roof"));
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setValue("dog.cat", "purr");
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setModel("dog.cat.tabby", new Model("purr"));
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setModel("dog.cat", new Model("purr"));
		DDMVC.runLoop();
		assertTrue(src.render == 3);
	}
	
	@Test
	public void valueObserving() {		
		DDMVC.setValue("person.english", "sup, foo");
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setModel("person.english", new Model("sup, foo"));
		DDMVC.runLoop();
		assertTrue(src.render == 3);
	}
	
	@Test
	public void fieldObserving() {
		DDMVC.setValue("frog", "smash");
		DDMVC.runLoop();
		assertTrue(src.render == 2);
		
		DDMVC.setValue("frog.toad", "smash");
		DDMVC.runLoop();
		assertTrue(src.render == 3);
		
		DDMVC.setModel("frog.toad", new Model("smash"));
		DDMVC.runLoop();
		assertTrue(src.render == 4);
		
		DDMVC.setModel("frog", new Model("smash"));
		DDMVC.runLoop();
		assertTrue(src.render == 5);
	}
	
	@Test
	public void observerRemoval() {
		SimpleRenderCounter src2 = new SimpleRenderCounter();
		
		DDMVC.addObserver(src, "bog.$");
		DDMVC.addObserver(src2, "bog.fish.car.$");
		
		DDMVC.setValue("bog", 1);
		DDMVC.setValue("bog.fish.car", 2);	
		DDMVC.runLoop();
		
		assertTrue(src.render == 2);
		assertTrue(src2.render == 2);
		
		DDMVC.removeObserver(src2, "bog.fish.car.$");
		DDMVC.setValue("bog", 1);
		DDMVC.setValue("bog.fish.car", 2);
		DDMVC.runLoop();
		
		assertTrue(src.render == 3);
		assertTrue(src2.render == 2);
	}
}