package com.google.gwt.ddmvc.model;

import java.util.HashMap;
import java.util.Set;
import com.google.gwt.ddmvc.DDMVC;
import com.google.gwt.ddmvc.Utility;
import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.exception.InvalidPathException;
import com.google.gwt.ddmvc.model.exception.ModelDoesNotExistException;
import com.google.gwt.ddmvc.model.update.ModelDeleted;
import com.google.gwt.ddmvc.model.update.ModelUpdate;
import com.google.gwt.ddmvc.model.update.SetModel;
import com.google.gwt.ddmvc.model.update.SetValue;
import com.google.gwt.ddmvc.model.update.UnknownUpdate;

/**
 * Model objects represent model data and dependencies.
 * 
 * There are three modes of observing a model, referential observers, value
 * observers, and field observers.  Referential observers are only notified
 * when this model is replaced by another model.  Value observes are notified
 * if the value data changes, and are also notified when referential observers
 * are notified.  Field observers respond to changes in any data in the
 * subtree of this model, and also when referential and value observers would
 * be notified.
 * 
 * This is hierarchical as follows:
 * Referential Observers -> Value Observers -> Field Observers
 * 
 * Models can be referenced by paths, either relative to one model, or relative
 * to the root model.  Path syntax is as follows:
 * 
 * fieldKey1.fieldKey2 to reference a model by field keys
 * fieldKey1.fieldKey2.$ to represent the value held by the rightmost field key
 * fieldKey1.fieldKey2.* to reference the subtree of fieldKey2, used only
 * for observation.
 * 
 * Models do not maintain their sets of observers; these are held in DDMVC.
 * The methods within Model related to observers are actually proxy methods
 * to DDMVC methods by the path.
 * 
 * Models can be overridden so that the familiar model interface can be used
 * as a stand-in for many different other types of data access.  However, a
 * lot of the method signatures are only present for convenience and actually
 * converge to a single method.  Methods marked @proxy methodName merely refer
 * to some other methodName in their implementation, so these methods should not
 * be overridden by subclasses.
 * 
 * @author Kevin Dolan
 */
public class Model {

	/**
	 * Represents the three possible levels of an update which could be fired
	 * @author Kevin Dolan
	 */
	public enum UpdateLevel {
		REFERENCE,
		VALUE,
		FIELD
	}
	
	private String key;
	private Model parent;
	private Path<?> path;
	private HashMap<String, Model> childData;
	protected Object value;
	
	/**
	 * Instantiate a new blank model
	 */
	public Model() {
		this.childData = new HashMap<String, Model>();
		calculatePath();
	}
	
	/**
	 * Instantiate a new model with just a value
	 * @param value - the value to attach to this model, can be null
	 */
	public Model(Object value) {
		this.childData = new HashMap<String, Model>();
		this.value = value;
		calculatePath();
	}
	
	//
	//
	// Parent-child Relationships
	//
	//
	
	//
	// This Model
	//
	
	/**
	 * @return this model's key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key associated with this model.
	 * Note - this could potentially break invariants, so it can only
	 * be called by the Model class
	 * @param key - the key to set this model to
	 */
	protected void setKey(String key) {
		Path.validateKey(key);
		this.key = key;
		calculatePath();
	}
	
	/**
	 * Get the path upward from this model to the top model.
	 * This path will be parameterized by the type of this model.
	 * @return the path from the root to this model
	 */
	public Path<?> getPath() {
		return path;
	}
	
	/**
	 * Calculate and set the upward path.
	 */
	protected void calculatePath() {
		if(parent == null || key == null)
			this.path = Path.ROOT_PATH;
		else
			this.path = parent.getPath().append(Path.make(getClass(), key));
		
		for(Model subModel : childData.values())
			subModel.calculatePath();
	}
	
	//
	// Child Models
	//
	
	/**
	 * Determine whether or not this model has any child models at all
	 * @return true if there are any child models
	 */
	public boolean hasChilds() {
		return childData.size() > 0;
	}
	
