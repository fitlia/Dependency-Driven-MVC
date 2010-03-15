package com.google.gwt.ddmvc.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.ddmvc.DDMVC;
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
 * @author Kevin Dolan
 */
public class Model {

	/**
	 * Represents the three possible levels of an update which could be fired
	 * @author Kevin Dolan
	 */
	public enum UpdateLevel {
		REFERENTIAL,
		VALUE,
		FIELD
	}
	
	private String key;
	private Model parent;
	private Path path;
	private HashSet<Observer> referentialObservers;
	private HashSet<Observer> valueObservers;
	private HashSet<Observer> fieldObservers;
	private HashMap<String, Model> childData;
	private Object value;
	
	/**
	 * Instantiate a new blank model
	 */
	public Model() {
		init(null, null, null);
	}
	
	/**
	 * Instantiate a new model with just a value
	 * @param value - the value to attach to this model, can be null
	 */
	public Model(Object value) {
		init(null, value, null);
	}
	
	/**
	 * Instantiate a new model
	 * @param parent - the parent of this model, can be null for root
	 * @param value - the value to attach to this model, can be null
	 * @param key - the key to set this model to, can be null but not blank
	 */
	public Model(Model parent, Object value, String key) {		
		init(parent,value,key);
	}
	
	/**
	 * Helper method for the constructors
	 * @param parent
	 * @param value
	 * @param key
	 */
	private void init(Model parent, Object value, String key) {
		if(key != null)
			Path.validateKey(key);
		
		if(parent != null && key == null)
			throw new InvalidPathException("You cannot instantiate a model with" +
					"a parent, but no key.");
		
		this.value = value;
		this.childData = new HashMap<String, Model>();
		
		this.referentialObservers = new HashSet<Observer>();
		this.valueObservers = new HashSet<Observer>();
		this.fieldObservers = new HashSet<Observer>();
		
		this.key = key;
		this.parent = parent;
		
		calculatePath();
	}
	
	
	//PARENT-CHILD RELATIONSHIPS
	
	/**
	 * Determine whether or not a field path exists, by path-string.
	 * Note, will throw an exception if the path ends in $ or *
	 * @param path - the path to check
	 * @return true if there exists the path
	 */
	public boolean hasPath(String pathString) {
		return hasPath(new Path(pathString));
	}
	
	/**
	 * Determine whether or not a field path exists.
	 * Note, if the path ends in $, it will return true if the associated value
	 * is non-null.  If the the path ends in *, it will return true if the model
	 * has any children.
	 * @param path - the path to check
	 * @return true if there exists the path
	 */
	public boolean hasPath(Path path) {
		if(path.getImmediate() == null)
			return true;
		if(path.getImmediate().equals("*"))
			return childData.size() > 0;
		if(path.getImmediate().equals("$"))
			return myValue() != null;
		
		if(!childData.containsKey(path.getImmediate()))
			return false;
		
		return getChild(path.getImmediate()).hasPath(path.advance());
	}
	
	/**
	 * Return the child model referenced by a given key.  If no model exists
	 * with that key, it will be created and returned.
	 * @param key - the key to check
	 * @return a model referenced by the key
	 */
	private Model getChild(String key) {
		Model model = childData.get(key);
		if(model == null) {
			//This will throw InvalidPathException if key is invalid, so we're safe
			model = new Model(this, null, key);
			childData.put(key, model);
		}
		return model;
	}
	
