package com.google.gwt.ddmvc.model.path;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A Path represents a traversible path through a model data.
 * 
 * Path is reflectively parameterized by the expected return type of the path.
 * 
 * This expected return type may not actually reflect the type referred to by
 * a path.  The parameterization of a Path merely provides a means of adding
 * some compile-time type-casting convenience for programmers and run-time
 * checking for earlier error detection.  Use of these features is entirely 
 * optional, but recommended.
 * 
 * In general, if a value at a given path does not match the expected type,
 * a run-time InvalidPathException should be thrown to indicate the mistake.
 * 
 * Paths implement CharSequence.  This allows for convenient method signatures, 
 * which allow auto-casting from Strings (or other char sequences).
 * 
 * PathStrings have the following formatting syntax:
 * Keys are alphanumeric, case-sensitive, underscore-allowed values without
 * spaces.  Most valid java variable names are valid keys.
 * Fields are keys optionally followed by a modifier ('*' or '$').  Fields 
 * without a modifier refer directly to the model and for observation, indicate 
 * Referential Observers.  Fields modified by '*' also refer to a model, but for
 * observation refer to Field Observers.  Fields modified by '$' refer to the
 * value held by the particular model and for observation, indicate Value
 * Observers.
 * Fields can be separated by either a '.' or a '<'.  The '.' represents a 
 * similar relationship to the '.' in Java.  The '<' represents the inverse 
 * relationship, (ie. from). For example, "dog.collie" is the same as 
 * "collie<dog".
 * The '.' relationship is evaluated before the '<' relationship.
 * Only one field in a path can have a modifier, and that must be the terminal
 * fields.
 * Spaces can exist in the PathString, but are ignored when they are compiled to
 * paths.
 * 
 * Examples of equivalent valid paths are shown below:
 * "square$ < rhombus < quadrilateral"
 * "square$ < quadrilateral.rhombus"
 * "rhombus.square$<quadrilateral"
 * 
 * In general, for method signatures that allow multiple Paths or Strings, the
 * comma separating arguments should represent a '<' relationship.
 * 
 * Path has no public constructors, and new Paths can only be created using
 * the static factory methods.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the expected type of value at this path
 * @param <ModelType> the expected type of model at this path
 * @param <ReferenceType> the expected type actually referred to at this path 
 */
