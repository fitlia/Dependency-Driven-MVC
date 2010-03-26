package com.google.gwt.ddmvc.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A Path represents a traversible path through a model data.
 * 
 * Path is reflectively parameterized by the expected return type of the path.
 * 
 * This expected return type may not actually reflect the type referred to by
 * a path.  The parameterization of a Path merely provides a means of adding
 * some compile-time type-casting convenience for programmers.  Use of these
 * features is entirely optional, but recommended.
 * 
 * In general, if a value at a given path does not match the expected type,
 * a run-time InvalidPathException should be thrown to indicate the mistake.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the expected type of value at this path
 * @param <ModelType> the expected type of model at this path
 * @param <ReferenceType> the expected type actually referred to at this path 
 * 				(should be one of the above)
 */
public class Path<ValueType, ModelType extends Model, ReferenceType> {
	
	private List<String> path;
	private boolean isTerminal;
	private boolean isValuePath;
	private boolean isFieldPath;
	private Class<ValueType> valueType;
	private Class<ModelType> modelType;
	private Class<ReferenceType> referenceType;
	
	//
	// Factory methods
	//
	
	/**
	 * Create a standard-parameterized path from a pathString
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static Path<Object,Model,Object> make(String pathString) {
		return new Path<Object,Model,Object>(Object.class, Model.class, 
				Object.class, pathString);
	}
	
	/**
	 * Create a path from a Field
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param <RT> the referenceType, packed into the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <VT,MT extends Model,RT> Path<VT,MT,RT> 
			make(Field<VT,MT,RT> field) {
		
		String pathString = field.getPathString();
		return new Path<VT,MT,RT>(field.getValueType(), field.getModelType(),
				field.getReferenceType(), pathString);
	}
	
	/**
	 * Create a path from a pathString followed by a Field
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param <RT> the referenceType, packed into the field
	 * @param pathString - the pathString to locate the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <VT,MT extends Model,RT> Path<VT,MT,RT> 
			make(String pathString, Field<VT,MT,RT> field) {
		
		if(pathString.length() == 0)
			pathString = field.getPathString();
		else
			pathString +=  "." + field.getPathString();
		
		return new Path<VT,MT,RT>(field.getValueType(), field.getModelType(),
				field.getReferenceType(), pathString);
	}
	
	/**
	 * Create a standard-parameterized Value path from a pathString
	 * If the path refers to a model, $ will be appended
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static Path<Object,Model,Object> makeValue(String pathString) {
		return makeValue(pathString,null);
	}
	
	/**
	 * Create a Value path from a Field
	 * If the field refers to a model, $ will be appended
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <VT,MT extends Model> Path<VT,MT,VT> 
			makeValue(Field<VT,MT,?> field) {
		
		return makeValue("",field);
	}
	
	/**
	 * Create a Value path from a pathString followed by a Field
	 * If the field refers to a model, $ will be appended
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param pathString - the pathString to locate the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	@SuppressWarnings("unchecked")
	public static <VT,MT extends Model> Path<VT,MT,VT> 
			makeValue(String pathString, Field<VT,MT,?> field) {
		
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
				(Class<VT>) valueType, pathString)).toValuePath();
	}
	
	/**
	 * Create a standard-parameterized Model path from a pathString
	 * If the paths refers to a value, the $ will be dropped
	 * @param pathString - the pathString to parse
	 * @return the newly created path
	 */
	public static Path<Object,Model,Model> makeModel(String pathString) {
		return makeModel(pathString,null);
	}
	
