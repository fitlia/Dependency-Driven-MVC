package com.google.gwt.ddmvc.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.ddmvc.Utility;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A Path represents a traversible path through a model data.
 * Path is reflectively parameterized by the expected return type of the path.
 * This is entirely logical and may not actually reflect the type of the model
 * at the given path.
 * @author Kevin Dolan
 * 
 * @param <Type> the type of data expected at the address, most commonly Object
 */
public class Path<Type> {
	
	private List<String> path;
	private boolean isTerminal;
	private Class<?> expectedType;
	
	//
	// Factory methods
	//
	
	/**
	 * Create an Object-parameterized path from a pathString
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static Path<Object> make(String pathString) {
		return new Path<Object>(Object.class, pathString);
	}
	
	/**
	 * Create an Model-parameterized path from a pathString
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static Path<Model> makeModel(String pathString) {
		if(pathString.endsWith("$"))
			throw new InvalidPathException("Model paths cannot end in $");
		return new Path<Model>(Model.class, pathString);
	}
	
	/**
	 * Create a Custom-parameterized path from a pathString
	 * @param expectedType - the type to expect from the path
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static <Type> Path<Type> 
			make(Class<Type> expectedType, String pathString) {
		
		if(!Utility.aExtendsB(expectedType, Model.class)) 
			if(!pathString.endsWith("$"))
				throw new InvalidPathException("Paths parameterized by classes" +
						"that do not extend Model must end in $");
		
		return new Path<Type>(expectedType, pathString);
	}
	
	/**
	 * Create a path from a Field
	 * @param <Type> the expected type, packed into the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <Type> Path<Type> make(Field<Type> field) {
		String pathString = field.getPathString();
		return new Path<Type>(field.getFieldClass(), pathString);
	}
	
	/**
	 * Create a path from a pathString followed by a Field
	 * @param <Type> the expected type, packed into the field
	 * @param pathString - the pathString to locate the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <Type> Path<Type>
			make(String pathString, Field<Type> field) {
		
		if(pathString.length() == 0)
			pathString = field.getPathString();
		else
			pathString +=  "." + field.getPathString();
		
		return new Path<Type>(field.getFieldClass(), pathString);
	}
	
	//
	// Constants
	//
	
	/**
	 * This can be used to reference the root without the need to create 
	 * a new path
	 */
	public static final Path<?> ROOT_PATH = new Path<Object>(null, "");
	
	//
	// Validators
	//
	
	/**
	 * Validate a key string, ensure if only contains A-z,0-9.
	 * @param key
	 */
	public static void validateKey(String key) {
		if(!key.matches("[_A-z0-9]+"))
			throw new InvalidPathException("Keys must be non-blank and can only" +
					" contain alphanumeric characters.");
	}
	
	/**
	 * Validate a string, ensure if only contains A-z,0-9 or is *,$
	 * @param key
	 */
	public static void validateKeySpecial(String key) {
		if(!key.matches("[_A-z0-9]+"))
			if(!key.equals("*") && !key.equals("$"))
				throw new InvalidPathException("Keys must be non-blank and can only" +
						" contain alphanumeric characters.");
	}
	
	/**
	 * Validate a path such that the character must be unique and must be at the
	 * end of the path, and must be by itself, if it is present
	 * @param pathString - the path to check
	 * @param special - the special character to assert the previous
	 */
	public static void validateSpecialEnd(String pathString, char special) {
		if(pathString.substring(0, pathString.length() - 1).indexOf(special) >= 0)
			throw new InvalidPathException("Path string cannot contain '" +
					special + "' anywhere by the final character");
		
		if(pathString.endsWith("" + special) && pathString.length() > 1 && 
				!pathString.endsWith("." + special))
			throw new InvalidPathException("The '"+special+"' must be in a path" +
					" field of its own.");
	}
	
	/**
	 * Determine whether or not the path is a valid path-string
	 * @param pathString the string to check
	 */
	public static void validatePathString(String pathString) {
		if(!pathString.matches("[_A-z0-9.*$]*"))
			throw new InvalidPathException("Path string must only contain " +
					"{[A-z],[0-9],'*','.','$'}.");
		
		validateSpecialEnd(pathString, '*');
		
		validateSpecialEnd(pathString, '$');
		
		if(pathString.indexOf("..") >= 0)
			throw new InvalidPathException("The path string must not contain two" +
					"periods in a row.");
		
		if(pathString.endsWith("."))
			throw new InvalidPathException("The path string cannot end with a " +
					"period.");
	}
	
	//
	// Private constructors
	//
	
	/**
	 * Parse a given path.  If the path string is not valid, InvalidPathException
	 * will be thrown.
	 * @param expectedType - the type to expect from the path
	 * @param pathString - the path string to parse
	 */
	private Path(Class<?> expectedType, String pathString) {
		this.expectedType = expectedType;
		if(pathString.length() > 0) {	
			validatePathString(pathString);
			
			isTerminal = 
				pathString.indexOf('$') >= 0 || pathString.indexOf('*') >= 0;
			
			String[] split = pathString.split("[.]");
			path = new LinkedList<String>();
			
			for(String pathField : split)
				path.add(pathField);
		}
		else {
			path = Collections.emptyList();
		}
	}
	
