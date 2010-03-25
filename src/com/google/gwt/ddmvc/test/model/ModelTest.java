package com.google.gwt.ddmvc.test.model;

import static org.junit.Assert.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ObjectModel;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.Property;
import com.google.gwt.ddmvc.model.SubModel;
import com.google.gwt.ddmvc.model.Model.UpdateLevel;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.exception.ModelDoesNotExistException;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.list.Append;

/**
 * ModelTest tests all the standard data-model features of the DDMVC library,
 * particularly through the proxies provided by the static DDMVC singleton.
 * 
 * @author Kevin Dolan
 */
public class ModelTest {
	
	private class FakeObserver implements Observer {
		public Path<?> getPath() { return null; }
		public void modelChanged(Collection<ModelUpdate> updates) {}
		public boolean hasObservers() { return false; }
		public void notifyObservers(ModelUpdate update, UpdateLevel level) {}
	}
	
	private Observer obs = new FakeObserver();
	
	@Before
	public void setUp() throws Exception {
		DDMVC.reset();
		
		DDMVC.setValue("cat","meow");
		DDMVC.setValue("dog", "bark");
		DDMVC.setValue("person.french", "bonjour");
		DDMVC.setValue("person.english", "hello");
		DDMVC.setValue("lists.counting", new ArrayList<Integer>());
		
		DDMVC.setValue("frog", "ribbit");
		DDMVC.setValue("frog.toad", "croak");
		DDMVC.setValue("frog.jumping", "jump");
	}

	//
	//
	// Parent-child relationships
	//
	//
	
	//
	// This Model
	//
	
	@Test
	public void getKey() {
		Model english = (Model) DDMVC.get("person.english");
		assertTrue(english.getKey().equals("english"));
	}

	@Test
	public void getPath() {
		assertTrue(DDMVC.getDataRoot().getPath() == Path.ROOT_PATH);
		
		Model english = (Model) DDMVC.get("person.english");
		assertTrue(english.getPath().toString().equals("person.english"));
	}
	
	//
	// Child Models
	//
	
	@Test
	public void hasPath() {
		assertTrue(DDMVC.hasPath("cat"));
		assertTrue(DDMVC.hasPath("person"));
		assertTrue(DDMVC.hasPath("person.english"));
		assertTrue(DDMVC.hasPath("frog"));
		assertTrue(DDMVC.hasPath("frog.toad"));
		
		assertFalse(DDMVC.hasPath("raccoon"));
		assertFalse(DDMVC.hasPath("person.spanish"));
		assertFalse(DDMVC.hasPath("raccoon.rabies"));
		
		assertTrue(DDMVC.hasPath("cat.$"));
		assertTrue(DDMVC.hasPath("$"));
		assertTrue(DDMVC.hasPath("person.*"));
		assertTrue(DDMVC.hasPath("cat.*"));		
	}
	
	@Test
	public void hasPathWithField() {
		assertTrue(DDMVC.hasPath("person", 
				Property.make(String.class, "english")));
		assertTrue(DDMVC.hasPath("person", 
				SubModel.make(Model.class, "english")));
		
		assertFalse(DDMVC.hasPath("person", 
				Property.make(String.class, "spanish")));
		assertFalse(DDMVC.hasPath("person", 
				SubModel.make(Model.class, "spanish")));
	}
	
