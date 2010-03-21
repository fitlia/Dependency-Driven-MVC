package com.google.gwt.ddmvc.test;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gwt.ddmvc.model.Path;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

public class PathTest {

	@Test
	public void singleField() {
		Path path1 = new Path("dog");
		assertTrue(path1.getImmediate().equals("dog"));
		assertTrue(path1.advance().getImmediate() == null);
	}
	
	@Test
	public void doubleField() {
		Path path1 = new Path("dog.Cat_1");
		assertTrue(path1.getImmediate().equals("dog"));
		
		Path path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		assertTrue(path2.advance().getImmediate() == null);
	}
	
	@Test
	public void multiField() {
		Path path1 = new Path("dog.Cat_1.candle");
		assertTrue(path1.getImmediate().equals("dog"));
		
		Path path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		
		Path path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("candle"));
		assertTrue(path3.advance().getImmediate() == null);
	}
	
	@Test
	public void multiFieldWithAsterisk() {
		Path path1 = new Path("dog.Cat_1.*");
		assertTrue(path1.getImmediate().equals("dog"));
		
		Path path2 = path1.advance();
		assertTrue(path2.getImmediate().equals("Cat_1"));
		
		Path path3 = path2.advance();
		assertTrue(path3.getImmediate().equals("*"));
		assertTrue(path3.advance().getImmediate() == null);
	}
	
	@Test
	public void blankString() {
		Path path1 = new Path("");
		assertTrue(path1.getImmediate() == null);
		assertTrue(path1.advance() == null);
	}
	
	@Test
	public void asteriskOnly() {
		Path path1 = new Path("*");
		assertTrue(path1.getImmediate().equals("*"));
		assertTrue(path1.advance().getImmediate() == null);
	}
	
	@Test
	public void internalAsteriskField() {
		try {
			new Path("dog.*.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalAsteriskField2() {
		try {
			new Path("dog.*.cat.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInField() {
		try {
			new Path("dog.cat.randy*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskInInternalField() {
		try {
			new Path("dog.cat*.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashOnly() {
		Path path1 = new Path("$");
		assertTrue(path1.getImmediate().equals("$"));
		assertTrue(path1.advance().getImmediate() == null);
	}
	
	@Test
	public void internalCashField() {
		try {
			new Path("dog.$.cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void internalCashField2() {
		try {
			new Path("dog.$.cat.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInField() {
		try {
			new Path("dog.cat.randy$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashInInternalField() {
		try {
			new Path("dog.cat$.randy");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void cashAndAsterisk() {
		try {
			new Path("dog.$.*");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void asteriskAndCash() {
		try {
			new Path("dog.*.$");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void blankField() {
		try {
			new Path("dog..cat");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void endBlank() {
		try {
			new Path("dog.cat.");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void invalidCharacter() {
		try {
			new Path("dog.cat .WhereMyPants");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendKey() {
		Path path1 = new Path("cat");
		Path path2 = path1.append("dog");
		
		assertTrue(path1.getImmediate().equals("cat"));
		assertTrue(path1.advance().getImmediate() == null);
		
		assertTrue(path2.getImmediate().equals("cat"));
		assertTrue(path2.advance().getImmediate().equals("dog"));
		assertTrue(path2.advance().advance().getImmediate() == null);
	}
	
	@Test
	public void appendIllegalKey() {
		Path path1 = new Path("cat");
		try {
			path1.append("dog.weimerhamer");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendIllegalKey2() {
		Path path1 = new Path("cat");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendIllegalKey3() {
		Path path1 = new Path("*");
		try {
			path1.append("dog ");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendToTerminal() {
		Path path1 = new Path("mom.dad.*");
		try {
			path1.append("dog");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAll() {
		Path path1 = new Path("dog.cat");
		Path path2 = new Path("mom.dad");
		assertTrue(path1.append(path2).toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void appendAllToTerminal() {
		Path path1 = new Path("dog.cat.$");
		Path path2 = new Path("mom.dad");
		try {
			path1.append(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendAllToTerminal2() {
		Path path1 = new Path("dog.cat");
		Path path2 = new Path("mom.dad.$");
		Path path3 = path1.append(path2);
		try {
			path3.append("mouse");
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void appendTerminalString() {
		Path path1 = new Path("dog.cat");
		Path path2 = path1.append("$");
		assertTrue(path2.toString().equals("dog.cat.$"));
	}
	
	@Test
	public void testToString() {
		Path path1 = new Path("dog.cat.mom.dad.*");
		assertTrue(path1.toString().equals("dog.cat.mom.dad.*"));
	}
	
	@Test
	public void resolvePath() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("dog.cat");
		Path path3 = path1.resolvePath(path2);
		
		assertTrue(path3.toString().equals("mom.dad"));
		assertTrue(path1.toString().equals("dog.cat.mom.dad"));
	}
	
	@Test
	public void resolveImpossiblePath() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("dog.bird");
		try {
			path1.resolvePath(path2);
			fail();
		} catch(InvalidPathException e) {}
	}
	
	@Test
	public void startsWithSuccess() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("dog.cat");
		
		assertTrue(path1.startsWith(path2));
	}	
	
	@Test
	public void startsWithFailure() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("dog.bird");
		
		assertFalse(path1.startsWith(path2));
	}
	
	@Test
	public void startsWithIdentical() {
		Path path1 = new Path("dog.cat.mom.dad");
		
		assertTrue(path1.startsWith(path1));
	}
	
	@Test
	public void endsWithSuccess() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("mom.dad");
		
		assertTrue(path1.endsWith(path2));
	}	
	
	@Test
	public void endsWithFailure() {
		Path path1 = new Path("dog.cat.mom.dad");
		Path path2 = new Path("fred.dad");
		
		assertFalse(path1.endsWith(path2));
	}
	
	@Test
	public void endsWithIdentical() {
		Path path1 = new Path("dog.cat.mom.dad");
		
		assertTrue(path1.endsWith(path1));
	}	
}
