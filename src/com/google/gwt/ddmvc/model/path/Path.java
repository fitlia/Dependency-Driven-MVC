package com.google.gwt.ddmvc.model.path;

import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;

/**
 * A Path represents a traversible path through a model data.
 * 
 * Paths are parameterized, for run-time type checking and compile-time
 * convenience.  This expected return type may not actually reflect the type 
 * referred to by a path.  Use of these features is entirely optional, but 
 * recommended.
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
 * Once processed, the pathstrings returned by Path's toString method will
 * contain fields separated by only the '.' relationship.
 * 
 * @author Kevin Dolan
 * 
 * @param <ValueType> the expected type of value at this path
 * @param <ModelType> the expected type of model at this path
 * @param <ReferenceType> the expected type actually referred to at this path 
 */
public abstract class Path<ValueType, ModelType extends Model, ReferenceType>
		implements CharSequence {

	/**
	 * The referential depth of a field
	 * 	MODEL -> nothing appended
	 *	VALUE -> $ appended
	 *	FIELD -> * appended
	 */
	public enum ReferenceDepth {
		MODEL,
		VALUE,
		FIELD
	}
	
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
	 * Validate a path such that the character must be unique and must be at the
	 * end of the path, and must be by itself, if it is present
	 * @param pathString - the path to check
	 * @param special - the special character to assert the previous
	 */
	public static void validateSpecialEnd(String pathString, char special) {
		if(pathString.substring(0, pathString.length() - 1).indexOf(special) >= 0)
			throw new InvalidPathException("DefaultPath string cannot contain '" +
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
			throw new InvalidPathException("DefaultPath string must only contain " +
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
	// Field access
	//
	
	/**
	 * Get the most immediate field from the path.
	 * @return the immediate field, null if this is a root
	 */
	public Field<?,?,?> getImmediate() {
		if(isRoot())
			return null;
		return getFieldByIndexSafe(0);
	}
	
	/**
	 * Get the most terminal field from the path.
	 * @return the most terminal field, null if this is a root
	 */
	@SuppressWarnings("unchecked")
	public Field<ValueType, ModelType, ReferenceType> getTerminal() {
		if(isRoot())
			return null;
		return (Field<ValueType, ModelType, ReferenceType>) 
				getFieldByIndexSafe(size() - 1);
	}
	
	/**
	 * Get a particular field by its index.
	 * Will throw IndexOutOfBoundsException if the index is invalid.
	 * @param index - the index to access
	 * @return the field at that index
	 */
	public Field<?,?,?> getFieldByIndex(int index) {
		if(index < 0 || index > size())
			throw new IndexOutOfBoundsException("Index: " + index + 
					", Size: " + size());
		return getFieldByIndexSafe(index);
	}
	
	//
	// Path information
	//
	
	/**
	 * @return the reference depth of this path's most terminal field, null if 
	 * this is a root.
	 */
	public ReferenceDepth getReferenceDepth() {
		return getTerminal().getReferenceDepth();
	}
	
	/**
	 * @return true if this path is a root path (ie size is 0)
	 */
	public boolean isRoot() {
		return size() == 0;
	}
	
	/**
	 * @return whether or not the terminal field is terminated ($ or *),
	 * or false if this is a root.
	 */
	public boolean isTerminated() {
		if(isRoot())
			return false;
		return getReferenceDepth() != ReferenceDepth.MODEL;
	}

	/**
	 * @return whether or not the terminal field is terminated ($ only),
	 * or false if this is a root.
	 */
	public boolean isValuePath() {
		if(isRoot())
			return false;
		return getReferenceDepth() == ReferenceDepth.VALUE;
	}
	
	/**
	 * @return whether or not the terminal field is terminated (* only),
	 * or false if this is a root.
	 */
	public boolean isFieldPath() {
		if(isRoot())
			return false;
		return getReferenceDepth() == ReferenceDepth.FIELD;
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
		if(isRoot())
			return "ROOT_PATH";
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < size(); i++) {
			Field<?,?,?> field = getFieldByIndexSafe(i);
			sb.append(field.toString() + ".");
		}
		sb.replace(sb.length() - 1, sb.length(), "");
		return sb.toString();
	}
	
	//
	// Path manipulators
	//
	
	/**
	 * Get a new path that represents this path, advanced by one.
	 * If this path is a root path, will return null.
	 * @return the new path, advanced by one
	 */
	public Path<ValueType, ModelType, ReferenceType> advance() {
		return null;
	}
	
	/**
	 * Get a new path that represents this path, with the other path appended.
	 * If this path is terminated, will throw InvalidPathExcpetion
	 * @param <VT> the valueType, packed into the other
	 * @param <MT> the modelType, packed into the other
	 * @param <RT> the referenceType, packed into the other
	 * @param other - the path to append
	 * @return the new path
	 */
	protected <VT,MT extends Model,RT> Path<VT,MT,RT> 
			append(Path<VT,MT,RT> other) {
		
		return null;
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
				throw new InvalidPathException("DefaultPath " + this + " does not start " +
						"with " + other + ".  Cannot resolve.");
			a = a.advance();
			b = b.advance();
		}
		
		return a;
	}
	
	//
	// Property analyzers
	//
	
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
		return toString().charAt(index);
	}

	@Override
	public int length() {
		return toString().length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}
	
	//
	// Abstract methods, to define path behavior
	//
	
	/**
	 * @return the number of fields represented by this path
	 */
	public abstract int size();
	
	/**
	 * Get the field at the provided index
	 * Does not need to do any bounds-checking, but should be able to accept
	 * any input such that 0 <= index <= size()
	 * @param index - the index of the field to get
	 * @return the field at that index
	 */
	protected abstract Field<?,?,?> getFieldByIndexSafe(int index);
	
	/**
	 * Convert this path to ReferenceDepth: Model
	 * @return the new path
	 */
	public abstract Path<ValueType,ModelType,ModelType> toModelPath();
	
	/**
	 * Convert this path to ReferenceDepth: Value
	 * @return the new path
	 */
	public abstract Path<ValueType, ModelType, ValueType> toValuePath();
	
	/**
	 * Convert this path to ReferenceDepth: Field
	 * @return the new path
	 */
	public abstract Path<ValueType, ModelType, ModelType> toFieldPath();
}
