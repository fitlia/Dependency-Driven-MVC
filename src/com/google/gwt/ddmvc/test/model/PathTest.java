package com.google.gwt.ddmvc.test.model;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.Property;
import com.google.gwt.ddmvc.model.SubModel;
import com.google.gwt.ddmvc.model.ValueModel;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * PathTest provides unit tests for all the methods in the Path class.
 * 
 * Particular effort is made to break the invariants of the Path class by
 * invalid inputs, since paths will often be built directly by programmers,
 * and this would be a very easy point of error.
 * 
 * @author Kevin Dolan
 */
public class PathTest {
	
	//
	// Path creation, acceptable
	//
	
	@Test
	public void singleField() {
		Path<?,?,?> path1 = Path.make("dog");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.advance().getImmediate() == null);
		assertTrue(path1.size() == 1);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void doubleField() {
		Path<?,?,?> path1 = Path.make("dog.Cat_1");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.size() == 2);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
		
		Path<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		assertTrue(path2.advance().getImmediate() == null);
		assertTrue(path2.size() == 1);
		
		assertFalse(path2.isTerminal());
		assertFalse(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void multiField() {
		Path<?,?,?> path1 = Path.make("dog.Cat_1.candle");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.size() == 3);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
		
		Path<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		assertTrue(path2.size() == 2);
		
		Path<?,?,?> path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("candle"));
		assertTrue(path3.advance().getImmediate() == null);
		assertTrue(path3.size() == 1);
	}
	
	@Test
	public void multiFieldWithAsterisk() {
		Path<?,?,?> path1 = Path.make("dog.Cat_1.*");
		assertTrue(path1.getImmediate().equals("dog"));
		
		assertTrue(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertTrue(path1.isFieldPath());
		
		Path<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		
		assertTrue(path2.isTerminal());
		assertFalse(path2.isValuePath());
		assertTrue(path2.isFieldPath());
		
		Path<?,?,?> path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("*"));
		assertTrue(path3.advance().getImmediate() == null);
		
		assertTrue(path3.isTerminal());
		assertFalse(path3.isValuePath());
		assertTrue(path3.isFieldPath());
	}
	
	@Test
	public void blankString() {
		Path<?,?,?> path1 = Path.make("");
		assertTrue(path1.getImmediate() == null);
		assertTrue(path1.advance() == null);
	}
	
	@Test
	public void asteriskOnly() {
		Path<?,?,?> path1 = Path.make("*");
		assertTrue(path1.getImmediate().equals("*"));
		assertTrue(path1.advance().getImmediate() == null);
	
		assertTrue(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertTrue(path1.isFieldPath());
	}

	@Test
	public void cashOnly() {
		Path<?,?,?> path1 = Path.make("$");
		assertTrue(path1.getImmediate().equals("$"));
		assertTrue(path1.advance().getImmediate() == null);
		
		assertTrue(path1.isTerminal());
		assertTrue(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeWithProperty() {
		Path<String,ValueModel,String> path1 = 
			Path.make(Property.make(String.class, "title"));
		assertTrue(path1.size() == 2);
		assertTrue(path1.getImmediate().equals("title"));
		assertTrue(path1.rightMost().equals("$"));
		
		assertTrue(path1.isTerminal());
		assertTrue(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeWithSubModel() {
		Path<Object,Model,Model> path1 = 
			Path.make(SubModel.make(Model.class, "title"));
		assertTrue(path1.size() == 1);
		assertTrue(path1.getImmediate().equals("title"));
		assertTrue(path1.advance().getImmediate() == null);
		assertFalse(path1.isTerminal());
	}
	
	@Test
	public void makeWithStringProperty() {
		Path<String,ValueModel,String> path1 = Path.make("cat.dog", 
				Property.make(String.class, "title"));
		assertTrue(path1.size() == 4);
		assertTrue(path1.getImmediate().equals("cat"));
		assertTrue(path1.advance().getImmediate().equals("dog"));
		assertTrue(path1.advance().advance().getImmediate().equals("title"));
		assertTrue(path1.rightMost().equals("$"));
		
		assertTrue(path1.isTerminal());
		assertTrue(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeWithStringSubModel() {
		Path<Object,Model,Model> path1 = Path.make("cat.dog",
				SubModel.make(Model.class, "title"));
		assertTrue(path1.size() == 3);
		assertTrue(path1.getImmediate().equals("cat"));
		assertTrue(path1.advance().getImmediate().equals("dog"));
		assertTrue(path1.advance().advance().getImmediate().equals("title"));
		assertTrue(path1.advance().advance().advance().getImmediate() == null);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeValue() {
		Path<?,?,?> path1 = Path.makeValue("something.$");
		assertTrue(path1.equals("something.$"));
		
		path1 = Path.makeValue("something.nothing");
		assertTrue(path1.equals("something.nothing.$"));
		
		path1 = Path.makeValue("something.nothing.*");
		assertTrue(path1.equals("something.nothing.$"));
	}
	
	@Test
	public void makeValueWithField() {
		Path<?,?,?> path1 = Path.makeValue(Property.make(String.class, "tom"));
		assertTrue(path1.equals("tom.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = Path.makeValue(SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("tom.$"));
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Object.class));
	}
	
	@Test
	public void makeValueWithStringField() {
		Path<?,?,?> path1 = Path.makeValue("sam",
				Property.make(String.class, "tom"));
		assertTrue(path1.equals("sam.tom.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = Path.makeValue("sam.noel", SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("sam.noel.tom.$"));
		assertTrue(path1.size() == 4);
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Object.class));
		
		try {
			Path.makeValue("sam.*", SubModel.make(Model.class, "tom"));
			fail();
		} catch (InvalidPathException e) {}
	}
	
	@Test
	public void makeModelString() {
		Path<Object,Model,Model> path1 = Path.makeModel("string.gizelle");
		assertTrue(path1.size() == 2);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("gizelle"));
		assertFalse(path1.isTerminal());
		
		path1 = Path.makeModel("string.gizelle.$");
		assertTrue(path1.size() == 2);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("gizelle"));
		assertFalse(path1.isTerminal());
		
		path1 = Path.makeModel("string.gizelle.*");
		assertTrue(path1.size() == 3);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("*"));
		assertTrue(path1.isTerminal());
	}
	
	@Test
	public void makeModelWithField() {
		Path<?,?,?> path1 = Path.makeModel(Property.make(String.class, "tom"));
		assertTrue(path1.equals("tom"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(ValueModel.class));
		
		path1 = Path.makeModel(SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("tom"));
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
	}

	@Test
	public void makeModelWithStringField() {
		Path<?,?,?> path1 = Path.makeModel("sam",
				Property.make(String.class, "tom"));
		assertTrue(path1.equals("sam.tom"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(ValueModel.class));
		
		path1 = Path.makeModel("sam.noel", SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("sam.noel.tom"));
		assertTrue(path1.size() == 3);
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		try {
			Path.makeModel("sam.*", SubModel.make(Model.class, "tom"));
			fail();
		} catch (InvalidPathException e) {}
	}
	
	//
	// Path creation, unacceptable
	//
	
	@Test
	public void internalAsteriskField() {
		try {
			Path.make("dog.*.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalAsteriskField2() {
		try {
			Path.make("dog.*.cat.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInField() {
		try {
			Path.make("dog.cat.randy*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInInternalField() {
		try {
			Path.make("dog.cat*.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
		
	@Test
	public void internalCashField() {
		try {
			Path.make("dog.$.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalCashField2() {
		try {
			Path.make("dog.$.cat.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInField() {
		try {
			Path.make("dog.cat.randy$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInInternalField() {
		try {
			Path.make("dog.cat$.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashAndAsterisk() {
		try {
			Path.make("dog.$.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskAndCash() {
		try {
			Path.make("dog.*.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void blankField() {
		try {
			Path.make("dog..cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void endBlank() {
		try {
			Path.make("dog.cat.");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void invalidCharacter() {
		try {
			Path.make("dog.cat .WhereMyPants");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	//
	// Path manipulators
	//
	
	@Test
	public void appendKey() {
		Path<?,?,?> path1 = Path.make("cat");
		Path<?,?,?> path2 = path1.append("dog");
		
		assertTrue(path1.getImmediate().equals("cat"));
		assertTrue(path1.advance().getImmediate() == null);
		
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate() == null);
	}
	
	@Test
	public void appendMultiKey() {
		Path<?,?,?> path1 = Path.make("cat");
		Path<?,?,?> path2 = path1.append("dog.weimerhamer");
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate().equals("weimerhamer"));
		assertTrue(path2.advance().advance().advance().getImmediate() == null);
	}
	
	@Test
	public void appendIllegalKey1() {
		Path<?,?,?> path1 = Path.make("cat");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendIllegalKey2() {
		Path<?,?,?> path1 = Path.make("*");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendToTerminal() {
		Path<?,?,?> path1 = Path.make("mom.dad.*");
		try {
			path1.append("dog");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAll() {
		Path<?,?,?> path1 = Path.make("dog.cat");
		Path<?,?,?> path2 = Path.make("mom.dad");
		assertTrue(path1.append(path2).toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void appendAllToTerminal() {
		Path<?,?,?> path1 = Path.make("dog.cat.$");
		Path<?,?,?> path2 = Path.make("mom.dad");
		try {
			path1.append(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAllToTerminal2() {
		Path<?,?,?> path1 = Path.make("dog.cat");
		Path<?,?,?> path2 = Path.make("mom.dad.$");
		Path<?,?,?> path3 = path1.append(path2);
		try {
			path3.append("mouse");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendTerminalString() {
		Path<?,?,?> path1 = Path.make("dog.cat");
		Path<?,?,?> path2 = path1.append("$");
		assertTrue(path2.toString().equals("dog.cat.$"));
		
		assertTrue(path2.isTerminal());
		assertTrue(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void appendProperty() {
		Path<?,?,?> path1 = Path.make("cat.dog");
		Path<String,ValueModel,String> path2 = 
			path1.append(Property.make(String.class, "title"));
		assertTrue(path2.size() == 4);
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate().equals("title"));
		assertTrue(path2.rightMost().equals("$"));
		
		assertTrue(path2.isTerminal());
		assertTrue(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void appendSubModel() {
		Path<?,?,?> path1 = Path.make("cat.dog");
		Path<Object,Model,Model> path2 = 
			path1.append(SubModel.make(Model.class, "title"));
		assertTrue(path2.size() == 3);
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate().equals("title"));
		assertTrue(path2.advance().advance().advance().getImmediate() == null);
		
		assertFalse(path2.isTerminal());
		assertFalse(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void appendPropertyWrong() {
		Path<?,?,?> path1 = Path.make("mom.dad.*");
		try {
			path1.append(Property.make(String.class, "title"));
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void testToString() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad.*");
		assertTrue(path1.toString().equals("dog.cat.mom.dad.*"));
	}
	
	@Test
	public void resolvePath() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("dog.cat");
		Path<?,?,?> path3 = path1.resolvePath(path2);
		
		assertTrue(path3.toString().equals("mom.dad"));
		assertTrue(path1.toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void resolveImpossiblePath() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("dog.bird");
		try {
			path1.resolvePath(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void startsWithSuccess() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("dog.cat");
		
		assertTrue(path1.startsWith(path2));
	}	
	
	@Test
	public void startsWithFailure() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("dog.bird");
		
		assertFalse(path1.startsWith(path2));
	}
	
	@Test
	public void startsWithIdentical() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		
		assertTrue(path1.startsWith(path1));
	}
	
	@Test
	public void endsWithSuccess() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("mom.dad");
		
		assertTrue(path1.endsWith(path2));
	}	
	
	@Test
	public void endsWithFailure() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		Path<?,?,?> path2 = Path.make("fred.dad");
		
		assertFalse(path1.endsWith(path2));
	}
	
	@Test
	public void endsWithIdentical() {
		Path<?,?,?> path1 = Path.make("dog.cat.mom.dad");
		
		assertTrue(path1.endsWith(path1));
	}	

}