	/**
	 * Determine whether or not a model-child exists
	 * @param key - the key of the child to check for
	 * @return true if the child exists
	 */
	public boolean hasChild(String key) {
		return childData.containsKey(key); 
	}
	
	/**
	 * Determine whether or not a field path exists.
	 * @param path - the path to check, relative to this model.
	 * @return true if there exists the path
	 * @proxy hasPath(Path)
	 */
	public boolean hasPath(String pathString) {
		return hasPath(Path.make(pathString));
	}
	
	/**
	 * Determine whether or not a field path exists.
	 * Note - This does not pay attention to parameterization whatsoever.  Also,
	 * any terminal fields will be ignored.
	 * @param path - the path to check, relative to this model.
	 * @param field - the field to check past the path.
	 * @return true if there exists the path
	 * @proxy hasPath(Path)
	 */
	public boolean hasPath(String pathString, Field<?> field) {
		return hasPath(Path.make(pathString, field));
	}
	
	/**
	 * Determine whether or not a field path exists.
	 * Note - This does not pay attention to parameterization whatsoever.  Also,
	 * any terminal fields will be ignored.
	 * @param path - the path to check, relative to this model
	 * @return true if there exists the path
	 */
	public boolean hasPath(Path<?> path) {
		path = path.ignoreTerminal();
		
		if(path.getImmediate() == null)
			return true;
		
		if(!hasChild(path.getImmediate()))
			return false;
		
		return getChild(path.getImmediate()).hasPath(path.advance());
	}
	
	/**
	 * Determine whether or not a given path matches the type referred to by the
	 * path.  
	 * If the path refers to a model that does not exist, 
	 * ModelDoesNotExistException will be thrown.
	 * @param pathString - the path to check, relative to this model
	 * @param field - the field past that path to check
	 * @return true if the type of the model/value referred to by the path 
	 */
	public boolean pathIsTypeValid(String pathString, Field<?> field) {
		return pathIsTypeValid(Path.make(pathString, field));
	}
	
	/**
	 * Determine whether or not a given path matches the type referred to by the
	 * path.  
	 * If the path refers to a model that does not exist, 
	 * ModelDoesNotExistException will be thrown.
	 * If the path is a field-path, InvalidPathException will be thrown.
	 * @param path - the path to check, relative to this model
	 * @return true if the type of the model/value referred to by the path 
	 */
	public boolean pathIsTypeValid(Path<?> path) {		
		if(path.isFieldPath())
			throw new InvalidPathException("pathIsTypeValid() cannot be called" +
					" on a field path.");
		
		if(path.getImmediate() == null)
			return Utility.aExtendsB(getClass(), path.getExpectedType());
		
		if(path.getImmediate().equals("$"))
			return Utility.aExtendsB(myValue().getClass(), path.getExpectedType());
		
		if(!hasChild(path.getImmediate()))
			throw new ModelDoesNotExistException(this.path.append(path));
		
		return getChild(path.getImmediate()).pathIsTypeValid(path.advance());
	}
	
	/**
	 * Return the deepest path through this model consistent with the provided
	 * path.
	 * Example - If this model has no children, resolvePath(Path.make("dog")) will
	 * return a blank path.
	 * @param pathString - the path to resolve
	 * @return the resolved path.
	 */
	public Path<?> resolvePath(String pathString) {
		return resolvePath(Path.make(pathString));
	}
	
