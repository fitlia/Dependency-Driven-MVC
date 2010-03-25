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

	private FakeObserver obs;

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
	}
	
	@Test
	public void basic() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setModel("cat", myModel);
		assertTrue(DDMVC.getValue("cat").equals("meow"));
		assertTrue(myModel.getPath().equals("cat"));
		assertTrue(myModel.getModel().getPath().equals("cat"));
	}
	
	@Test
	public void deepSet() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		assertTrue(DDMVC.getValue("cat").equals("purr"));
		assertTrue(DDMVC.getValue("cat.tabby").equals("meow"));
		assertTrue(myModel.getPath().equals("cat.tabby"));
		
		Model tabby = myModel.getModel();
		assertTrue(tabby.getValue().equals("meow"));
		assertTrue(tabby.getPath().equals("cat.tabby"));
	}
	
	@Test
	public void setValue() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.setValue("cat.tabby", "hiss");
		assertTrue(DDMVC.getValue("cat.tabby").equals("hiss"));
		assertTrue(myModel.getModel().getValue().equals("hiss"));
	}
	
	@Test
	public void setModel() {
		ModelModel<ValueModel<String>> myModel = 
			ModelModel.make(ValueModel.make("meow"));
		DDMVC.setModel("cat", myModel);
		DDMVC.setModel("cat", ValueModel.make("purr"));
		assertTrue(DDMVC.getModel("cat").getClass().equals(ModelModel.class));
		assertTrue(DDMVC.getValue("cat").equals("purr"));
	}
	
	@Test
	public void setModelSubtype() {
		ModelModel<Model> myModel = 
			ModelModel.make(new Model("meow"));
		DDMVC.setModel("cat", myModel);
		DDMVC.setModel("cat", ValueModel.make("purr"));
		assertTrue(DDMVC.getModel("cat").getClass().equals(ModelModel.class));
		assertTrue(DDMVC.getValue("cat").equals("purr"));
	}
	
	@Test
	public void setModelWrongType() {
		ModelModel<ValueModel<String>> myModel = 
			ModelModel.make(ValueModel.make("meow"));
		DDMVC.setModel("cat", myModel);
		try {
			DDMVC.setModel("cat", new Model());
			fail();
		} catch (ClassCastException e) {}
	}
	
	@Test
	public void setValueModel() {
		ModelModel<ValueModel<String>> myModel = 
			ModelModel.make(ValueModel.make("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);	
		DDMVC.setValue("cat.tabby", "hiss");
		assertTrue(DDMVC.getValue("cat.tabby").equals("hiss"));
		assertTrue(myModel.getModel().getValue().equals("hiss"));
		ValueModel<String> tabby = myModel.getModel();
		tabby.set("pow");
		assertTrue(DDMVC.getValue("cat.tabby").equals("pow"));
		assertTrue(myModel.getModel().getValue().equals("pow"));
	}
	
	@Test
	public void distalSet() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.setValue("cat.tabby.male.fish", "reeo");
		assertTrue(DDMVC.getValue("cat.tabby.male.fish").equals("reeo"));
		assertTrue(myModel.getValue("male.fish").equals("reeo"));
		assertTrue(myModel.getModel().getValue("male.fish").equals("reeo"));
		assertTrue(myModel.getModel("male.fish").getPath()
				.equals("cat.tabby.male.fish"));
	}
	
	@Test
	public void properReflection() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		assertTrue(DDMVC.getModel("cat.tabby").getClass().equals(ModelModel.class));
	}
	
	@Test
	public void addObserver() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.addObserver(obs, "cat.tabby.$");
		assertTrue(DDMVC.getModel("cat.tabby").getValueObservers().contains(obs));
		DDMVC.setValue("cat.tabby", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void addSubObserver() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.addObserver(obs, "cat.tabby.male.$");
		assertTrue(DDMVC.getObservers("cat.tabby.male.$").contains(obs));
		DDMVC.setValue("cat.tabby.male", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void addSubObserver2() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.addObserver(obs, "cat.tabby.*");
		assertTrue(DDMVC.getModel("cat.tabby")
				.getFieldObservers().contains(obs));
		DDMVC.setValue("cat.tabby.male", "reeo");
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
	}
	
	@Test
	public void addSubObserver3() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.addObserver(obs, "cat.tabby");
		assertTrue(DDMVC.getModel("cat.tabby")
				.getReferentialObservers().contains(obs));
		
		DDMVC.setModel("cat.tabby", new Model("roar"));
		DDMVC.runLoop();
		assertTrue(obs.change == 1);
		assertTrue(DDMVC.getValue("cat.tabby").equals("roar"));
		assertTrue(DDMVC.getModel("cat.tabby")
				.getReferentialObservers().contains(obs));
	}
	
	@Test
	public void defaultConstructor() {
		ModelModel<Model> myModel = ModelModel.make(Model.class);
		DDMVC.setValue("cat", "purr");
		DDMVC.setModel("cat.tabby", myModel);
		try {
			DDMVC.setValue("cat.tabby", "meow");
			fail();
		} catch(NullPointerException e) {}
	}
	
	@Test
	public void replaceModel() {
		ModelModel<Model> myModel = ModelModel.make(new Model("meow"));
		DDMVC.setModel("cat.tabby", myModel);
		DDMVC.deleteModel("cat.tabby");
		assertFalse(DDMVC.hasPath("cat.tabby"));
		DDMVC.setValue("cat.tabby", "purr");
		assertTrue(DDMVC.getModel("cat.tabby").getClass().equals(Model.class));
	}
}