	/**
	 * Set the child at the given key to a new model.  The old model's 
	 * observers will be transferred to the new model, and the parent-child
	 * relationship will be resolved appropriately.
	 * 
	 * If the new model has any observers or a parent set, 
	 * ModelOverwriteException will be thrown.
	 * 
	 * Note - this will not send any notifications.
	 * 
	 * @param key - the key of the model to replace
	 * @param model - the model to do the replacing
	 */
	public void setChild(String key, Model model) {
		Path.validateKey(key);
		
		if(model.getReferentialObservers().size() > 0
				|| model.getValueObservers().size() > 0
				|| model.getFieldObservers().size() > 0)
			throw new ModelOverwriteException("A model cannot be set as a child" +
					"if it already has observers.");
		
		if(model.getParent() != null)
			throw new ModelOverwriteException("A model cannot be set as a child" +
				"if it already has a parent.");
		
		Model current = getChild(key);
		for(Observer observer : current.getReferentialObservers())
			model.addReferentialObserver(observer);
		for(Observer observer : current.getValueObservers())
			model.addValueObserver(observer);
		for(Observer observer : current.getFieldObservers())
			model.addFieldObserver(observer);
		model.setParent(this);
		model.setKey(key);
		
		childData.put(key, model);
	}
	
	/**
	 * @return the parent of this model
	 */
	public Model getParent() {
		return parent;
	}
	
	/**
	 * Set the parent reference to point to a new model.
	 * Note - this could potentially break invariants, so it can only
	 * be called by the Model class
	 * @param parent - the new parent to set
	 */
	private void setParent(Model model) {
		parent = model;
	}
	
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
	private void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Get the path upward from this model to the top model
	 * @return the path from the root to this model
	 */
	public Path getPath() {
		return path;
	}
	
	/**
	 * Calculate and set the upward path.
	 */
	private void calculatePath() {
		if(parent == null)
			this.path = Path.ROOT_PATH;
		else
			this.path = parent.getPath().append(key);
	}
	
	
	//OBSERVER GET/ADD/REMOVE's
	
	//Getters
	/**
	 * @return the set of referential observers, unmodifiable
	 */
	public Set<Observer> getReferentialObservers() {
		return Collections.unmodifiableSet(referentialObservers);
	}
	
	/**
	 * @return the set of value observers, unmodifiable
	 */
	public Set<Observer> getValueObservers() {
		return Collections.unmodifiableSet(valueObservers);
	}
	
	/**
	 * @return the set of field observers, unmodifiable
	 */
	public Set<Observer> getFieldObservers() {
		return Collections.unmodifiableSet(fieldObservers);
	}
	
	//Adders
	/**
	 * Add a referential observer to this model.
	 * A referential observer only pays attention to changes in the model
	 * referenced by a given path.
	 * @param observer - the observer to add, if null will not be added
	 */
	public void addReferentialObserver(Observer observer) {
		if(observer != null)
			referentialObservers.add(observer);
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
			valueObservers.add(observer);
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
			fieldObservers.add(observer);
	}
	
	/**
	 * Add an observer to the model's set of dependents, according to the
	 * path variable.  If the path ends in a field name, it will
	 * be a referential observer.  If it ends with a '$' it will be a value
	 * observer, and if it ends with a '*' it will be a field observer.
	 * Note - if the model does not exist yet, it will be created.
	 * @param observer - the observer to add, if null will not be added
	 * @param path - the path (relative to this model) to observe
	 */
	public void addObserver(Observer observer, Path path) {
		if(path.getImmediate() == null)
			addReferentialObserver(observer);
		else if(path.getImmediate().equals("$"))
			addValueObserver(observer);
		else if(path.getImmediate().equals("*"))
			addFieldObserver(observer);
		else 
			getChild(path.getImmediate())
				.addObserver(observer, path.advance());
	}
	
	//Removals
	/**
	 * Remove an observer from the set of referential observers
	 * @param observer - the observer to remove
	 */
	public void removeReferentialObserver(Observer observer) {
		referentialObservers.remove(observer);
	}
	
	/**
	 * Remove an observer from the set of value observers
	 * @param observer - the observer to remove
	 */
	public void removeValueObserver(Observer observer) {
		valueObservers.remove(observer);
	}
	
	/**
	 * Remove an observer from the set of field observers
	 * @param observer - the observer to remove
	 */
	public void removeFieldObserver(Observer observer) {
		fieldObservers.remove(observer);
	}
	