	/**
	 * Return the deepest path through this model consistent with the provided
	 * path.
	 * Example - If this model has no children, resolvePath(Path.make("dog")) will
	 * return a blank path.
	 * @param pathString - the path to resolve
	 * @param field - the field past the path to resolve
	 * @return the resolved path.
	 */
	public Path<?> resolvePath(String pathString, Field<?> field) {
		return resolvePath(Path.make(pathString, field));
	}
	
	
	/**
	 * Return the deepest path through this model consistent with the provided
	 * path.
	 * Example - If this model has no children, resolvePath(Path.make("dog")) will
	 * return a blank path.
	 * @param path - the path to resolve
	 * @return the resolved path.
	 */
	public Path<?> resolvePath(Path<?> path) {
		if(hasChild(path.getImmediate()))
			return (Path.make(path.getImmediate()))
				.append(getChild(path.getImmediate()).resolvePath(path.advance()));
		
		return Path.make("");
	}
	
	
	/**
	 * Return the child model referenced by a given key.  If no model exists
	 * with that key, it will be created and returned.
	 * @param key - the key to check
	 * @return a model referenced by the key
	 */
	protected Model getChild(String key) {
		Model model = childData.get(key);
		if(model == null) {
			//This will throw InvalidPathException if key is invalid, so we're safe
			model = new Model();
			model.setParent(this);
			model.setKey(key);
			childData.put(key, model);
		}
		return model;
	}
	
	
	/**
	 * Set the child at the given key to a new model.  Observers will be preserved
	 * from the old model, because those are stored by DDMVC and unrelated to
	 * model.
	 * 
	 * If the new model has a parent set, 
	 * ModelOverwriteException will be thrown.
	 * 
	 * Note - this will not send any notifications
	 * 
	 * @param key - the key of the model to replace
	 * @param model - the model to do the replacing
	 */
	protected void setChild(String key, Model model) {		
		if(model.getParent() != null)
			model.getParent().deleteModel(model.getKey());
		
		model.setKey(key);
		model.setParent(this);
		
		childData.put(key, model);
	}
	
	//
	// Parent Models
	//
	
	//
	
	/**
	 * @return the parent of this model
	 */
	public Model getParent() {
		return parent;
	}
	
	
	/**
	 * Set the parent reference to point to a new model.
	 * @param parent - the new parent to set
	 */
	protected void setParent(Model model) {
		parent = model;
		calculatePath();
	}
	
	
	/**
	 * @return the root model of this model
	 */
	public Model getRoot() {
		if(getParent() == null)
			return this;
		return getParent().getRoot();
	}
	
	//
	//
	// Observer methods
	//
	//
	
	//
	// Existence
	//
	
	//
	
	/**
	 * @return true if a change to this model would result in any observers
	 * being notified
	 */
	public boolean hasObservers() {
		return DDMVC.hasObservers(path);
	}
	
	//
	// Getters
	//
	
	//
	
	/**
	 * @return the set of referential observers, unmodifiable
	 */
	public Set<Observer> getReferentialObservers() {
		return DDMVC.getObservers(path);
	}
	
	
	/**
	 * @return the set of value observers, unmodifiable
	 */
	public Set<Observer> getValueObservers() {
		return DDMVC.getObservers(path.append("$"));
	}
	
	
	/**
	 * @return the set of field observers, unmodifiable
	 */
	public Set<Observer> getFieldObservers() {
		return DDMVC.getObservers(path.append("*"));
	}
	
	//
	// Adders
	//
	
	//
	
	/**
	 * Add a referential observer to this model.
	 * A referential observer only pays attention to changes in the model
	 * referenced by a given path.
	 * @param observer - the observer to add, if null will not be added
	 */
	public void addReferentialObserver(Observer observer) {
		if(observer != null)
			DDMVC.addObserver(observer, path);
	}
	
	
	/**
	 * Add a value observer to this model.
	 * A value observer pays attention to changes in the model referenced
	 * by a given path, as well as changes to the value held by a particular
	 * model.
	 * @param observer - the observer to add, if null will not be added
	 */
	public void addValueObserver(Observer observer) {
		if(observer != null)
			DDMVC.addObserver(observer, path.append("$"));
	}
	
	
	/**
	 * Add a field observer to this model
	 * A field observer pays attention to changes in the model referenced
	 * by a given path, changes to the value held by a particular model,
	 * and also changes to any child models of a particular model.
	 * @param observer - the observer to add, if null will not be added
	 */
	public void addFieldObserver(Observer observer) {
		if(observer != null)
			DDMVC.addObserver(observer, path.append("*"));
	}
	
	
	/**
	 * Add an observer to a model's set of observers, according to the
	 * path variable.  If the path ends in a field name, it will
	 * be a reference observer.  If it ends with a '$' it will be a value
	 * observer, and if it ends with a '*' it will be a field observer.
	 * @param observer - the observer to add, if null will not be added
	 * @param pathString - the path (relative to this model) to observe
	 * @proxy addObserver(Observer, Path)
	 */
	public void addObserver(Observer observer, String pathString) {
		addObserver(observer, Path.make(pathString));
	}
	

