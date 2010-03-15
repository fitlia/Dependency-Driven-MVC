package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.model.InvalidPathException;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ModelDoesNotExistException;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.Observer;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.list.Append;

public class ModelTest {
	
	private class FakeObserver implements Observer {
		public String getKey() { return null; }
		public Set<Observer> getObservers() { return null; }
		public void modelChanged(Collection<ModelUpdate> updates) {}
	}
	
	private Model root;
	private Observer obs = new FakeObserver();
	
	@Before
	public void setUp() throws Exception {
		root = new Model();
		
		root.setValue("cat","meow");
		root.setValue("dog", "bark");
		root.setValue("person.french", "bonjour");
		root.setValue("person.english", "hello");
		root.setValue("lists.counting", new ArrayList<Integer>());
	}
	
	@Test
	public void testHasPath() {
		assertTrue(root.hasPath("cat"));
		assertTrue(root.hasPath("person"));
		assertTrue(root.hasPath("person.english"));
		
		assertFalse(root.hasPath("raccoon"));
		assertFalse(root.hasPath("person.spanish"));
		assertFalse(root.hasPath("raccoon.rabies"));
		
		assertTrue(root.hasPath("cat.$"));
		assertFalse(root.hasPath("$"));
		assertTrue(root.hasPath("person.*"));
		assertFalse(root.hasPath("cat.*"));
	}

	@Test
	public void testSetChild() {
		root.setChild("monkey", new Model(null, "oo oo", "anything"));
		assertTrue(root.get("monkey.$").equals("oo oo"));
		
		root.setChild("dog", new Model(null, "roof", null));
		assertTrue(root.get("dog.$").equals("roof"));
		
		try {
			root.setChild("dog.cow", root);
			fail();
		} catch(InvalidPathException e) {}
	}

	@Test
	public void testGetParent() {
		assertTrue(root.getParent() == null);
		Model cat = root.getModel("cat");
		assertTrue(cat.getParent() == root);
	}

	@Test
	public void testGetKey() {
		Model english = (Model) root.get("person.english");
		assertTrue(english.getKey().equals("english"));
	}

	@Test
	public void testGetPath() {
		assertTrue(root.getPath() == Path.ROOT_PATH);
		
		Model english = (Model) root.get("person.english");
		assertTrue(english.getPath().toString().equals("person.english"));
	}

	@Test
	public void testGetValue() {
		Model cat = root.getModel("cat");
		assertTrue(cat.getValue().equals("meow"));
	}
	
	@Test
	public void testGetValueByObserver() {
		Model cat = root.getModel("cat");
		assertTrue(cat.getValue(obs).equals("meow"));
		assertTrue(cat.getValueObservers().contains(obs));
	}
	
	@Test
	public void testGetValueByString() {
		assertTrue(root.getValue("cat").equals("meow"));
		
		try {
			root.getValue("cat.tabby");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void testGetValueByStringObserver() {
		assertTrue(root.getValue("cat", obs).equals("meow"));
		
		Model cat = root.getModel("cat");
		assertTrue(cat.getValueObservers().contains(obs));
	}

	@Test
	public void testGetValueByPath() {
		assertTrue(root.getValue(new Path("cat")).equals("meow"));
	}
	
	@Test
	public void testGetValueByPathObserver() {
		assertTrue(root.getValue(new Path("person.english"), obs).equals("hello"));
		
		Model cat = root.getModel("person.english");
		assertTrue(cat.getValueObservers().contains(obs));
	}
	
	@Test
	public void testGetModelByString() {
		assertTrue(root.getModel("cat").getValue().equals("meow"));
		
		try {
			root.getModel("cat.tabby");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void testGetModelByStringObserver() {
		assertTrue(root.getModel("cat", obs).getValue().equals("meow"));
		
		Model cat = root.getModel("cat");
		assertTrue(cat.getReferentialObservers().contains(obs));
	}
	
	@Test
	public void testGetModelByPath() {
		assertTrue(root.getModel(new Path("cat")).getValue().equals("meow"));
	}
	
	@Test
	public void testGetModelByPathObserver() {
		assertTrue(root.getModel(new Path("cat"), obs).getValue().equals("meow"));
		
		Model cat = root.getModel("cat");
		assertTrue(cat.getReferentialObservers().contains(obs));
	}

	@Test
	public void testGetByPathStringObserver() {
		assertTrue(root.get("person.french.$").equals("bonjour"));
		assertTrue(((Model)root.get("person.french"))
				.getValue().equals("bonjour"));
		assertTrue(((Model)root.get(new Path("person.french.*")))
				.getValue().equals("bonjour"));
		
		assertTrue(root.get("person.french.$", obs).equals("bonjour"));
		assertTrue(root.getModel("person.french")
				.getValueObservers().contains(obs));
		
		assertTrue(((Model)root.get("person.english", obs))
				.getValue().equals("hello"));
		assertTrue(root.getModel("person.english")
				.getReferentialObservers().contains(obs));
		
		assertTrue(((Model)root.get(new Path("cat.*"), obs))
				.getValue().equals("meow"));
		assertTrue(root.getModel("cat")
				.getFieldObservers().contains(obs));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHandleUpdate() {
		ModelUpdate update = new Append("lists.counting", 1);
		root.handleUpdate(update);
		
		List<Integer> counting = (List<Integer>) root.getValue("lists.counting");
		assertTrue(counting.size() == 1);
		assertTrue(counting.get(0).equals(1));
		
		update = new Append("lists.counting", 2);
		root.handleUpdate(update);
		assertTrue(counting.size() == 2);
		assertTrue(counting.get(0).equals(1));
		assertTrue(counting.get(1).equals(2));
	}

	@Test
	public void testSetValueByObject() {
		root.setValue("history");
		assertTrue(root.getValue().equals("history"));
	}

	@Test
	public void testSetValueStringObject() {
		root.setValue("cat.tabby","moo");
		assertTrue(root.getValue("cat.tabby").equals("moo"));
	}

	@Test
	public void testSetValuePathObject() {
		root.setValue(new Path("cat.tabby"),"moo");
		assertTrue(root.getValue("cat.tabby").equals("moo"));
	}

	@Test
	public void testSetModelByModel() {
		Model person = root.getModel("person");
		Model newPerson = new Model("maw");
		person.setModel(newPerson);
		
		assertTrue(root.getValue("person").equals("maw"));
		
		try {
			root.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}

	@Test
	public void testSetModelByStringModel() {
		Model newPerson = new Model("maw");
		root.setModel("person", newPerson);
		
		assertTrue(root.getValue("person").equals("maw"));
		
		try {
			root.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}

	@Test
	public void testSetModelByPathModel() {
		Model newPerson = new Model("maw");
		root.setModel(new Path("person"), newPerson);
		
		assertTrue(root.getValue("person").equals("maw"));
		
		try {
			root.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}

}