public class Path<ValueType, ModelType extends Model, ReferenceType> 
		implements CharSequence {
	
	private List<Field<?,?,?>> path;
	private String stringRepresentation;
	private int current;
	
	//
	// Factory methods
	//
	
	/**
	 * Create a standard-parameterized path from any number of paths,
	 * separated by the '<' relationship.
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static Path<Object,Model,Object> make(CharSequence... paths) {
		
		//TODO - implement me
		return new Path<Object,Model,Object>(Object.class, Model.class, 
				null, pathString);
	}
	
	/**
	 * Create a custom-parameterized path from any number of paths,
	 * separated by the '<' relationship.
	 * Can throw an InvalidPathException if the path is not consistent with
	 * the provided parameterization.
	 * @param <VT> the valueType, packed into the provided class
	 * @param <MT> the modelType, packed into the provided class
	 * @param <RT> the referenceType, packed into the provided class
	 * @param valueType - the value type to use
	 * @param modelType - the model type to use
	 * @param referenceType - the reference type to use
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static <VT,MT extends Model, RT> Path<VT,MT,RT> 
			make(Class<VT> valueType, Class<MT> modelType, Class<RT> referenceType,
			CharSequence... paths) {
		
		//TODO - implement me
		return new Path<VT,MT,RT>(valueType, modelType, referenceType, pathString);
	}
	
	/**
	 * Create a parameterized path from a parameterized path, and any number of 
	 * other paths, separated by the '<' relationship.
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param <RT> the referenceType, packed into the field
	 * @param path - the most terminal path to access
	 * @param paths - the rest of the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static <VT,MT extends Model,RT> Path<VT,MT,RT> 
			make(Path<VT,MT,RT> path, CharSequence... paths) {
		
		//TODO - implement me
		String pathString = field.getPathString();
		return new Path<VT,MT,RT>(field.getValueType(), field.getModelType(),
				field.getReferenceType(), pathString);
	}

	/**
	 * Create a standard-parameterized Value path from any number of paths,
	 * separated by the '<' relationship.
	 * If the most terminal path refers to a model, $ will be appended
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static Path<Object,Model,Object> makeValue(CharSequence... paths) {
		//TODO - implement me
		return makeValue(pathString,null);
	}
	
	/**
	 * Create a custom-parameterized value path from any number of paths,
	 * separated by the '<' relationship.
	 * @param <VT> the valueType, packed into the provided class
	 * @param <MT> the modelType, packed into the provided class
	 * @param valueType - the value type to use
	 * @param modelType - the model type to use
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static <VT,MT extends Model> Path<VT,MT,VT> 
			makeValue(Class<VT> valueType, Class<MT> modelType, 
			CharSequence... pathString) {
		
		//TODO - implement me
		return (Path.make(valueType, modelType, null, pathString))
			.toValuePath();
	}
	
	/**
	 * Create a Value path from a parameterized path, and any number of 
	 * other paths, separated by the '<' relationship.
	 * If the most terminal path refers to a model, $ will be appended
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param path - the most terminal path to access
	 * @param paths - the rest of the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static <VT,MT extends Model> Path<VT,MT,VT> 
			makeValue(Path<VT,MT,?> path, CharSequence... paths) {
		
		//TODO - implement me
		String fieldPathString = "";
		Class<?> valueType = Object.class;
		Class<?> modelType = Model.class;
		
		if(field != null) {
			fieldPathString = field.getPathString();
			valueType = field.getValueType();
			modelType = field.getModelType();
		}
		
		if(pathString.length() == 0)
			pathString = fieldPathString;
		else if(fieldPathString.length() > 0)
			pathString +=  "." + fieldPathString;
		
		return (new Path<VT,MT,VT>((Class<VT>) valueType, (Class<MT>) modelType,
				null, pathString)).toValuePath();
	}
	
	/**
	 * Create a standard-parameterized Model path from any number of paths,
	 * separated by the '<' relationship.
	 * If the most terminal path refers to a value, the $ will be dropped.
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static Path<Object,Model,Model> makeModel(CharSequence... paths) {
		//TODO - implement me
		return makeModel(pathString,null);
	}
	
	/**
	 * Create a custom-parameterized model path from any number of paths,
	 * separated by the '<' relationship.
	 * If the most terminal path refers to a value, the $ will be dropped.
	 * @param <VT> the valueType, packed into the provided class
	 * @param <MT> the modelType, packed into the provided class
	 * @param valueType - the value type to use
	 * @param modelType - the model type to use
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static <VT,MT extends Model> Path<VT,MT,MT> 
			makeModel(Class<VT> valueType, Class<MT> modelType, 
			CharSequence... paths) {
		
		//TODO - implement me
		return (Path.make(valueType, modelType, null, pathString))
			.toModelPath();
	}
	
	/**
	 * Create a Model path from a a parameterized path, and any number of 
	 * other paths, separated by the '<' relationship.
	 * If the most terminal path refers to a value, the $ will be dropped.
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param path - the most terminal path to access
	 * @param paths - the rest of the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	@SuppressWarnings("unchecked")
	public static <VT,MT extends Model> Path<VT,MT,MT> 
			makeModel(Path<VT,MT,?> path, CharSequence... paths) {
		
		//TODO - implement Me
		String fieldPathString = "";
		Class<?> valueType = Object.class;
		Class<?> modelType = Model.class;
		
		if(field != null) {
			fieldPathString = field.getPathString();
			valueType = field.getValueType();
			modelType = field.getModelType();
		}
		
		if(pathString.length() == 0)
			pathString = fieldPathString;
		else if(fieldPathString.length() > 0)
			pathString +=  "." + fieldPathString;
		
		return (new Path<VT,MT,MT>((Class<VT>) valueType, (Class<MT>) modelType,
				null, pathString)).toModelPath();
	}
	
	//
	// Constants
	//
	
	/**
	 * This can be used to reference the root without the need to create 
	 * a new path
	 */
	public static final Path<Object,Model,Object> ROOT_PATH = make("");
	
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
					special + "' anywhere but the final character");
		
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
	 * @param valueType - the expected type of value at this path
	 * @param modelType - the expected type of model at this path
	 * @param referenceType - the expected type actually referred to by this path
	 * 				(should be one of the above)
	 * @param pathString - the path string to parse
	 */
	protected Path(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, String pathString) {
		
		//TODO - parse to list of fields
		List<String> path;
		if(pathString.length() > 0) {	
			validatePathString(pathString);
			
			String[] split = pathString.split("[.]");
			path = new LinkedList<String>();
			
			for(String pathField : split)
				path.add(pathField);
		}
		else {
			path = Collections.emptyList();
		}
		
		this.path = path;
	}
	
	/**
	 * Instantiate a new path directly from a list, bypassing parse checks
	 * @param path - the list of fields, may be null
	 */
	protected Path(List<Field<?,?,?>> path, int current) {
		this.current = current;
		this.path = path;
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
	 * Get the immediate, leftmost path field from the path
	 * @return the immediate field
	 */
	public Field<?,?,?> getImmediate() {
		if(path.size() <= current)
			return null;
		return path.get(current);
	}
	
	/**
	 * @return the rightmost field of this path, null if this is an empty path
	 */
	@SuppressWarnings("unchecked")
	public Field<ValueType, ModelType, ReferenceType> getTerminal() {
		if(path.size() <= current)
			return null;
		return (Field<ValueType, ModelType, ReferenceType>) 
				path.get(path.size() - 1);
	}
	
	/**
	 * @return whether or not this path is terminated (ends in $ or *)
	 */
	public boolean isTerminal() {
		return getTerminal().isTerminal();
	}

	/**
	 * @return whether or not this path is terminated by a $
	 */
	public boolean isValuePath() {
		return getTerminal().isValuePath();
	}
	
	/**
	 * @return whether or not this path is terminated by a *
	 */
	public boolean isFieldPath() {
		return getTerminal().isFieldPath();
	}
	
	/**
	 * @return the expected type of value at this path
	 */
	public Class<ValueType> getValueType() {
		return getTerminal().getValueType();
	}

	/**
	 * @return the expected type of model at this path
	 */
	public Class<ModelType> getModelType() {
		return getTerminal().getModelType();
	}

	/**
	 * @return the expected type actually referred to at this path
	 */
	public Class<ReferenceType> getReferenceType() {
		return getTerminal().getReferenceType();
	}
	
	@Override
	public String toString() {
		if(path.size() == 0)
			return "ROOT_PATH";
		
		StringBuilder sb = new StringBuilder();
		for(int i = current; i < path.size(); i++) {
			Field<?,?,?> field = path.get(i);
			sb.append(field + ".");
		}
		if(path.size() > current)
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
	public Path<ValueType, ModelType, ReferenceType> advance() {		
		if(path.size() == 0)
			return null;
		
		return new Path<ValueType, ModelType, ReferenceType>(path, current + 1);
	}
	
	/**
	 * Get a new path that represents this path, with the pathString appended
	 * @param pathString - the pathString to append
	 * @return the new path
	 */
	public Path<Object,Model,Object> append(String pathString) {		
		return append(make(pathString));
	}
	
	/**
	 * Get a new path that represents this path, with the other path appended
	 * @param <VT> the valueType, packed into the other
	 * @param <MT> the modelType, packed into the other
	 * @param <RT> the referenceType, packed into the other
	 * @param other - the path to append
	 * @return the new path
	 */
	public <VT,MT extends Model,RT> Path<VT,MT,RT> append(Path<VT,MT,RT> other) {
		if(isTerminal())
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
		
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.addAll(other.path);
		return new Path<VT,MT,RT>(other.getValueType(), other.getModelType(),
				other.getReferenceType(), newPathList);
	}
	
	/**
	 * Get a path equal to this path, with the $ or * eliminated from the end,
	 * if any.  Note - this will necessarily make this path refer to a model.
	 * @return a non-terminal path
	 */
	@SuppressWarnings("unchecked")
	public Path<ValueType,ModelType,ModelType> ignoreTerminal() {		
		if(!isTerminal())
			return (Path<ValueType, ModelType, ModelType>) this;
			
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.remove(newPathList.size() - 1);
		
		return new Path<ValueType, ModelType, ModelType>
			(getValueType(), getModelType(), getModelType(), newPathList);
	}
	
	/**
	 * Get a new path that represents this path, with any terminal fields dropped
	 * and $ appended.
	 * @return the new path
	 */
	@SuppressWarnings("unchecked")
	public Path<ValueType, ModelType, ValueType> toValuePath() {
		if(isValuePath())
			return (Path<ValueType, ModelType, ValueType>) this;
		return ignoreTerminal().appendValueField();
	}
	
	/**
	 * Get a new path that represents this path, with $ dropped, if it is present
	 * @return the new path
	 */
	@SuppressWarnings("unchecked")
	public Path<ValueType, ModelType, ModelType> toModelPath() {
		if(isFieldPath())
			return (Path<ValueType, ModelType, ModelType>) this;
		return ignoreTerminal();
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
	public Path<ValueType, ModelType, ReferenceType> 
			resolvePath(Path<?,?,?> other) {
		
		Path<ValueType, ModelType, ReferenceType>  a = this;
		Path<?,?,?> b = other;
		
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
	public boolean startsWith(Path<?,?,?> other) {
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
	public boolean endsWith(Path<?,?,?> other) {
		if(other.size() > size())
			return false;
		
		Path<?,?,?> a = this;
		while(a.size() > other.size())
			a = a.advance();
		
		while((a != null | other != null) && a.getImmediate() != null) {
			if(!a.getImmediate().equals(other.getImmediate()))
				return false;
			
			a = a.advance();
			other = other.advance();
		}
		
		return true;
	}
	
	/**
	 * Checks for equality, only with regards to path, no type checking
	 * @param other - the other path to check
	 * @return true if the paths refer to the same model/scope
	 */
	public boolean equals(Path<?,?,?> other) {
		return toString().equals(other.toString());
	}

	/**
	 * Checks for equality, only with regards to path, no type checking
	 * @param other - the other path to check, as a string
	 * @return true if the paths refer to the same model/scope
	 */
	public boolean equals(String other) {
		return toString().equals(other);
	}
	
	//
	// CharSequence methods
	//
	
	@Override
	public char charAt(int index) {
		return stringRepresentation.charAt(index);
	}

	@Override
	public int length() {
		return stringRepresentation.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return stringRepresentation.subSequence(start, end);
	}
}