	/**
	 * Add an observer to a model's set of observers, according to the
	 * path variable.  If the path ends in a field name, it will
	 * be a reference observer.  If it ends with a '$' it will be a value
	 * observer, and if it ends with a '*' it will be a field observer.
	 * @param observer - the observer to add, if null will not be added
	 * @param pathString - the path (relative to this model) to observe
	 * @param field - the field path the path to add the observer to
	 * @proxy addObserver(Observer, Path)
	 */
	public void addObserver(Observer observer, String pathString,
			Field<?> field) {
		
		addObserver(observer, Path.make(pathString, field));
	}
	
	/**
	 * Add an observer to a model's set of observers, according to the
	 * path variable.  If the path ends in a field name, it will
	 * be a reference observer.  If it ends with a '$' it will be a value
	 * observer, and if it ends with a '*' it will be a field observer.
	 * @param observer - the observer to add, if null will not be added
	 * @param path - the path (relative to this model) to observe
	 */
	public void addObserver(Observer observer, Path<?> path) {
		DDMVC.addObserver(observer, this.path.append(path));
	}
	
	//
	// Removals
	//
	
	//
	
	/**
	 * Remove an observer from the set of referential observers
	 * @param observer - the observer to remove
	 */
	public void removeReferentialObserver(Observer observer) {
		DDMVC.removeObserver(observer, path);
	}
	
	
	/**
	 * Remove an observer from the set of value observers
	 * @param observer - the observer to remove
	 */
	public void removeValueObserver(Observer observer) {
		DDMVC.removeObserver(observer, path.append("$"));
	}
	
	
	/**
	 * Remove an observer from the set of field observers
	 * @param observer - the observer to remove
	 */
	public void removeFieldObserver(Observer observer) {
		DDMVC.removeObserver(observer, path.append("*"));
	}
	
	//
	//
	// Accessors
	//
	//
	
	//
	// Value Accessors
	//
	
	//
	
	/**
	 * Get the value associated with this model, for internal use.
	 * By default, this returns the value field, but it can be overridden to
	 * provide some other value.
	 * @return the value associated with this model
	 */
	protected Object myValue() {
		return value;
	}
	
	
	/**
	 * Get the associated value.
	 * @return the value
	 * @proxy getValue(Observer)
	 */
	public Object getValue() {
		return getValue((Observer) null);
	}
	
	
	/**
	 * Get the associated value, and add a value observer.
	 * @param observer - the observer to add, if null will not be added
	 * @return the value associated with this model
	 * @proxy myValue()
	 */
	public Object getValue(Observer observer) {
		addValueObserver(observer);
		return myValue();
	}
	
	
	/**
	 * Get the associated value, by a particular path.
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @return the associated value of the field represented by this path
	 * @proxy getValue(Path, Observer)
	 */
	public Object getValue(String pathString) {
		return getValue(Path.make(pathString), null);
	}
	
	/**
	 * Get the associated value, by a particular path.
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @param field - the field past the path to access
	 * @return the associated value of the field represented by this path
	 * @proxy getValue(Path, Observer)
	 */
	public Object getValue(String pathString, Field<?> field) {
		return getValue(Path.make(pathString, field), null);
	}
	
	
	/**
	 * Get the associated value, by a particular path, as a string, and add
	 * a value observer
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @param observer - the observer to add
	 * @return the associated value of the field represented by this path
	 * @proxy getValue(Path, Observer)
	 */
	public Object getValue(String pathString, Observer observer) {
		return getValue(Path.make(pathString), observer);
	}	
	
