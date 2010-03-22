package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;

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
import com.google.gwt.ddmvc.model.exception.ModelDoesNotExistException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.list.Append;

public class ValueModelTest {

	private Model root;
	private FakeObserver obs;

	private class FakeObserver implements Observer {
		public int change = 0;
		public Path getPath() { return null; }
		public void modelChanged(Collection<ModelUpdate> updates) {
			change++;
		}
		public boolean hasObservers() { return false; }
		public void notifyObservers(ModelUpdate update, UpdateLevel level) {}
	}
	
	@Before
	public void setUp() {
		DDMVC.reset();
		root = DDMVC.getDataRoot();
		obs = new FakeObserver();
	}
	
	@Test
	public void testGetValue() {
		root.setModel("cat", new ValueModel<String>("meow"));
		assertTrue(root.getValue("cat").equals("meow"));
		assertTrue(root.getModel("cat").getValue().equals("meow"));
		
		root.setModel("frog.toad", new ValueModel<String>("ribbit"));
		assertTrue(root.getValue("frog.toad").equals("ribbit"));
	}
	
	@Test
	public void testSetValue() {
		root.setModel("cat", new ValueModel<String>("meow"));
		root.setValue("cat", "purr");
		assertTrue(root.getValue("cat").equals("purr"));
		
		root.setModel("frog.toad", new ValueModel<String>("ribbit"));
		root.setValue("frog.toad", "croak");
		assertTrue(root.getValue("frog.toad").equals("croak"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateHandling() {
		root.setModel("lists.listA", 
				new ValueModel<List<String>>(new ArrayList<String>()));
		root.handleUpdate(new Append("lists.listA", "one"));
		assertTrue(((List<String>)root.getValue("lists.listA")).size() == 1);
	}
	
	@Test
	public void testIllegalSet() {
		root.setModel("cat", new ValueModel<String>("meow"));
		try{
			root.setValue("cat.tabby", "pow");
			fail();
		} catch(InvalidPathException e) {}
		assertTrue(root.getValue("cat").equals("meow"));
		
		root.setModel("cat", new ValueModel<String>("meow"));
		try{
			root.setModel("cat.tabby", new Model("pow"));
			fail();
		} catch(InvalidPathException e) {}
		assertTrue(root.getValue("cat").equals("meow"));
	}
	
	@Test
	public void testSetTypeSafe() {
		ValueModel<Integer> vm = new ValueModel<Integer>(1);
		root.setModel("cat", vm);
		vm.set(22);
		assertTrue(root.getValue("cat").equals(22));
	}
	
	@Test
	public void testReset() {
		root.setModel("cat", new ValueModel<String>("meow"));
		root.setModel("cat", new Model("moo"));
		assertTrue(root.getValue("cat").equals("moo"));
		root.setValue("cat.tabby", "purr");
		assertTrue(root.getValue("cat.tabby").equals("purr"));
	}
	
	@Test
	public void path() {
		root.setModel("cat.tabby", new ValueModel<String>("meow"));
		assertTrue(root.getModel("cat.tabby").getPath().equals("cat.tabby"));
	}
	
	@Test
	public void observation() {
		assertTrue(obs.change == 0);
		root.setModel("frog.toad", new ValueModel<String>("ribbit"));
		root.addObserver(obs, "frog.toad.$");
		root.setValue("frog.toad", "cooow");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	//Are observers preserved even when a higher-level model has
	//been replaced with a ValueModel?
	@Test
	public void copyObserversToValueModel() {
		root.setValue("frog.toad", "bibbit");
		root.setValue("frog.toad.green", "fibbit");
		root.addObserver(obs, "frog.toad.green");
		root.setModel("frog.toad", new ValueModel<String>("ribbit"));
		assertTrue(DDMVC.getObservers("frog.toad.green").size() == 1);
		try {
			root.getValue("frog.toad.green");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
}
