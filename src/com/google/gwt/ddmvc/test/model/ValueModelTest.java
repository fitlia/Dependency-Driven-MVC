package com.google.gwt.ddmvc.test.model;

import static org.junit.Assert.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.*;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.ValueModel;
import com.google.gwt.ddmvc.model.Model.UpdateLevel;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.list.Append;

public class ValueModelTest {

	private FakeObserver obs;
	private FakeObserver obs2;

	private class FakeObserver implements Observer {
		public int change = 0;
		public Path<?> getPath() { return null; }
		public void modelChanged(Collection<ModelUpdate> updates) {
			change++;
		}
		public boolean hasObservers() { return false; }
		public void notifyObservers(ModelUpdate update, UpdateLevel level) {}
	}
	
	@Before
	public void setUp() {
		DDMVC.reset();
		obs = new FakeObserver();
		obs2 = new FakeObserver();
	}
	
	@Test
	public void classReflection() {
		ValueModel<?> model1 = ValueModel.make("meow");
		assertTrue(model1.getValueClass().equals(String.class));
		
		ValueModel<?> model2 = ValueModel.make(String.class);
		assertTrue(model2.getValueClass().equals(String.class));
		
		ValueModel<?> model3 = ValueModel.make(String.class, "meow");
		assertTrue(model3.getValueClass().equals(String.class));
	}
	
	@Test
	public void getValue() {
		DDMVC.setModel("cat", ValueModel.make("meow"));
		assertTrue(DDMVC.getValue("cat").equals("meow"));
		assertTrue(DDMVC.getModel("cat").getValue().equals("meow"));
		
		DDMVC.setModel("frog.toad", ValueModel.make(String.class, "ribbit"));
		assertTrue(DDMVC.getValue("frog.toad").equals("ribbit"));
	}
	
	@Test
	public void setValue() {
		DDMVC.setModel("cat", ValueModel.make("meow"));
		DDMVC.setValue("cat", "purr");
		assertTrue(DDMVC.getValue("cat").equals("purr"));
		assertTrue(DDMVC.getModel("cat").getPath().getExpectedType()
				.equals(ValueModel.class));
		
		DDMVC.setModel("frog.toad", ValueModel.make("ribbit"));
		DDMVC.setValue("frog.toad", "croak");
		assertTrue(DDMVC.getValue("frog.toad").equals("croak"));
	}
	
	@Test
	public void setValueSubtype() {
		DDMVC.setModel("cat", ValueModel.make(AbstractList.class));
		DDMVC.setValue("cat", new ArrayList<String>());
	}
	
	@Test
	public void setValueWrongType() {
		DDMVC.setModel("cat", ValueModel.make(String.class));
		try {
			DDMVC.setValue("cat", 3);
			fail();
		} catch (ClassCastException e) {} 
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateHandling() {
		DDMVC.setModel("lists.listA", 
				ValueModel.make(new ArrayList<String>()));
		DDMVC.handleUpdate(new Append("lists.listA", "one"));
		assertTrue(((List<String>)DDMVC.getValue("lists.listA")).size() == 1);
	}
	
	@Test
	public void illegalSet() {
		DDMVC.setModel("cat", ValueModel.make("meow"));
		try{
			DDMVC.setValue("cat.tabby", "pow");
			fail();
		} catch(InvalidPathException e) {}
		assertTrue(DDMVC.getValue("cat").equals("meow"));
		
		DDMVC.setModel("cat", ValueModel.make("meow"));
		try{
			DDMVC.setModel("cat.tabby", new Model("pow"));
			fail();
		} catch(InvalidPathException e) {}
		assertTrue(DDMVC.getValue("cat").equals("meow"));
	}
		
	@Test
	public void reset() {
		DDMVC.setModel("cat", ValueModel.make("meow"));
		DDMVC.setModel("cat", new Model("moo"));
		assertTrue(DDMVC.getValue("cat").equals("moo"));
		DDMVC.setValue("cat.tabby", "purr");
		assertTrue(DDMVC.getValue("cat.tabby").equals("purr"));
	}
	
	@Test
	public void path() {
		DDMVC.setModel("cat.tabby", ValueModel.make("meow"));
		assertTrue(DDMVC.getModel("cat.tabby").getPath().equals("cat.tabby"));
	}
	
	@Test
	public void observation() {
		assertTrue(obs.change == 0);
		DDMVC.setModel("frog.toad", ValueModel.make("ribbit"));
		DDMVC.addObserver(obs, "frog.toad.$");
		DDMVC.setValue("frog.toad", "cooow");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
		
		DDMVC.addObserver(obs2, "frog.toad");
		DDMVC.setModel("frog.toad", new Model("rooo"));
		DDMVC.runLoop();
		assertTrue(obs.change == 2);
		assertTrue(obs2.change == 1);
	}

}