	@Test
	public void pathIsTypeValid() {
		assertTrue(DDMVC.pathIsTypeValid(Path.make("cat")));
		assertTrue(DDMVC.pathIsTypeValid("", 
				Property.make(String.class, "cat")));
		assertTrue(DDMVC.pathIsTypeValid("person", 
				Property.make(String.class, "english")));
		assertTrue(DDMVC.pathIsTypeValid("lists", 
				Property.make(AbstractList.class, "counting")));
		assertTrue(DDMVC.pathIsTypeValid("person",
				SubModel.make(Model.class, "french")));
		
		assertFalse(DDMVC.pathIsTypeValid("person", 
				Property.make(Integer.class, "english")));
		assertFalse(DDMVC.pathIsTypeValid("person",
				SubModel.make(ObjectModel.class, "french")));
		
		try {
			DDMVC.pathIsTypeValid("person", 
					Property.make(Integer.class, "mexican"));
			fail();
		} catch(ModelDoesNotExistException e) {}
		
		try {
			DDMVC.pathIsTypeValid(Path.make("person.english.*"));
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void resolvePath() {
		Model root = DDMVC.getDataRoot();
		assertTrue(root.resolvePath("cat").equals("cat"));
		assertTrue(root.resolvePath("person.english").equals("person.english"));
		assertTrue(root.resolvePath("person.english.abe").equals("person.english"));
		assertTrue(root.resolvePath("cat.fred").equals("cat"));
		assertTrue(root.resolvePath("person.german").equals("person"));
		assertTrue(root.resolvePath("noontime").equals("ROOT_PATH"));
	}
	
	//
	// Parent Models
	//

	@Test
	public void getParent() {
		assertTrue(DDMVC.getDataRoot().getParent() == null);
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getParent() == DDMVC.getDataRoot());
		
		Model toad = DDMVC.getModel("frog.toad");
		assertTrue(toad.getParent().getKey().equals("frog"));
	}
	
	@Test
	public void getRoot() {
		Model english = DDMVC.getModel("person.english");
		assertTrue(english.getRoot() == DDMVC.getDataRoot());
	}
	
	//
	//
	// Observer Methods
	//
	//
	
	//
	// Existence
	//

	@Test
	public void hasObservers() {
		DDMVC.addObserver(obs, "dog.cat.rain.main");
		assertTrue(DDMVC.hasObservers("dog.cat.rain.main"));
		
		DDMVC.setValue("fish.raid", "maui");
		DDMVC.addObserver(obs, "fish.gills.$");
		assertTrue(DDMVC.hasObservers("fish.gills"));
		assertFalse(DDMVC.hasObservers("fish.raid"));
		
		DDMVC.addObserver(obs, "pal.*");
		assertTrue(DDMVC.hasObservers("pal.fish.cat"));
	}
		
	//
	//
	// Accessors
	//
	//
	
	//
	// Value Accessors
	//
	
	@Test
	public void getValue() {
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getValue().equals("meow"));
	}
	