	/**
	 * Get the associated value, by a particular path, as a string, and add
	 * a value observer
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @param field - the field past the path to access
	 * @param observer - the observer to add
	 * @return the associated value of the field represented by this path
	 * @proxy getValue(Path, Observer)
	 */
	public Object getValue(String pathString, Field<?> field, Observer observer) {
		return getValue(Path.make(pathString, field), observer);
	}	

	/**
	 * Get the associated value, by a particular path
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @return the associated value of the field represented by this path
	 * @proxy getValue(Path, Observer)
	 */
	public Object getValue(Path<?> path) {
		return getValue(path, null);
	}
	
	
	/**
	 * Get the associated value, by a particular path, and add
	 * a value observer
	 * Note - any terminal fields on the path will be ignored, and subsequently
	 * a value field will be appended to ensure the path refers to a value.
	 * @param pathString - the string to parse for the path
	 * @param observer - the observer to add
	 * @return the associated value of the field represented by this path
	 * @proxy get(Path, Observer)
	 */
	public Object getValue(Path<?> path, Observer observer) {
		if(!path.isValuePath())
			path = path.ignoreTerminal().append("$");
		return get(path, observer);
	}
	
	//
	// Model Accessors
	//
	
	//
	
	/**
	 * Get a model at a given path.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @return the model at the given path
	 * @proxy getModel(Path, Observer)
	 */
	public Model getModel(String pathString) {
		return getModel(Path.make(pathString), null);
	}
	
	/**
	 * Get a model at a given path.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @param field - the field past the path to access
	 * @return the model at the given path
	 * @proxy getModel(Path, Field, Observer)
	 */
	public Model getModel(String pathString, Field<?> field) {
		return getModel(pathString, field, null);
	}
	
	
	/**
	 * Get a model at a given path, and add a reference/field observer.  The type
	 * of observer to add will depend on whether or not the path ends in *.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @return the model at the given path
	 * @proxy getModel(Path, Observer)
	 */
	public Model getModel(String pathString, Observer observer) {
		return getModel(Path.make(pathString), observer);
	}
	
	/**
	 * Get a model at a given path, and add a reference/field observer.  The type
	 * of observer to add will depend on whether or not the path ends in *.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @param field - the field past the path to access
	 * @return the model at the given path
	 * @proxy getModel(Path, Observer)
	 */
	public Model getModel(String pathString, Field<?> field, Observer observer) {
		return getModel(Path.make(pathString, field), observer);
	}
	
	/**
	 * Get a model at a given path, and add a reference/field observer.  The type
	 * of observer to add will depend on whether or not the path ends in *.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @return the model at the given path
	 * @proxy getModel(Path, Observer)
	 */
	public Model getModel(Path<?> path) {
		return getModel(path, null);
	}
	
	/**
	 * Get a model at a given path, and add a reference/field observer.  The type
	 * of observer to add will depend on whether or not the path ends in *.
	 * If the path ends in $, it will be ignored.
	 * @param pathString - the path to the model
	 * @return the model at the given path
	 * @proxy get(Path, Observer)
	 */
	public Model getModel(Path<?> path, Observer observer) {
		
		if(path.isValuePath())
			path = path.ignoreTerminal();
		
		return (Model) get(path, observer);
	}
	
	//
	// Generic Accessors
	//
	
	/**
	 * Get the reference of the path.
	 * @param pathString - the path to the model/value
	 * @return the model/value referred to by the path
	 * @proxy get(Path, Observer)
	 */
	public Object get(String pathString) {
		return get(Path.make(pathString), null);
	}
	