	//DATA ACCESSORS
	/**
	 * Get the value associated with this model, for internal use
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
	 */
	public Object getValue() {
		return getValue((Observer) null);
	}
	
	/**
	 * Get the associated value, and add a value observer.
	 * @param observer - the observer to add, if null will not be added
	 * @return the value associated with this model
	 */
	public Object getValue(Observer observer) {
		addValueObserver(observer);
		return myValue();
	}
	
	/**
	 * Get the associated value, by a particular path, as a string
	 * @param pathString - the string to parse for the path
	 * @return the associated value of the field represented by this path
	 */
	public Object getValue(String pathString) {
		return getValue(new Path(pathString), null);
	}
	
	/**
	 * Get the associated value, by a particular path, as a string, and add
	 * a value observer
	 * @param pathString - the string to parse for the path
	 * @param observer - the observer to add
	 * @return the associated value of the field represented by this path
	 */
	public Object getValue(String pathString, Observer observer) {
		return getValue(new Path(pathString), observer);
	}
	
	/**
	 * Get the associated value, by a particular path
	 * @param pathString - the string to parse for the path
	 * @return the associated value of the field represented by this path
	 */
	public Object getValue(Path path) {
		return getValue(path, null);
	}
	
	/**
	 * Get the associated value, by a particular path, and add
	 * a value observer
	 * @param pathString - the string to parse for the path
	 * @param observer - the observer to add
	 * @return the associated value of the field represented by this path
	 */
	public Object getValue(Path path, Observer observer) {
		if(path.isTerminal())
			throw new InvalidPathException("Value query paths must be non-terminal.");
		return get(path.append("$"), observer);
	}
	
	/**
	 * Get a model by a given path string
	 * Will throw ModelDoesNotExistException if the model does not exist
	 * @param pathString - the path to the model
	 * @return the model at the given path
	 */
	public Model getModel(String pathString) {
		return getModel(new Path(pathString), null);
	}
	
	/**
	 * Get a model by a given path string, and add a referential observer
	 * Will throw ModelDoesNotExistException if the model does not exist
	 * @param pathString - the path to the model
	 * @param observer - the observer to add
	 * @return the model at the given path
	 */
	public Model getModel(String pathString, Observer observer) {
		return getModel(new Path(pathString), observer);
	}
	
	/**
	 * Get a model by a given path
	 * Will throw ModelDoesNotExistException if the model does not exist
	 * @param path - the path to the model
	 * @return the model at the given path
	 */
	public Model getModel(Path path) {
		return getModel(path, null);
	}
	
	/**
	 * Get a model by a given path, and add a referential observer
	 * Will throw ModelDoesNotExistException if the model does not exist
	 * @param path - the path to the model
	 * @param observer - the observer to add
	 * @return the model at the given path
	 */
	public Model getModel(Path path, Observer observer) {
		if(path.isTerminal())
			throw new InvalidPathException("Model query paths must be non-terminal.");
		return (Model) get(path, observer);
	}
	 
	/**
	 * Get the reference of the path, relative to this model.
	 * @param path - the path to access
	 * @return the value/model represented by the path
	 */
	public Object get(Path path) {
		return get(path, null);
	}
	
	/**
	 * Get the reference of the path, from a raw path-string.
	 * Note - this may throw InvalidPathException if the string is not valid.
	 * @param pathString - the string to parse
	 * @return the value/model represented by the path-string
	 */
	public Object get(String pathString) {
		return get(pathString, null);
	}
	
