package com.google.gwt.ddmvc.model.path;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A DefaultPath represents a traversible path through a model data.
 * 
 * DefaultPath has no public constructors, and new Paths can only be created using
 * the static factory methods.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the expected type of value at this path
 * @param <ModelType> the expected type of model at this path
 * @param <ReferenceType> the expected type actually referred to at this path 
 */
public class DefaultPath<ValueType, ModelType extends Model, ReferenceType> 
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
	public static DefaultPath<Object,Model,Object> make(CharSequence... paths) {
		
		//TODO - implement me
		return new DefaultPath<Object,Model,Object>(Object.class, Model.class, 
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
	public static <VT,MT extends Model, RT> DefaultPath<VT,MT,RT> 
			make(Class<VT> valueType, Class<MT> modelType, Class<RT> referenceType,
			CharSequence... paths) {
		
		//TODO - implement me
		return new DefaultPath<VT,MT,RT>(valueType, modelType, referenceType, pathString);
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
	public static <VT,MT extends Model,RT> DefaultPath<VT,MT,RT> 
			make(DefaultPath<VT,MT,RT> path, CharSequence... paths) {
		
		//TODO - implement me
		String pathString = field.getPathString();
		return new DefaultPath<VT,MT,RT>(field.getValueType(), field.getModelType(),
				field.getReferenceType(), pathString);
	}

	/**
	 * Create a standard-parameterized Value path from any number of paths,
	 * separated by the '<' relationship.
	 * If the most terminal path refers to a model, $ will be appended
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static DefaultPath<Object,Model,Object> makeValue(CharSequence... paths) {
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
	public static <VT,MT extends Model> DefaultPath<VT,MT,VT> 
			makeValue(Class<VT> valueType, Class<MT> modelType, 
			CharSequence... pathString) {
		
		//TODO - implement me
		return (DefaultPath.make(valueType, modelType, null, pathString))
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
	public static <VT,MT extends Model> DefaultPath<VT,MT,VT> 
			makeValue(DefaultPath<VT,MT,?> path, CharSequence... paths) {
		
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
		
		return (new DefaultPath<VT,MT,VT>((Class<VT>) valueType, (Class<MT>) modelType,
				null, pathString)).toValuePath();
	}
	
	/**
	 * Create a standard-parameterized Model path from any number of paths,
	 * separated by the '<' relationship.
	 * If the most terminal path refers to a value, the $ will be dropped.
	 * @param paths - the paths to use (pathStrings will be parsed)
	 * @return the newly created path
	 */
	public static DefaultPath<Object,Model,Model> makeModel(CharSequence... paths) {
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
	public static <VT,MT extends Model> DefaultPath<VT,MT,MT> 
			makeModel(Class<VT> valueType, Class<MT> modelType, 
			CharSequence... paths) {
		
		//TODO - implement me
		return (DefaultPath.make(valueType, modelType, null, pathString))
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
	public static <VT,MT extends Model> DefaultPath<VT,MT,MT> 
			makeModel(DefaultPath<VT,MT,?> path, CharSequence... paths) {
		
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
		
		return (new DefaultPath<VT,MT,MT>((Class<VT>) valueType, (Class<MT>) modelType,
				null, pathString)).toModelPath();
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
	protected DefaultPath(Class<ValueType> valueType, Class<ModelType> modelType, 
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
	protected DefaultPath(List<Field<?,?,?>> path, int current) {
		this.current = current;
		this.path = path;
	}
	
	//
	// DefaultPath info
	//
	
	/**
	 * @return the number of fields in this path
	 */
	public int size() {
		return path.size();
	}
	
	

	
	
	
	//
	// DefaultPath manipulators
	//
	
	/**
	 * Get a new path that represents this path, advanced right by one
	 * @return the new path, advanced by one
	 */
	public DefaultPath<ValueType, ModelType, ReferenceType> advance() {		
		if(path.size() == 0)
			return null;
		
		return new DefaultPath<ValueType, ModelType, ReferenceType>(path, current + 1);
	}
	
	

	
	/**
	 * Get a new path that represents this path, with any terminal fields dropped
	 * and $ appended.
	 * @return the new path
	 */
	@SuppressWarnings("unchecked")
	public DefaultPath<ValueType, ModelType, ValueType> toValuePath() {
		if(isValuePath())
			return (DefaultPath<ValueType, ModelType, ValueType>) this;
		return ignoreTerminal().appendValueField();
	}
	
	/**
	 * Get a new path that represents this path, with $ dropped, if it is present
	 * @return the new path
	 */
	@SuppressWarnings("unchecked")
	public DefaultPath<ValueType, ModelType, ModelType> toModelPath() {
		if(isFieldPath())
			return (DefaultPath<ValueType, ModelType, ModelType>) this;
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
	public DefaultPath<ValueType, ModelType, ReferenceType> 
			resolvePath(DefaultPath<?,?,?> other) {
		
		DefaultPath<ValueType, ModelType, ReferenceType>  a = this;
		DefaultPath<?,?,?> b = other;
		
		while(b.getImmediate() != null) {
			if(!b.getImmediate().equals(a.getImmediate()))
				throw new InvalidPathException("DefaultPath " + this + " does not start " +
						"with " + other + ".  Cannot resolve.");
			a = a.advance();
			b = b.advance();
		}
		
		return a;
	}
	
	//
	// DefaultPath property analyzers
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
	public boolean startsWith(DefaultPath<?,?,?> other) {
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
	public boolean endsWith(DefaultPath<?,?,?> other) {
		if(other.size() > size())
			return false;
		
		DefaultPath<?,?,?> a = this;
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
	public boolean equals(DefaultPath<?,?,?> other) {
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