	@Test
	public void getValueByObserver() {
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getValue(obs).equals("meow"));
		assertTrue(cat.getValueObservers().contains(obs));
	}
	
	@Test
	public void getValueByString() {
		assertTrue(DDMVC.getValue("cat").equals("meow"));
		
		try {
			DDMVC.getValue("cat.tabby");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void getValueByStringObserver() {
		assertTrue(DDMVC.getValue("cat", obs).equals("meow"));
		
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getValueObservers().contains(obs));
	}

	@Test
	public void getValueByPath() {
		assertTrue(DDMVC.getValue(Path.make("cat")).equals("meow"));
	}
	
	@Test
	public void getValueByPathObserver() {
		assertTrue(DDMVC.getValue(Path.make("person.english"), obs).equals("hello"));
		
		Model cat = DDMVC.getModel("person.english");
		assertTrue(cat.getValueObservers().contains(obs));
	}
	
	//
	// Model Accessors
	//
	
	@Test
	public void getModelByString() {
		assertTrue(DDMVC.getModel("cat").getValue().equals("meow"));
		
		try {
			DDMVC.getModel("cat.tabby");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void getModelByStringObserver() {
		assertTrue(DDMVC.getModel("cat", obs).getValue().equals("meow"));
		
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getReferentialObservers().contains(obs));
	}
	
	@Test
	public void getModelByPath() {
		assertTrue(DDMVC.getModel(Path.make("cat")).getValue().equals("meow"));
	}
	
	@Test
	public void getModelByPathObserver() {
		assertTrue(DDMVC.getModel(Path.make("cat"), obs).getValue().equals("meow"));
		
		Model cat = DDMVC.getModel("cat");
		assertTrue(cat.getReferentialObservers().contains(obs));
	}
	
	//
	// Generic Accessors
	//

	@Test
	public void getByPathStringObserver() {
		assertTrue(DDMVC.get("person.french.$").equals("bonjour"));
		assertTrue(((Model)DDMVC.get("person.french"))
				.getValue().equals("bonjour"));
		assertTrue(((Model)DDMVC.get(Path.make("person.french.*")))
				.getValue().equals("bonjour"));
		
		assertTrue(DDMVC.get("person.french.$", obs).equals("bonjour"));
		assertTrue(DDMVC.getModel("person.french")
				.getValueObservers().contains(obs));
		
		assertTrue(((Model)DDMVC.get("person.english", obs))
				.getValue().equals("hello"));
		assertTrue(DDMVC.getModel("person.english")
				.getReferentialObservers().contains(obs));
		
		assertTrue(((Model)DDMVC.get(Path.make("cat.*"), obs))
				.getValue().equals("meow"));
		assertTrue(DDMVC.getModel("cat")
				.getFieldObservers().contains(obs));
	}
	
	//
	//
	// Update Handling
	//
	//
	
	//
	// Generic update handling
	//
	
	@SuppressWarnings("unchecked")
	@Test
	public void handleUpdate() {
		ModelUpdate update = new Append("lists.counting", 1);
		DDMVC.handleUpdate(update);
		
		List<Integer> counting = (List<Integer>) DDMVC.getValue("lists.counting");
		assertTrue(counting.size() == 1);
		assertTrue(counting.get(0).equals(1));
		
		update = new Append("lists.counting", 2);
		DDMVC.handleUpdate(update);
		assertTrue(counting.size() == 2);
		assertTrue(counting.get(0).equals(1));
		assertTrue(counting.get(1).equals(2));
		
		Model model = DDMVC.getModel("lists.counting");
		update = new Append("lists.counting", 3);
		model.handleUpdate(update);
		assertTrue(counting.size() == 3);
		assertTrue(counting.get(0).equals(1));
		assertTrue(counting.get(1).equals(2));
		assertTrue(counting.get(2).equals(3));
		
		Model model2 = DDMVC.getModel("cat");
		update = new Append("lists.counting", 4);
		try{
			model2.handleUpdate(update);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	//
	// Set Value
	//

	@Test
	public void setValueByObject() {
		Model model = DDMVC.getModel("person.english");
		model.setValue(6);
		assertTrue(DDMVC.getValue("person.english").equals(6));
	}

	@Test
	public void setValueStringObject() {
		DDMVC.setValue("cat.tabby","moo");
		assertTrue(DDMVC.getValue("cat.tabby").equals("moo"));
	}

	@Test
	public void setValuePathObject() {
		DDMVC.setValue(Path.make("cat.tabby"),"moo");
		assertTrue(DDMVC.getValue("cat.tabby").equals("moo"));
	}
	
	//
	// Set Model
	//

	@Test
	public void setModelByModel() {
		Model person = DDMVC.getModel("person");
		Model newPerson = new Model("maw");
		person.setModel(newPerson);
		
		assertTrue(DDMVC.getValue("person").equals("maw"));
		
		try {
			DDMVC.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}

	@Test
	public void setModelByStringModel() {
		Model newPerson = new Model("maw");
		DDMVC.setModel("person", newPerson);
		
		assertTrue(DDMVC.getValue("person").equals("maw"));
		
		try {
			DDMVC.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}

	@Test
	public void setModelByPathModel() {
		Model newPerson = new Model("maw");
		DDMVC.setModel(Path.make("person"), newPerson);
		
		assertTrue(DDMVC.getValue("person").equals("maw"));
		
		try {
			DDMVC.getValue("person.english");
			fail();
		} catch(ModelDoesNotExistException e) {}
	}
	
	@Test
	public void setModelObserverDuplication() {
		DDMVC.addObserver(obs, "frog.granouille.green");
		DDMVC.setValue("frog.granouille.green", "bark");
		DDMVC.setModel("frog", new Model("ribbit"));
		try{
			DDMVC.getValue("frog.granouille.green");
			fail();
		} catch(ModelDoesNotExistException e) {}
		assertTrue(DDMVC.getObservers("frog.granouille.green").contains(obs));
	}
	
	@Test
	public void pathSetting() {
		Model dog = new Model("roof");
		dog.setValue("cat", "meow");
		DDMVC.setModel("dog", dog);
		
		assertTrue(DDMVC.getModel("dog.cat").getPath().equals("dog.cat"));
	}
	
	@Test
	public void moveModel() {
		DDMVC.setValue("dog.food", "bones");
		Model model = DDMVC.getModel("dog.food");
		DDMVC.setModel("cat.food", model);
		assertTrue(model.getParent() == DDMVC.getModel("cat"));
		assertTrue(DDMVC.hasPath("cat.food"));
		assertFalse(DDMVC.hasPath("dog.food"));
	}

	@Test
	public void moveModelDifferentKey() {
		DDMVC.setValue("dog.food", "bones");
		Model model = DDMVC.getModel("dog.food");
		DDMVC.setModel("cat.notfood", model);
		assertTrue(model.getKey().equals("notfood"));
		assertTrue(model.getParent() == DDMVC.getModel("cat"));
		assertTrue(DDMVC.hasPath("cat.notfood"));
		assertFalse(DDMVC.hasPath("dog.food"));
	}
	
	//
	// Delete Model
	//
	
	@Test
	public void deleteModelByString() {
		DDMVC.deleteModel("person");
		assertFalse(DDMVC.hasPath("person"));
	}
	
	@Test
	public void deleteModelByPath() {
		DDMVC.deleteModel(Path.make("person"));
		assertFalse(DDMVC.hasPath("person"));
	}
	
}