	/**
	 * Get the reference of the path
	 * @param <Type> - the type to return, packed in the Field
	 * @param pathString - the path to the model
	 * @param field - the field past the model to return
	 * @return the model/value referred to by the path
	 * @proxy get(Path, Observer)
	 */
	public <Type> Type get(String pathString, Field<Type> field) {
		return get(Path.make(pathString, field), null);
	}
	
	/**
	 * Get the reference of the path.
	 * @param path - the path to the model/value
	 * @return the model/value referred to by the path
	 * @proxy get(Path, Observer)
	 */
	public <Type> Type get(Path<Type> path) {
		return get(path, null);
	}
	
	/**
	 * Get the reference of the path, and add an observer (type determined by the
	 * path's rightmost field)
	 * @param path - the path to the model/value
	 * @param observer - the observer to add
	 * @return the model/value referred to by the path
	 * @proxy get(Path, Observer)
	 */
	public Object get(String pathString, Observer observer) {
		return get(Path.make(pathString), observer);
	}	
	
	/**
	 * Get the reference of the path, and add an observer (type determined by the
	 * path's rightmost field)
	 * @param <Type> - the type to return, packed in the Field
	 * @param path - the path to the model/value
	 * @param field - the field past the model to access
	 * @param observer - the observer to add
	 * @return the model/value referred to by the path
	 * @proxy get(Path, Observer)
	 */
	public <Type> Type get(String pathString, Field<Type> field, 
			Observer observer) {
		
		return get(Path.make(pathString, field), observer);
	}	
	
	/**
	 * Get the reference of the path, relative to this model.
	 * ModelDoesNotExistException will be thrown in the event a non-existent model
	 * is attempted to be accessed.
	 * If the query ends with *, the model will be returned, and the observer will
	 * be added as a field observer.
	 * If the query ends with $, the associated value will be returns, and the
	 * observer will be added as a value observer.
	 * If the query ends with a field, the model will be returned and the
	 * observer will be added as a referential observer. 
	 * Note - this method will enforce type-safety on the path, and throw
	 * ClassCastException if it cannot be cast.
	 * @param <Type> - the type to return, packed in the Path
	 * @param path - the path associated with this model
	 * @param observer - the observer to add, if null it will not be added
	 * @return the value/model represented by the path
	 */
	@SuppressWarnings("unchecked")
	public <Type> Type get(Path<Type> path, Observer observer) {
		boolean returnModel = false;
		if(path.getImmediate() == null) {
			addReferentialObserver(observer);
			returnModel = true;
		}
		else if(path.getImmediate().equals("*")) {
			addFieldObserver(observer);
			returnModel = true;
		}
		
		if(returnModel) {
			if(!Utility.aExtendsB(getClass(), path.getExpectedType()))
					throw new ClassCastException(getPath().append(path) + 
							" cannot be cast to " + path.getExpectedType());
			return (Type) this;
		}
		
		if(path.getImmediate().equals("$")) {
			if(!Utility.aExtendsB(myValue().getClass(), path.getExpectedType()))
				throw new ClassCastException(getPath().append(path) 
						+ " cannot be cast to " + path.getExpectedType());
			return (Type) getValue(observer);
		}
		
		String key = path.getImmediate();
		if(!hasPath(key))
			throw new ModelDoesNotExistException(getPath().append(key));
		
		return getChild(key).get(path.advance(), observer);
	}
	
	//
	//
	// Update Handling
	//
	//
	
	//
	// Generic update handling
	//
	
	/**
	 * Handle a ModelUpdate request, notify relevant observers.
	 * Any terminal fields in the path will be ignored.
	 * Note - this will attempt to resolve the absolute path in ModelUpdate
	 * with the path to this model.  If it is inconsistent, an 
	 * InvalidPathException will be thrown.
	 * @param update - the update request being processed
	 */
	public void handleUpdate(ModelUpdate update) {
		Path<?> relative = update.getTarget().resolvePath(getPath());
		relative = relative.ignoreTerminal();
		handleUpdateSafe(update, relative);
	}	
	
	
	/**
	 * Handle a model update, with a path relative to this model.  Bypasses
	 * checking if the addresses are consistent, so can only be called by
	 * this class.
	 * If need be, this method will create new child models.
	 * Override this method if you want to affect the way this model processes
	 * updates.
	 * @param update - the update to apply
	 * @param relative - the relative path to pursue
	 */
	protected void handleUpdateSafe(ModelUpdate update, Path<?> relative) {
		if(relative.getImmediate() == null)
			applyUpdate(update);
		else
			getChild(relative.getImmediate())
				.handleUpdateSafe(update, relative.advance());
	}
	
