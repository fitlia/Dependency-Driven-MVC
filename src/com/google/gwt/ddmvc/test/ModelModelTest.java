package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ModelModel;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.ValueModel;
import com.google.gwt.ddmvc.model.Model.UpdateLevel;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

public class ModelModelTest {

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
	public void testBasic() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setModel("cat", myModel);
		assertTrue(root.getValue("cat").equals("meow"));
		assertTrue(myModel.getPath().equals("cat"));
		assertTrue(myModel.getModel().getPath().equals("cat"));
	}
	
	@Test
	public void testDeepSet() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		assertTrue(root.getValue("cat").equals("purr"));
		assertTrue(root.getValue("cat.tabby").equals("meow"));
		assertTrue(myModel.getPath().equals("cat.tabby"));
		
		Model tabby = myModel.getModel();
		assertTrue(tabby.getValue().equals("meow"));
		assertTrue(tabby.getPath().equals("cat.tabby"));
	}
	
	@Test
	public void testSetValue() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.setValue("cat.tabby", "hiss");
		assertTrue(root.getValue("cat.tabby").equals("hiss"));
		assertTrue(myModel.getModel().getValue().equals("hiss"));
	}
	
	@Test
	public void testSetValueModel() {
		ModelModel<ValueModel<String>> myModel = 
			new ModelModel<ValueModel<String>>(new ValueModel<String>("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);	
		root.setValue("cat.tabby", "hiss");
		assertTrue(root.getValue("cat.tabby").equals("hiss"));
		assertTrue(myModel.getModel().getValue().equals("hiss"));
		ValueModel<String> tabby = myModel.getModel();
		tabby.set("pow");
		assertTrue(root.getValue("cat.tabby").equals("pow"));
		assertTrue(myModel.getModel().getValue().equals("pow"));
	}
	
	@Test
	public void testDistalSet() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.setValue("cat.tabby.male.fish", "reeo");
		assertTrue(root.getValue("cat.tabby.male.fish").equals("reeo"));
		assertTrue(myModel.getValue("male.fish").equals("reeo"));
		assertTrue(myModel.getModel().getValue("male.fish").equals("reeo"));
		assertTrue(myModel.getModel("male.fish").getPath()
				.equals("cat.tabby.male.fish"));
	}
	
	@Test
	public void testProperReflection() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		assertTrue(root.getModel("cat.tabby").getClass().equals(ModelModel.class));
	}
	
	@Test
	public void testAddObserver() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.addObserver(obs, "cat.tabby.$");
		assertTrue(root.getModel("cat.tabby").getValueObservers().contains(obs));
		root.setValue("cat.tabby", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void testAddSubObserver() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.addObserver(obs, "cat.tabby.male.$");
		assertTrue(DDMVC.getObservers("cat.tabby.male.$").contains(obs));
		root.setValue("cat.tabby.male", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void testAddSubObserver2() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.addObserver(obs, "cat.tabby.*");
		assertTrue(root.getModel("cat.tabby")
				.getFieldObservers().contains(obs));
		root.setValue("cat.tabby.male", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void testAddSubObserver3() {
		ModelModel<Model> myModel = new ModelModel<Model>(new Model("meow"));
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		root.addObserver(obs, "cat.tabby");
		assertTrue(root.getModel("cat.tabby")
				.getReferentialObservers().contains(obs));
		
		root.setModel("cat.tabby", new Model("roar"));
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
		assertTrue(root.getValue("cat.tabby").equals("roar"));
		assertTrue(root.getModel("cat.tabby")
				.getReferentialObservers().contains(obs));
	}
	
	@Test
	public void testDefaultConstructor() {
		ModelModel<Model> myModel = new ModelModel<Model>();
		root.setValue("cat", "purr");
		root.setModel("cat.tabby", myModel);
		try {
			root.setValue("cat.tabby", "meow");
			fail();
		} catch(NullPointerException e) {}
	}
	
}