	/**
	 * Get the reference of the path, from a raw path-string, and add an observer
	 * to the list of observers, according to what type of query this is.
	 * Note - this may throw InvalidPathException if the string is not valid.
	 * @param pathString - the string to parse
	 * @return the value/model represented by the path-string
	 */
	public Object get(String pathString, Observer observer) {
		return get(new Path(pathString), observer);
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
	 * @param path - the path associated with this model
	 * @param observer - the observer to add, if null it will not be added
	 * @return the value/model represented by the path
	 */
	public Object get(Path path, Observer observer) {
		if(path.getImmediate() == null) {
			addReferentialObserver(observer);
			return this;
		}
		if(path.getImmediate().equals("*")) {
			addFieldObserver(observer);
			return this;
		}
		if(path.getImmediate().equals("$"))
			return getValue(observer);
		
		String key = path.getImmediate();
		if(!hasPath(key))
			throw new ModelDoesNotExistException(getPath().append(key));
		
		return getChild(key).get(path.advance(), observer);
	}
	
	
	//UPDATE HANDLING
	
	//Generic update handling
	/**
	 * Handle a ModelUpdate request, notify relevant observers.
	 * Note - this will attempt to resolve the absolute path in ModelUpdate
	 * with the path to this model.  If it is inconsistent, an 
	 * InvalidPathException will be thrown.
	 * @param update - the update request being processed
	 */
	public void handleUpdate(ModelUpdate update) {
		Path relative = update.getTarget().resolvePath(getPath());
		handleUpdateSafe(update, relative);
	}	
	
	/**
	 * Handle a model update, with a path relative to this model.  Bypasses
	 * checking if the addresses are consistent, so can only be called by
	 * this class.
	 * If need be, this method will create new child models.
	 * @param update - the update to apply
	 * @param relative - the relative path to pursue
	 */
	private void handleUpdateSafe(ModelUpdate update, Path relative) {
		if(relative.getImmediate() == null)
			applyUpdate(update);
		else if(relative.getImmediate().equals("$"))
			throw new InvalidPathException("Update path cannot end with '$'.");
		else if(relative.getImmediate().equals("*"))
			throw new InvalidPathException("Update path cannot end with '*'.");
		else
			getChild(relative.getImmediate())
				.handleUpdateSafe(update, relative.advance());
	}
	
	/**
	 * Blindly apply this update to this model.  Does not checking, so must
	 * only be called by this class!
	 * @param update - the update to apply
	 */
	private void applyUpdate(ModelUpdate update) {
		Object result = update.process(value);
		
		if(result.getClass().getName()
				.equals(ModelUpdate.SET_MODEL_TO.class.getName())) {
			
			notifyObservers(update, UpdateLevel.REFERENTIAL);
			parent.setChild(key, 
					(((ModelUpdate.SET_MODEL_TO) result).getModel()));
		}
		else {
			notifyObservers(update, UpdateLevel.VALUE);
			value = result;
		}
	}
	
	//Convenience Handlers
	//Set Value
	/**
	 * Set the associated value of this model, notify observers of the change
	 * @param value - the value to set
	 */
	public void setValue(Object value) {
		setValue(new Path(""), value);
	}
	
	/**
	 * Set the associated of the model referenced by the path-string, relative
	 * to this model, notify observers of the change
	 * Note - because this explicitly sets the data of a field, it is not
	 * necessary, and will throw an exception if the path ends with '$'
	 * @param pathString - the path relative to this model
	 * @param value - the value to set
	 */
	public void setValue(String pathString, Object value) {
		setValue(new Path(pathString), value);
	}
	
	/**
	 * Set the associated of the model referenced by the path, relative
	 * to this model, notify observers of the change
	 * Note - because this explicitly sets the data of a field, it is not
	 * necessary, and will throw an exception if the path ends with '$'
	 * @param path - the path relative to this model
	 * @param value - the value to set
	 */
	public void setValue(Path path, Object value) {
		ModelUpdate update = new SetValue(getPath().append(path), value);
		handleUpdateSafe(update, path);
	}
	
	//Set Model
	/**
	 * Set the model reference, notify observers of the change
	 * @param model - the model to set
	 */
	public void setModel(Model model) {
		setModel(new Path(""), model);
	}
	
	/**
	 * Set the model reference by the path-string, relative
	 * to this model, notify observers of the change
	 * Note - because this explicitly sets the data of a field, it is not
	 * necessary, and will throw an exception if the path ends with '$'
	 * @param model - the model to set
	 * @param pathString - the path relative to this model
	 */
	public void setModel(String pathString, Model model) {
		setModel(new Path(pathString), model);
	}
	
	/**
	 * Set the model reference by the path, relative
	 * to this model, notify observers of the change
	 * Note - because this explicitly sets the data of a field, it is not
	 * necessary, and will throw an exception if the path ends with '$'
	 * @param value - the model to set
	 * @param path - the path relative to this model
	 */
	public void setModel(Path path, Model model) {
		ModelUpdate update = new SetModel(getPath().append(path), model);
		handleUpdateSafe(update, path);
	}
	
	//Delete Model
	/**
	 * Delete the reference to a model
	 * @param pathString - the path to the model to be deleted
	 */
	public void deleteModel(String pathString) {
		deleteModel(new Path(pathString));
	}
	
	/**
	 * Delete the reference to a model
	 * Note - this will cause a loss of all observer lists for this and all
	 * models in this model's subtree.  This means that delete should not
	 * be called unless you're sure you won't want the values again.
	 * @param path - the path to the model to be deleted
	 */
	public void deleteModel(Path path) {
		if(path.size() == 0)
			throw new InvalidPathException("Cannot delete a blank path, dummy!");
		else if(path.size() == 1) {
			if(path.getImmediate().equals("$"))
				throw new InvalidPathException("Delete path cannot end with '$'.");
			if(path.getImmediate().equals("*"))
				throw new InvalidPathException("Delete path cannot end with '*'.");
			if(!hasPath(path.getImmediate()))
				throw new ModelDoesNotExistException(getPath().append(path));
			
			childData.remove(path.getImmediate());
		}
		else {
			if(!hasPath(path.getImmediate())) 
				throw new ModelDoesNotExistException(getPath().append(path));
			getChild(path.getImmediate()).deleteModel(path.advance());
		}	
	}
	
	//Explicit updates
	/**
	 * Notify the observers of a value change to this model, where the 
	 * update type is not known.  This causes the ModelUpdate UnknownUpdate 
	 * to be passed along.
	 */
	public void update() {
		update(new Path(""));
	}	
	
	/**
	 * Notify the observers of a value change to a model relative to this one,
	 * by the supplied path-string,  where the update type is not known.  This 
	 * causes the ModelUpdate UnknownUpdate to be passed along.
	 * @param pathString - the path string representing the target model
	 */
	public void update(String pathString) {
		update(new Path(pathString));
	}
	
	/**
	 * Notify the observers of a value change to a model relative to this one,
	 * by the supplied path,  where the update type is not known.  This 
	 * causes the ModelUpdate UnknownUpdate to be passed along.
	 * @param path - the path representing the target model
	 */
	public void update(Path path) {
		ModelUpdate update = new UnknownUpdate(getPath().append(path));
		getModel(path).notifyObservers(update, UpdateLevel.VALUE);
	}
	
	//Notification sending
	/**
	 * Send notification of a model update to all appropriate observers
	 * @param update - the update to notify observers of
	 * @param level - the update level
	 */
	private void notifyObservers(ModelUpdate update, UpdateLevel level) {		
		if(level == UpdateLevel.REFERENTIAL) {
			DDMVC.addNotify(referentialObservers, update);
			DDMVC.addNotify(valueObservers, update);
			DDMVC.addNotify(fieldObservers, update);
		}
		else if(level == UpdateLevel.VALUE) {
			DDMVC.addNotify(valueObservers, update);
			DDMVC.addNotify(fieldObservers, update);
		}
		else {
			DDMVC.addNotify(fieldObservers, update);
		}
		
		if(parent != null)
			parent.notifyObservers(update, UpdateLevel.FIELD);
	}
	
}