	/**
	 * Blindly apply this update to this model.
	 * Override this method if you want to affect the way this model processes
	 * updates.
	 * @param update - the update to apply
	 */
	protected void applyUpdate(ModelUpdate update) {
		Object result = update.process(value);
		
		if(result.getClass().getName()
				.equals(ModelUpdate.SET_MODEL_TO.class.getName())) {
			
			notifyObservers(update, UpdateLevel.REFERENCE);
			parent.setChild(key, 
					(((ModelUpdate.SET_MODEL_TO) result).getModel()));
		}
		else {
			notifyObservers(update, UpdateLevel.VALUE);
			value = result;
		}
	}
	
	//
	// Set Value
	//
	
	/**
	 * Set the associated value of this model; notify observers of the change
	 * @param value - the value to set
	 * @proxy setValue(Path, Object)
	 */
	public void setValue(Object value) {
		setValue(Path.make(""), value);
	}
	
	/**
	 * Set the value associated with the model referenced by the path, relative
	 * to this model; notify observers of the change
	 * Note - any terminal fields will be ignored
	 * @param pathString - the path relative to this model
	 * @param value - the value to set
	 * @proxy setValue(Path, Object)
	 */
	public void setValue(String pathString, Object value) {
		setValue(Path.make(pathString), value);
	}
	
	/**
	 * Set the value associated with the model referenced by the path, relative
	 * to this model; notify observers of the change
	 * Note - any terminal fields will be ignored
	 * @param pathString - the path relative to this model
	 * @param field - the field past the path to set
	 * @param value - the value to set
	 * @proxy setValue(Path, Object)
	 */
	public void setValue(String pathString, Field<?> field, Object value) {
		setValue(Path.make(pathString, field), value);
	}
	
	/**
	 * Set the value associated with the model referenced by the path, relative
	 * to this model; notify observers of the change
	 * Note - any terminal fields will be ignored
	 * @param path - the path relative to this model
	 * @param value - the value to set
	 * @proxy handleUpdateSafe(SetValue, Path)
	 */
	public void setValue(Path<?> path, Object value) {
		ModelUpdate update = new SetValue(getPath().append(path), value);
		handleUpdate(update);
	}
	
	//
	// Set Model
	//
	
	/**
	 * Set the model reference; notify observers of the change.
	 * @param model - the model to set
	 * @proxy setModel(Path, Model)
	 */
	public void setModel(Model model) {
		setModel(Path.make(""), model);
	}
	
	/**
	 * Set the model reference by the path, relative to this model; 
	 * notify observers of the change
	 * @param model - the model to set
	 * @param pathString - the path relative to this model
	 * @proxy setModel(Path, Model)
	 */
	public void setModel(String pathString, Model model) {
		setModel(Path.make(pathString), model);
	}
	
	/**
	 * Set the model reference by the path, relative to this model; 
	 * notify observers of the change
	 * @param model - the model to set
	 * @param pathString - the path relative to this model
	 * @param field - the field past the path to access
	 * @proxy setModel(Path, Model)
	 */
	public void setModel(String pathString, Field<?> field, Model model) {
		setModel(Path.make(pathString, field), model);
	}
		
