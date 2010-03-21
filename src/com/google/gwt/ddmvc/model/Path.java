package com.google.gwt.ddmvc.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A Path represents a traversible path through a model data
 * @author Kevin Dolan
 */
public class Path {
	
	private List<String> path;
	private Path advancedPath;
	private boolean isTerminal;
	
	/**
	 * This can be used to reference the root without the need to create 
	 * a new path
	 */
	public static final Path ROOT_PATH = new Path("");
	
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
	
	/**
	 * Parse a given path.  If the path string is not valid, InvalidPathException
	 * will be thrown.
	 * @param pathString - the path string to parse
	 */
	public Path(String pathString) {
		if(pathString.length() > 0) {	
			validatePathString(pathString);
			
			isTerminal = 
				pathString.indexOf('$') >= 0 || pathString.indexOf('*') >= 0;
			
			String[] split = pathString.split("[.]");
			path = new LinkedList<String>();
			
			for(String pathField : split)
				path.add(pathField);
			
			if(path.size() > 0)
				advancedPath = new Path(path.subList(1, path.size()));
		}
		else {
			path = Collections.emptyList();
		}
	}
	
	/**
	 * Instantiate a new path directly from a list, bypassing parse checks
	 * @param path - the list of fields
	 */
	private Path(List<String> path) {
		this.path = path;
		if(path.size() > 0) {
			String right = rightMost();
			isTerminal = right.equals("*") || right.equals("$");
		}
		if(path.size() > 0)
			advancedPath = new Path(path.subList(1, path.size()));
	}

	/**
	 * @return the number of fields in this path
	 */
	public int size() {
		return path.size();
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
	 * Get a new path that represents this path, advanced right by one
	 * @return the new path, advanced by one
	 */
	public Path advance() {
		return advancedPath;
	}
	
	/**
	 * Get a new path that represents this path, with the field appended
	 * @param field - the field to append
	 */
	public Path append(String field) {
		if(isTerminal)
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
			
		validateKeySpecial(field);
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.add(field);
		return new Path(newPathList);
	}
	
	/**
	 * Get a new path that represents this path, with the other path appended
	 * @param other - the path to append
	 */
	public Path append(Path other) {
		if(isTerminal)
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
		
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.addAll(other.path);
		return new Path(newPathList);
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
	public Path resolvePath(Path other) {
		Path a = this;
		Path b = other;
		
		while(b.getImmediate() != null) {
			if(!b.getImmediate().equals(a.getImmediate()))
				throw new InvalidPathException("Path " + this + " does not start " +
						"with " + other + ".  Cannot resolve.");
			a = a.advance();
			b = b.advance();
		}
		
		return a;
	}
	
	/**
	 * @param other - the other path to look at
	 * @return true if this path starts with the other path
	 */
	public boolean startsWith(Path other) {
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
	public boolean endsWith(Path other) {
		if(other.size() > size())
			return false;
		
		Path a = this;
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
	
	public boolean equals(Path other) {
		return toString().equals(other.toString());
	}
	
	public boolean equals(String other) {
		return toString().equals(other);
	}
}