	/**
	 * Create a Model path from a Field
	 * If the field refers to a value, the $ will be dropped
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	public static <VT,MT extends Model> Path<VT,MT,MT> 
			makeModel(Field<VT,MT,?> field) {
		
		return makeModel("",field);
	}
	
	/**
	 * Create a Model path from a pathString followed by a Field
	 * If the field refers to a value, the $ will be dropped
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param pathString - the pathString to locate the field
	 * @param field - the field to access
	 * @return the newly created path
	 */
	@SuppressWarnings("unchecked")
	public static <VT,MT extends Model> Path<VT,MT,MT> 
			makeModel(String pathString, Field<VT,MT,?> field) {
		
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
				(Class<MT>) modelType, pathString)).toModelPath();
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
	private Path(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, String pathString) {
		
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
		init(valueType, modelType, referenceType, path);
	}
	
	/**
	 * Instantiate a new path directly from a list, bypassing parse checks
	 * @param valueType - the expected type of value at this path
	 * @param modelType - the expected type of model at this path
	 * @param referenceType - the expected type actually referred to by this path
	 * 				(should be one of the above)
	 * @param path - the list of fields
	 */
	private Path(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, List<String> path) {
		init(valueType, modelType, referenceType, path);
	}
	
	private void init(Class<ValueType> valueType, Class<ModelType> modelType, 
			Class<ReferenceType> referenceType, List<String> path) {
		
		if(valueType.isInterface()
				|| modelType.isInterface()
				|| referenceType.isInterface())
			throw new IllegalArgumentException("Types cannot be an interface.");
		
		if(!referenceType.equals(valueType)
				&& !referenceType.equals(modelType))
			throw new IllegalArgumentException("ReferenceType must be either" +
					" ValueType or ModelType.");
		
		this.valueType = valueType;
		this.modelType = modelType;
		this.referenceType = referenceType;
		
		this.path = path;
		if(path.size() > 0) {
			String right = rightMost();
			isValuePath = right.equals("$");
			isFieldPath = right.equals("*");
			isTerminal = isValuePath || isFieldPath;
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

	/**
	 * @return whether or not this path is terminated by a $
	 */
	public boolean isValuePath() {
		return isValuePath;
	}
	
	/**
	 * @return whether or not this path is terminated by a *
	 */
	public boolean isFieldPath() {
		return isFieldPath;
	}
	
	/**
	 * @return the expected type of value at this path
	 */
	public Class<ValueType> getValueType() {
		return valueType;
	}

	/**
	 * @return the expected type of model at this path
	 */
	public Class<ModelType> getModelType() {
		return modelType;
	}

	/**
	 * @return the expected type actually referred to at this path
	 */
	public Class<ReferenceType> getReferenceType() {
		return referenceType;
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
	public Path<ValueType, ModelType, ReferenceType> advance() {
		if(path.size() == 0)
			return null;
		return new Path<ValueType, ModelType, ReferenceType>(
			getValueType(), getModelType(), getReferenceType(), 
			path.subList(1, path.size()));
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
	 * Get a new path that represents this path, with the field appended
	 * @param <VT> the valueType, packed into the field
	 * @param <MT> the modelType, packed into the field
	 * @param <RT> the referenceType, packed into the field
	 * @param field - the field to append
	 * @return the new path
	 */
	public <VT,MT extends Model,RT> Path<VT,MT,RT> append(Field<VT,MT,RT> field) {
		
		return append(make(field));
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
		if(isTerminal)
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
		
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.addAll(other.path);
		return new Path<VT,MT,RT>(other.getValueType(), other.getModelType(),
				other.getReferenceType(), newPathList);
	}
	
	/**
	 * Get a new path that represents this path, with $ appended.
	 * If this is a terminal path, an exception will be thrown.
	 * @return the new path
	 */
	public Path<ValueType, ModelType, ValueType> appendValueField() {
		if(isTerminal)
			throw new InvalidPathException("It is illegal to append a field to " +
					"a terminated path.");
		
		List<String> newPathList = new LinkedList<String>();
		newPathList.addAll(path);
		newPathList.add("$");
		return new Path<ValueType, ModelType, ValueType>
			(getValueType(), getModelType(), getValueType(), newPathList);
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
		
		while(a.getImmediate() != null) {
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
}