	/**
	 * Set the model reference by the path, relative to this model; 
	 * notify observers of the change
	 * @param model - the model to set
	 * @param pathString - the path relative to this model
	 * @param field - the field past the path to access
	 * @proxy handleUpdateSafe(SetModel, Path)
	 */
	public void setModel(Path<?> path, Model model) {
		ModelUpdate update = new SetModel(getPath().append(path), model);
		handleUpdate(update);
	}
	
	//
	// Delete Model
	//
	
	/**
	 * Delete the reference to a model
	 * @param pathString - the path to the model to be deleted
	 * @proxy deleteModel(Path)
	 */
	public void deleteModel(String pathString) {
		deleteModel(Path.make(pathString));
	}
	
	/**
	 * Delete the reference to a model
	 * @param pathString - the path to the model to be deleted
	 * @param field - the field past the path to delete
	 * @proxy deleteModel(Path)
	 */
	public void deleteModel(String pathString, Field<?> field) {
		deleteModel(Path.make(pathString, field));
	}

	/**
	 * Delete the reference to a model
	 * Note - this will cause a loss of all observer lists for this and all
	 * models in this model's subtree.  This means that delete should not
	 * be called unless you're sure you won't want the values again.
	 * @param path - the path to the model to be deleted
	 */
	public void deleteModel(Path<?> path) {
		if(path.isTerminal())
			throw new InvalidPathException("Delete path cannot be terminal.");
		if(path.size() == 0)
			throw new InvalidPathException("Cannot delete a blank path, dummy!");
		else if(path.size() == 1) {
			if(!hasPath(path.getImmediate()))
				throw new ModelDoesNotExistException(getPath().append(path));
			
			Model model = getChild(path.getImmediate());
			model.notifyObservers(new ModelDeleted(model.getPath()),
					UpdateLevel.VALUE);
			childData.remove(path.getImmediate());
		}
		else {
			if(!hasPath(path.getImmediate())) 
				throw new ModelDoesNotExistException(getPath().append(path));
			getChild(path.getImmediate()).deleteModel(path.advance());
		}	
	}
	
	//
	// Explicit updates
	//
	
	/**
	 * Notify the observers of a value change to this model, where the 
	 * update type is not known.  This causes the ModelUpdate UnknownUpdate 
	 * to be passed along.
	 * @proxy update(Path)
	 */
	public void update() {
		update(Path.make(""));
	}	
	
	
	/**
	 * Notify the observers of a value change to a model relative to this one,
	 * by the supplied path-string,  where the update type is not known.  This 
	 * causes the ModelUpdate UnknownUpdate to be passed along.
	 * @param pathString - the path string representing the target model
	 * @proxy update(Path)
	 */
	public void update(String pathString) {
		update(Path.make(pathString));
	}
	
	/**
	 * Notify the observers of a value change to a model relative to this one,
	 * by the supplied path-string,  where the update type is not known.  This 
	 * causes the ModelUpdate UnknownUpdate to be passed along.
	 * @param pathString - the path string representing the target model
	 * @param field - the field past the path to update
	 * @proxy update(Path)
	 */
	public void update(String pathString, Field<?> field) {
		update(Path.make(pathString, field));
	}
		
	/**
	 * Notify the observers of a value change to a model relative to this one,
	 * by the supplied path,  where the update type is not known.  This 
	 * causes the ModelUpdate UnknownUpdate to be passed along.
	 * @param path - the path representing the target model
	 * @proxy handleUpdateSafe(UnknownUpdate, Path)
	 */
	public void update(Path<?> path) {
		ModelUpdate update = new UnknownUpdate(getPath().append(path));
		DDMVC.notifyObservers(update, UpdateLevel.VALUE);
	}
	
	//
	// Notification Sending
	//
	
	/**
	 * Send notification of a model update to all appropriate observers.
	 * Note - this just puts the notifications in the DDMVC's pending
	 * notifications set.  They will not be applied until the next run-loop.
	 * @param update - the update to notify observers of
	 * @param level - the update level
	 */
	public void notifyObservers(ModelUpdate update, UpdateLevel level) {
		DDMVC.notifyObservers(update, level);
	}
	
}
