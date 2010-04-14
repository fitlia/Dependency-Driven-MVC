package com.google.gwt.ddmvc.test.model.path;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.ValueModel;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.path.DefaultPath;
import com.google.gwt.ddmvc.model.path.Property;
import com.google.gwt.ddmvc.model.path.SubModel;

/**
 * PathTest provides unit tests for all the methods in the DefaultPath class.
 * 
 * Particular effort is made to break the invariants of the DefaultPath class by
 * invalid inputs, since paths will often be built directly by programmers,
 * and this would be a very easy point of error.
 * 
 * @author Kevin Dolan
 */
public class PathTest {
	
	//
	// DefaultPath creation, acceptable
	//
	
	@Test
	public void singleField() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.advance().getImmediate() == null);
		assertTrue(path1.size() == 1);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void doubleField() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.Cat_1");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.size() == 2);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
		
		DefaultPath<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		assertTrue(path2.advance().getImmediate() == null);
		assertTrue(path2.size() == 1);
		
		assertFalse(path2.isTerminal());
		assertFalse(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void multiField() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.Cat_1.candle");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.size() == 3);
		
		assertFalse(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertFalse(path1.isFieldPath());
		
		DefaultPath<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		assertTrue(path2.size() == 2);
		
		DefaultPath<?,?,?> path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("candle"));
		assertTrue(path3.advance().getImmediate() == null);
		assertTrue(path3.size() == 1);
	}
	
	@Test
	public void multiFieldWithAsterisk() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.Cat_1.*");
		assertTrue(path1.getImmediate().equals("dog"));
		
		assertTrue(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertTrue(path1.isFieldPath());
		
		DefaultPath<?,?,?> path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		
		assertTrue(path2.isTerminal());
		assertFalse(path2.isValuePath());
		assertTrue(path2.isFieldPath());
		
		DefaultPath<?,?,?> path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("*"));
		assertTrue(path3.advance() == null);
		
		assertTrue(path3.isTerminal());
		assertFalse(path3.isValuePath());
		assertTrue(path3.isFieldPath());
	}
	
	@Test
	public void blankString() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("");
		assertTrue(path1.getImmediate() == null);
		assertTrue(path1.advance() == null);
	}
	
	@Test
	public void asteriskOnly() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("*");
		assertTrue(path1.getImmediate().equals("*"));
		assertTrue(path1.advance()== null);
	
		assertTrue(path1.isTerminal());
		assertFalse(path1.isValuePath());
		assertTrue(path1.isFieldPath());
	}

	@Test
	public void cashOnly() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("$");
		assertTrue(path1.getImmediate().equals("$"));
		assertTrue(path1.advance() == null);
		
		assertTrue(path1.isTerminal());
		assertTrue(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeWithTypes() {
		DefaultPath<String, Model, Model> path1 = DefaultPath.make(String.class, Model.class, 
				Model.class, "qa.qb.qc");
		assertTrue(path1.equals("qa.qb.qc"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		path1 = DefaultPath.make(String.class, Model.class, 
				Model.class, "qa.qb.qc.*");
		assertTrue(path1.equals("qa.qb.qc.*"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		DefaultPath<String, Model, String> path2 = DefaultPath.make(String.class, Model.class, 
				String.class, "qa.qb.qc.$");
		assertTrue(path2.equals("qa.qb.qc.$"));
		assertTrue(path2.getValueType().equals(String.class));
		assertTrue(path2.getModelType().equals(Model.class));
		assertTrue(path2.getReferenceType().equals(String.class));
	}
	
	@Test
	public void makeWithProperty() {
		DefaultPath<String,ValueModel,String> path1 = 
			DefaultPath.make(Property.make(String.class, "title"));
		assertTrue(path1.size() == 2);
		assertTrue(path1.getImmediate().equals("title"));
		assertTrue(path1.rightMost().equals("$"));
		
		assertTrue(path1.isTerminal());
		assertTrue(path1.isValuePath());
		assertFalse(path1.isFieldPath());
	}
	
	@Test
	public void makeWithSubModel() {
		DefaultPath<Object,Model,Model> path1 = 
			DefaultPath.make(SubModel.make(Model.class, "title"));
		assertTrue(path1.size() == 1);
		assertTrue(path1.getImmediate().equals("title"));
		assertTrue(path1.advance().getImmediate() == null);
		assertFalse(path1.isTerminal());
	}
	
	@Test
	public void makeWithStringProperty() {
		DefaultPath<String,ValueModel,String> path1 = DefaultPath.make("cat.dog", 
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
		DefaultPath<Object,Model,Model> path1 = DefaultPath.make("cat.dog",
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
		DefaultPath<?,?,?> path1 = DefaultPath.makeValue("something.$");
		assertTrue(path1.equals("something.$"));
		
		path1 = DefaultPath.makeValue("something.nothing");
		assertTrue(path1.equals("something.nothing.$"));
		
		path1 = DefaultPath.makeValue("something.nothing.*");
		assertTrue(path1.equals("something.nothing.$"));
	}
	
	@Test
	public void makeValueWithTypes() {
		DefaultPath<String, Model, String> path1 = DefaultPath.makeValue(String.class, 
				Model.class, "qa.qb");
		assertTrue(path1.equals("qa.qb.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = DefaultPath.makeValue(String.class, Model.class, "qa.qb.*");
		assertTrue(path1.equals("qa.qb.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = DefaultPath.makeValue(String.class, Model.class, "qa.qb.$");
		assertTrue(path1.equals("qa.qb.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(String.class));
	}
	
	@Test
	public void makeValueWithField() {
		DefaultPath<?,?,?> path1 = DefaultPath.makeValue(Property.make(String.class, "tom"));
		assertTrue(path1.equals("tom.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = DefaultPath.makeValue(SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("tom.$"));
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Object.class));
	}
	
	@Test
	public void makeValueWithStringField() {
		DefaultPath<?,?,?> path1 = DefaultPath.makeValue("sam",
				Property.make(String.class, "tom"));
		assertTrue(path1.equals("sam.tom.$"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(String.class));
		
		path1 = DefaultPath.makeValue("sam.noel", SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("sam.noel.tom.$"));
		assertTrue(path1.size() == 4);
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Object.class));
		
		try {
			DefaultPath.makeValue("sam.*", SubModel.make(Model.class, "tom"));
			fail();
		} catch (InvalidPathException e) {}
	}
	
	@Test
	public void makeModelString() {
		DefaultPath<Object,Model,Model> path1 = DefaultPath.makeModel("string.gizelle");
		assertTrue(path1.size() == 2);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("gizelle"));
		assertFalse(path1.isTerminal());
		
		path1 = DefaultPath.makeModel("string.gizelle.$");
		assertTrue(path1.size() == 2);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("gizelle"));
		assertFalse(path1.isTerminal());
		
		path1 = DefaultPath.makeModel("string.gizelle.*");
		assertTrue(path1.size() == 3);
		assertTrue(path1.leftMost().equals("string"));
		assertTrue(path1.rightMost().equals("*"));
		assertTrue(path1.isTerminal());
	}
	
	@Test
	public void makeModelWithTypes() {
		DefaultPath<String, Model, Model> path1 = DefaultPath.makeModel(String.class, 
				Model.class, "qa.qb");
		assertTrue(path1.equals("qa.qb"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		path1 = DefaultPath.makeModel(String.class, Model.class, "qa.qb.*");
		assertTrue(path1.equals("qa.qb.*"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		path1 = DefaultPath.makeModel(String.class, Model.class, "qa.qb.$");
		assertTrue(path1.equals("qa.qb"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
	}
	
	@Test
	public void makeModelWithField() {
		DefaultPath<?,?,?> path1 = DefaultPath.makeModel(Property.make(String.class, "tom"));
		assertTrue(path1.equals("tom"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(ValueModel.class));
		
		path1 = DefaultPath.makeModel(SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("tom"));
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
	}

	@Test
	public void makeModelWithStringField() {
		DefaultPath<?,?,?> path1 = DefaultPath.makeModel("sam",
				Property.make(String.class, "tom"));
		assertTrue(path1.equals("sam.tom"));
		assertTrue(path1.getValueType().equals(String.class));
		assertTrue(path1.getModelType().equals(ValueModel.class));
		assertTrue(path1.getReferenceType().equals(ValueModel.class));
		
		path1 = DefaultPath.makeModel("sam.noel", SubModel.make(Model.class, "tom"));
		assertTrue(path1.equals("sam.noel.tom"));
		assertTrue(path1.size() == 3);
		assertTrue(path1.getValueType().equals(Object.class));
		assertTrue(path1.getModelType().equals(Model.class));
		assertTrue(path1.getReferenceType().equals(Model.class));
		
		try {
			DefaultPath.makeModel("sam.*", SubModel.make(Model.class, "tom"));
			fail();
		} catch (InvalidPathException e) {}
	}
	
	//
	// DefaultPath creation, unacceptable
	//
	
	@Test
	public void internalAsteriskField() {
		try {
			DefaultPath.make("dog.*.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalAsteriskField2() {
		try {
			DefaultPath.make("dog.*.cat.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInField() {
		try {
			DefaultPath.make("dog.cat.randy*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInInternalField() {
		try {
			DefaultPath.make("dog.cat*.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
		
	@Test
	public void internalCashField() {
		try {
			DefaultPath.make("dog.$.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalCashField2() {
		try {
			DefaultPath.make("dog.$.cat.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInField() {
		try {
			DefaultPath.make("dog.cat.randy$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInInternalField() {
		try {
			DefaultPath.make("dog.cat$.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashAndAsterisk() {
		try {
			DefaultPath.make("dog.$.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskAndCash() {
		try {
			DefaultPath.make("dog.*.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void blankField() {
		try {
			DefaultPath.make("dog..cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void endBlank() {
		try {
			DefaultPath.make("dog.cat.");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void invalidCharacter() {
		try {
			DefaultPath.make("dog.cat .WhereMyPants");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void makeWithBadTypes() {
		try {
			DefaultPath.make(String.class, Model.class, String.class, "qa.qb.qc");
			fail();
		} catch(InvalidPathException e) {}
		
		try {
			DefaultPath.make(String.class, Model.class, String.class, "qa.qb.*");
			fail();
		} catch(InvalidPathException e) {}
		
		try {
			DefaultPath.make(String.class, Model.class, Model.class, "qa.qb.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	//
	// DefaultPath manipulators
	//
	
	@Test
	public void appendKey() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("cat");
		DefaultPath<?,?,?> path2 = path1.append("dog");
		
		assertTrue(path1.getImmediate().equals("cat"));
		assertTrue(path1.advance().getImmediate() == null);
		
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate() == null);
	}
	
	@Test
	public void appendMultiKey() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("cat");
		DefaultPath<?,?,?> path2 = path1.append("dog.weimerhamer");
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate().equals("weimerhamer"));
		assertTrue(path2.advance().advance().advance().getImmediate() == null);
	}
	
	@Test
	public void appendIllegalKey1() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("cat");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendIllegalKey2() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("*");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendToTerminal() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("mom.dad.*");
		try {
			path1.append("dog");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAll() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat");
		DefaultPath<?,?,?> path2 = DefaultPath.make("mom.dad");
		assertTrue(path1.append(path2).toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void appendAllToTerminal() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.$");
		DefaultPath<?,?,?> path2 = DefaultPath.make("mom.dad");
		try {
			path1.append(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAllToTerminal2() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat");
		DefaultPath<?,?,?> path2 = DefaultPath.make("mom.dad.$");
		DefaultPath<?,?,?> path3 = path1.append(path2);
		try {
			path3.append("mouse");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendTerminalString() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat");
		DefaultPath<?,?,?> path2 = path1.append("$");
		assertTrue(path2.toString().equals("dog.cat.$"));
		
		assertTrue(path2.isTerminal());
		assertTrue(path2.isValuePath());
		assertFalse(path2.isFieldPath());
	}
	
	@Test
	public void appendProperty() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("cat.dog");
		DefaultPath<String,ValueModel,String> path2 = 
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
		DefaultPath<?,?,?> path1 = DefaultPath.make("cat.dog");
		DefaultPath<Object,Model,Model> path2 = 
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
		DefaultPath<?,?,?> path1 = DefaultPath.make("mom.dad.*");
		try {
			path1.append(Property.make(String.class, "title"));
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void testToString() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad.*");
		assertTrue(path1.toString().equals("dog.cat.mom.dad.*"));
	}
	
	@Test
	public void resolvePath() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("dog.cat");
		DefaultPath<?,?,?> path3 = path1.resolvePath(path2);
		
		assertTrue(path3.toString().equals("mom.dad"));
		assertTrue(path1.toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void resolveImpossiblePath() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("dog.bird");
		try {
			path1.resolvePath(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void startsWithSuccess() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("dog.cat");
		
		assertTrue(path1.startsWith(path2));
	}	
	
	@Test
	public void startsWithFailure() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("dog.bird");
		
		assertFalse(path1.startsWith(path2));
	}
	
	@Test
	public void startsWithIdentical() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		
		assertTrue(path1.startsWith(path1));
	}
	
	@Test
	public void endsWithSuccess() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("mom.dad");
		
		assertTrue(path1.endsWith(path2));
	}	
	
	@Test
	public void endsWithFailure() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		DefaultPath<?,?,?> path2 = DefaultPath.make("fred.dad");
		
		assertFalse(path1.endsWith(path2));
	}
	
	@Test
	public void endsWithIdentical() {
		DefaultPath<?,?,?> path1 = DefaultPath.make("dog.cat.mom.dad");
		
		assertTrue(path1.endsWith(path1));
	}	

}