	/**
	 * Instantiate a new path directly from a list, bypassing parse checks
	 * @param expectedType - the type to expect from the path
	 * @param path - the list of fields
	 */
	private Path(Class<?> expectedType, List<String> path) {
		this.expectedType = expectedType;
		this.path = path;
		if(path.size() > 0) {
			String right = rightMost();
			isTerminal = right.equals("*") || right.equals("$");
		}
	}
	
	//
	// Path info
	//
	
	/**
	 * @return the number of fields in this path
	 */
	public int size() {
		return path.size();
	}
	
	/**
	 * @return the expected type of this path
	 */
	public Class<?> getExpectedType() {
		return expectedType;
	}
	
	/**
	 * Get the immediate, leftmost path field from the path
	 * @return the immediate field
	 */
	public String getImmediate() {
		if(path.size() == 0)
			return null;
		return path.get(0);
	}
	
	/**
	 * @return the leftmost field of this path, null if this is an empty path
	 */
	public String leftMost() {
		if(path.size() == 0)
			return null;
		return path.get(0);
	}
	
	/**
	 * @return the rightmost field of this path, null if this is an empty path
	 */
	public String rightMost() {
		if(path.size() == 0)
			return null;
		return path.get(path.size() - 1);
	}
	
	/**
	 * @return whether or not this path is terminated (ends in $ or *)
	 */
	public boolean isTerminal() {
		return isTerminal;
	}
	
	@Override
	public String toString() {
		if(path.size() == 0)
			return "ROOT_PATH";
		
		StringBuilder sb = new StringBuilder();
		for(String field : path)
			sb.append(field + ".");
		if(path.size() > 0)
			sb.replace(sb.length() - 1, sb.length(), "");
		return sb.toString();
	}
	
	//
	// Path manipulators
	//
	
	/**
	 * Get a new path that represents this path, advanced right by one
	 * @return the new path, advanced by one
	 */
	public Path<Type> advance() {
		if(path.size() == 0)
			return null;
		return new Path<Type>(expectedType, path.subList(1, path.size()));
	}
	
	/**
	 * Get a new path that represents this path, with the pathString appended
	 * @param fpathString - the pathString to append
	 * @return the new path
	 */
	public Path<Object> append(String pathString) {		
		return append(make(pathString));
	}
	
	/**
	 * Get a new path that represents this path, with the field appended
	 * @param field - the field to append
	 * @return the new path
	 */
	public <NewType> Path<NewType> append(Field<NewType> field) {
		return append(make(field));
	}
	
	/**
	 * Get a new path that represents this path, with the other path appended
	 * @param other - the path to append
	 */
	public <NewType> Path<NewType> append(Path<NewType> other) {
		if(isTerminal)
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
		
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.addAll(other.path);
		return new Path<NewType>(other.getExpectedType(), newPathList);
	}
	
	/**
	 * Get a path equal to this path, with the $ or * eliminated from the end,
	 * if any
	 * @return a non-terminal path
	 */
	public Path<Object> ignoreTerminal() {
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		
		if(isTerminal())
			newPathList.remove(newPathList.size() - 1);
		
		return new Path<Object>(Object.class, newPathList);
	}	

	/**
	 * Ensures that this path starts with the other path, and if it
	 * does, returns the remainder of the path.  If it does not, 
	 * InvalidPathException will be thrown.
	 * 
	 * Examples:
	 * this: dog.cat.mom.dad
	 * other: dog.cat
	 * returns: mom.dad
	 * 
	 * this: dog.cat.mom.dad
	 * other: dog.bird
	 * throws InvalidPathException
	 * 
	 * @param other - the current path
	 * @return the right-side of target, after current
	 */
	public Path<Type> resolvePath(Path<?> other) {
		Path<Type> a = this;
		Path<?> b = other;
		
		while(b.getImmediate() != null) {
			if(!b.getImmediate().equals(a.getImmediate()))
				throw new InvalidPathException("Path " + this + " does not start " +
						"with " + other + ".  Cannot resolve.");
			a = a.advance();
			b = b.advance();
		}
		
		return a;
	}
	
	//
	// Path property analyzers
	//
	
	/**
	 * @param other - the other path to look at
	 * @return true if this path starts with the other path
	 */
	public boolean startsWith(String other) {
		return startsWith(make(other));
	}
	
	/**
	 * @param other - the other path to look at
	 * @return true if this path starts with the other path
	 */
	public boolean startsWith(Path<?> other) {
		try{
			resolvePath(other);
			return true;
		} catch(InvalidPathException e) {
			return false;
		}
	}
	
	/**
	 * @param other - the other path to look at
	 * @return true if this path ends with the other path
	 */
	public boolean endsWith(String other) {
		return endsWith(make(other));
	}
	
	/**
	 * @param other - the other path to look at
	 * @return true if this path ends with the other path
	 */
	public boolean endsWith(Path<?> other) {
		if(other.size() > size())
			return false;
		
		Path<Type> a = this;
		while(a.size() > other.size())
			a = a.advance();
		
		while(a.getImmediate() != null) {
			if(!a.getImmediate().equals(other.getImmediate()))
				return false;
			
			a = a.advance();
			other = other.advance();
		}
		
		return true;
	}
	
	public boolean equals(Path<?> other) {
		return toString().equals(other.toString());
	}

	public boolean equals(String other) {
		return toString().equals(other);
	}
}
