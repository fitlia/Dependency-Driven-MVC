package com.google.gwt.ddmvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.multimap.MultiHashListMap;
import org.multimap.MultiHashMap;
import org.multimap.MultiMap;
import com.google.gwt.ddmvc.controller.Controller;
import com.google.gwt.ddmvc.controller.ServerRequest;
import com.google.gwt.ddmvc.event.AppEvent;
import com.google.gwt.ddmvc.event.Observer;
import com.google.gwt.ddmvc.model.Model;
import com.google.gwt.ddmvc.model.Model.UpdateLevel;
import com.google.gwt.ddmvc.model.path.Field;
import com.google.gwt.ddmvc.model.path.Path;
import com.google.gwt.ddmvc.model.update.Cascade;
import com.google.gwt.ddmvc.model.update.ExceptionComputed;
import com.google.gwt.ddmvc.model.update.ModelUpdate;

/**
 * The DDMVC object is the top-level object for managing the data and run-loop 
 * execution. 
 * 
 * It is a static singleton.
 * 
 * DDMVC maintains the set of observational dependencies.
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
 * Referential Observers <- Value Observers <- Field Observers
 * 
 * @author Kevin Dolan
 */
public class DDMVC {

	private static MultiMap<Observer, ModelUpdate> pendingNotifies;
	private static List<AppEvent> pendingEvents;
	private static MultiMap<String, Controller> subscriptions;
	private static Model dataRoot;
	private static Model observerRoot;
	
	private static final int REFERENCE_OBSERVER_INDEX = 0;
	private static final int VALUE_OBSERVER_INDEX = 1;
	private static final int FIELD_OBSERVER_INDEX = 2;
	
	//Static initialization
	static { init(); }
	
	/**
	 * Initialize all the DDMVC components
	 */
	private static void init() {
		dataRoot = new Model();
		observerRoot = new Model();
		pendingNotifies = new MultiHashListMap<Observer, ModelUpdate>();
		pendingEvents = new ArrayList<AppEvent>();
		subscriptions = new MultiHashMap<String, Controller>();
	}
	
	/**
	 * Get the default data-store instance.
	 * 
	 * The data-store is just a model that has a static reference here in
	 * DDMVC, so that it can remain consistent throughout your application.
	 * 
	 * Generally, there is no need to have any other rooted models, though
	 * there's nothing stopping you from doing that.  It just won't behave
	 * as expected in all circumstanced.  Really, just don't do it!
	 * 
	 * Also, most reasonably public methods of the data root are proxied through
	 * DDMVC, so it is very unlikely that you would ever even need a reference
	 * to it.
	 * 
	 * @return the data root.
	 */
	public static Model getDataRoot() {
		return dataRoot;
	}
	
	//
	//                     
	//  Observer Methods
	//                     
	//
	
	//
	// Observer Existence
	//
	
	/**
	 * Return true if the model at the given path has any observers of any type, 
	 * or any of its parents have any field observers.
	 * Note - any terminal fields will be ignored.
	 * @param pathString - the path to check for observers
	 * @return true if the path has any observers
	 */
	public static boolean hasObservers(String pathString) {
		return hasObservers(Path.make(pathString));
	}
	
	/**
	 * Return true if the model at the given path has any observers of any type, 
	 * or any of its parents have any field observers.
	 * Note - any terminal fields will be ignored.
	 * @param pathString - the path to check for observers
	 * @param field - the field past the path to check
	 * @return true if the path has any observers
	 */
	public static boolean hasObservers(String pathString, Field<?,?,?> field) {
		return hasObservers(Path.make(pathString, field));
	}
	
	/**
	 * Return true if the model at the given path has any observers of any type, 
	 * or any of its parents have any field observers.
	 * Note - any terminal fields will be ignored.
	 * @param path - the path to check for observers
	 * @return true if the path has any observers
	 */
	public static boolean hasObservers(Path<?,?,?> path) {
		path = path.ignoreTerminal();
		
		if(!observerRoot.hasPath(path))
			return hasObservers(
					observerRoot.getModel(observerRoot.resolvePath(path)), true, true);
		else
			return hasObservers(observerRoot.getModel(path), true, false);
		
	}
	
	/**
	 * Determines if any observers should be notified if a given
	 * model changes.
	 * @param observerModel - the observer model to check
	 * @param recursive - if true, will also check field observers upstream
	 * @param fieldOnly - if true, will only check field observers
	 * @param true if there are any observers
	 */
	@SuppressWarnings("unchecked")
	private static boolean hasObservers(Model observerModel, boolean recursive, 
			boolean fieldOnly) {
		
		Set<Observer>[] observers = (Set<Observer>[]) observerModel.getValue();
		
		if(observers != null) {
			if(observers[FIELD_OBSERVER_INDEX].size() > 0)
				return true;
			if(!fieldOnly && (
					observers[REFERENCE_OBSERVER_INDEX].size() > 0
					|| observers[VALUE_OBSERVER_INDEX].size() > 0 ))
				return true;
		}
		
		if(recursive && observerModel.getParent() != null)
			return hasObservers(observerModel.getParent(), true, true);
		
		return false;
	}
	
	//
	// Observer Accessors
	//
	
	/**
	 * Return all observers for a given path.  All terminal fields will be
	 * ignored.
	 * Modifying any set will be reflected in the observation tree.
	 * @param path - the path to access
	 * @param create - if true, this will create anything necessary, otherwise
	 * 				it will just return null if it runs into anything uncreated
	 * @return the sets of observers, packed in an array
	 */
	@SuppressWarnings("unchecked")
	private static Set<Observer>[] getAllObservers(Path path, boolean create) {
		Model observerModel;
		Path modelPath = path.ignoreTerminal();
		if(observerRoot.hasPath(modelPath))
			observerModel = observerRoot.getModel(modelPath);
		else if(create) {
			Set<Observer>[] observers = new Set[3];
			for(int i = 0; i < 3; i++)
				observers[i] = new HashSet<Observer>();
			observerModel = new Model(observers);
			observerRoot.setModel(modelPath, observerModel);
		}
		else {
			return null;
		}
		
		Set<Observer>[] observers = (Set<Observer>[]) observerModel.getValue();
		return observers;
	}
	
	/**
	 * Return the observers for a given path.  The type of observers returned
	 * depends on the path, where a path ending in $ will return value observers,
	 * a path ending in * will return field observers, and anything else will
	 * return reference observers.
	 * Modifying the set will be reflected in the observation tree.
	 * @param path - the path to access
	 * @param create - if true, The set of observers will be created if it 
	 * 				does not exist.
	 * @return the sets of observers
	 */
	private static Set<Observer> getObserversSafe(Path<?,?,?> path, boolean create) {
		Set<Observer>[] observers = getAllObservers(path, create);
		if(observers == null)
			return null;
		
		if(path.endsWith("$"))
			return observers[VALUE_OBSERVER_INDEX];
		else if(path.endsWith("*"))
			return observers[FIELD_OBSERVER_INDEX];
		else
			return observers[REFERENCE_OBSERVER_INDEX];
	}
	
	/**
	 * Return the observers for a given path.  The type of observers returned
	 * depends on the path, where a path ending in $ will return value observers,
	 * a path ending in * will return field observers, and anything else will
	 * return reference observers.
	 * Modifying the set will be reflected in the observation tree.
	 * @param pathString - the path to access
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getObservers(String pathString) {
		return getObservers(Path.make(pathString));
	}
	
	/**
	 * Return the observers for a given path.  The type of observers returned
	 * depends on the path, where a path ending in $ will return value observers,
	 * a path ending in * will return field observers, and anything else will
	 * return reference observers.
	 * Modifying the set will be reflected in the observation tree.
	 * @param pathString - the path to access
	 * @param field - the field to access after the path
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getObservers(String pathString, Field<?,?,?> field) {
		return getObservers(Path.make(pathString, field));
	}
	
	/**
	 * Return the observers for a given path.  The type of observers returned
	 * depends on the path, where a path ending in $ will return value observers,
	 * a path ending in * will return field observers, and anything else will
	 * return reference observers.
	 * The set returned will be unmodifiable
	 * @param path - the path to access
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getObservers(Path<?,?,?> path) {
		Set<Observer> observers = getObserversSafe(path, false);
		if(observers == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(observers);
	}
	
	/**
	 * Return all observers for a given path. All terminal fields will be ignored.
	 * The set returned will be unmodifiable.
	 * @param pathString - the path to access
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getAllObservers(String pathString) {
		return getAllObservers(Path.make(pathString));
	}
	
	/**
	 * Return all observers for a given path. All terminal fields will be ignored.
	 * The set returned will be unmodifiable.
	 * @param pathString - the path to access
	 * @param field - the field to access after the path
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getAllObservers(String pathString,
			Field<?,?,?> field) {
		
		return getAllObservers(Path.make(pathString, field));
	}
	
	/**
	 * Return all observers for a given path. All terminal fields will be ignored.
	 * The set returned will be unmodifiable.
	 * @param path - the path to access
	 * @return the sets of observers, unmodifiable
	 */
	public static Set<Observer> getAllObservers(Path<?,?,?> path) {
		Set<Observer>[] observers = getAllObservers(path, false);
		if(observers == null)
			return Collections.emptySet();
		
		Set<Observer> newSet = new HashSet<Observer>();
		for(Set<Observer> oldSet : observers)
			newSet.addAll(oldSet);
		
		return Collections.unmodifiableSet(newSet);
	}
	
	/**
	 * Get all observers that would be affected by an update of a given level.
	 * All terminal fields will be ignored.
	 * @param pathString - the path at which the update would occur
	 * @param level - the level of update which would occur
	 * @return the set of all observers who would be affected
	 */
	public static Set<Observer> getAllAffected(String pathString, 
			UpdateLevel level) {
		
		return getAllAffected(Path.make(pathString), level);
	}
	
	/**
	 * Get all observers that would be affected by an update of a given level.
	 * All terminal fields will be ignored.
	 * @param pathString - the path at which the update would occur
	 * @param level - the level of update which would occur
	 * @return the set of all observers who would be affected
	 */
	public static Set<Observer> getAllAffected(String pathString, 
			Field<?,?,?> field, UpdateLevel level) {
		
		return getAllAffected(Path.make(pathString, field), level);
	}
	
	/**
	 * Get all observers that would be affected by an update of a given level.
	 * All terminal fields will be ignored.
	 * @param path - the path at which the update would occur
	 * @param level - the level of update which would occur
	 * @return the set of all observers who would be affected
	 */
	public static Set<Observer> getAllAffected(Path<?,?,?> path, UpdateLevel level) {
		path = path.ignoreTerminal();
		
		if(!observerRoot.hasPath(path)) {
			level = UpdateLevel.FIELD;
			path = observerRoot.resolvePath(path);
		}
		
		Set<Observer> result = new HashSet<Observer>();
		packAllAffected(observerRoot.getModel(path), level, result);
		return result;
	}
	
	/**
	 * Pack all observers that would affected into the provided Set
	 * @param observerModel - the observer model to check
	 * @param level - the level of update that would be applied
	 * @param set - the set to pack the observers into
	 */
	@SuppressWarnings("unchecked")
	private static void packAllAffected(Model observerModel, UpdateLevel level, 
			Set<Observer> set) {
		
		Set<Observer>[] observers = (Set<Observer>[]) observerModel.getValue();
		
		if(observers != null) {
			set.addAll(observers[FIELD_OBSERVER_INDEX]);
			if(level != UpdateLevel.FIELD)
				set.addAll(observers[VALUE_OBSERVER_INDEX]);
			if(level == UpdateLevel.REFERENCE)
				set.addAll(observers[REFERENCE_OBSERVER_INDEX]);
		}
		
		if(observerModel.getParent() != null)
			packAllAffected(observerModel.getParent(), UpdateLevel.FIELD, set);
	}
	
	//
	// Observer Addition
	//
	
	/**
	 * Add an observer to the set of observers, according to the path variable.
	 * Note - this makes no attempt to ensure a model is present here
	 * @param observer - the observer to add
	 * @param pathString - the path to add the observer to (defines what type of 
	 * observer it is according to the right-most path field)
	 */
	public static void addObserver(Observer observer, String pathString) {
		addObserver(observer, Path.make(pathString));
	}
	
	/**
	 * Add an observer to the set of observers, according to the path variable.
	 * Note - this makes no attempt to ensure a model is present here
	 * @param observer - the observer to add
	 * @param pathString - the path to add the observer to
	 * @param field - the field past the path to add the observer to, defines what
	 * type of observer it is
	 */
	public static void addObserver(Observer observer, String pathString, 
			Field<?,?,?> field) {
		
		addObserver(observer, Path.make(pathString, field));
	}
	
	/**
	 * Add an observer to the set of observers, according to the path variable.
	 * Note - this makes no attempt to ensure a model is present here
	 * @param observer - the observer to add
	 * @param path - the path to add the observer to (defines what type of 
	 * observer it is according to the right-most path field)
	 */
	public static void addObserver(Observer observer, Path<?,?,?> path) {
		Set<Observer> observers = getObserversSafe(path, true);
		observers.add(observer);
	}
	
	//
	// Observer Removal
	//
	
	/**
	 * Remove an observer from the set of observers, according to the path 
	 * variable.
	 * @param observer - the observer to remove
	 * @param pathString - the path to remove the observer from (defines what type
	 * of observer it is according to the right-most path field)
	 */
	public static void removeObserver(Observer observer, String pathString) {			
		removeObserver(observer, Path.make(pathString));
	}
	
	/**
	 * Remove an observer from the set of observers, according to the path 
	 * variable.
	 * @param observer - the observer to remove
	 * @param pathString - the path to remove the observer from 
	 * @param field - the field to access after the path (defines what type of 
	 * observer it is, either reference or value)
	 */
	public static void removeObserver(Observer observer, String pathString, 
			Field<?,?,?> field) {			
		
		removeObserver(observer, Path.make(pathString, field));
	}
	
	/**
	 * Remove an observer from the set of observers, according to the path 
	 * variable.
	 * @param observer - the observer to add
	 * @param path - the path to add the observer to (defines what type of 
	 * observer it is according to the right-most path field)
	 */
	public static void removeObserver(Observer observer, Path<?,?,?> path) {			
		Set<Observer> observers = getObserversSafe(path, false);
		if(observers != null)	{
			observers.remove(observer);
			cleanUp(observerRoot.getModel(path.ignoreTerminal()));
		}
	}
	
	/**
	 * If this model is a leaf, and if it has no observers in its value, delete it
	 * and recursively work its way up
	 * @param model - the observer model to check
	 */
	private static void cleanUp(Model model) {
		if(model.hasChilds())
			return;
		
		if(!hasObservers(model, false, false)) {
			Model parent = model.getParent();
			parent.deleteModel(model.getKey());
			cleanUp(parent);
		}
	}
	
	//
	//
	// Model Accessor Methods
	//
	//
	
	//
	// Path Methods
	//
	
	/**
	 * Determine whether or not a field path exists.
	 * @param path - the path to check, relative to this model.
	 * @return true if there exists the path
	 */
	public static boolean hasPath(String pathString) {
		return hasPath(Path.make(pathString));
	}

	/**
	 * Determine whether or not a field path exists.
	 * Note - This does not pay attention to parameterization whatsoever.  Also,
	 * any terminal fields will be ignored.
	 * @param path - the path to check, relative to this model.
	 * @param field - the field to check past the path.
	 * @return true if there exists the path
	 */
	public static boolean hasPath(String pathString, Field<?,?,?> field) {
		return hasPath(Path.make(pathString, field));
	}
	
	/**
	 * Determine whether or not a field path exists.
	 * Note - This does not pay attention to parameterization whatsoever.  Also,
	 * any terminal fields will be ignored.
	 * @param path - the path to check, relative to this model
	 * @return true if there exists the path
	 */
	public static boolean hasPath(Path<?,?,?> path) {
		return dataRoot.hasPath(path);
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
	public static boolean pathIsTypeValid(String pathString, Field<?,?,?> field) {
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
	public static boolean pathIsTypeValid(Path<?,?,?> path) {
		return dataRoot.pathIsTypeValid(path);
	}
	
	//
	// Value Accessors
	//
	
	/**
	 * Get the value at a given path
	 * @param pathString - the path to the data model to access
	 * @return the value at the path
	 */
	public static Object getValue(String pathString) {
		return getValue(Path.make(pathString), null);
	}
	
	/**
	 * Get the value at a given path
	 * @param <Type> - the return type, packed into the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @return the value at the path
	 */
	public static <Type> Type getValue(String pathString, Field<Type,?,?> field) {
		return getValue(Path.make(pathString, field), null);
	}
	
	/**
	 * Get the value at a given path and add the observer to the list of observers
	 * @param pathString - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static Object getValue(String pathString, Observer observer) {
		return getValue(Path.make(pathString), observer);
	}	
	
	/**
	 * Get the value at a given path and add the observer to the list of observers
	 * @param <Type> - the return type, packed into the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static <Type> Type getValue(String pathString, Field<Type,?,?> field,
			Observer observer) {
		
		return getValue(Path.make(pathString, field), observer);
	}
	
	/**
	 * Get the value at a given path
	 * @param <Type> - the return type, packed into the field
	 * @param path - the path to the data model to access
	 * @return the value at the path
	 */
	public static <Type> Type getValue(Path<Type,?,?> path) {
		return getValue(path, null);
	}
	
	/**
	 * Get the value at a given path and add the observer to the list of observers
	 * @param <Type> - the return type, packed into the path
	 * @param path - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static <Type> Type getValue(Path<Type,?,?> path, Observer observer) {
		return dataRoot.getValue(path, observer);
	}

	//
	// Model Accessors
	//
	
	/**
	 * Get the Model at a given path
	 * @param pathString - the path to the data model to access
	 * @return the Model at the path
	 */
	public static Model getModel(String pathString) {
		return getModel(Path.make(pathString), null);
	}
	
	/**
	 * Get the Model at a given path
	 * @param <ModelType> - the model type to return, packed in the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @return the Model at the path
	 */
	public static <ModelType extends Model> ModelType
			getModel(String pathString, Field<?,ModelType,?> field) {
		
		return getModel(Path.make(pathString, field), null);
	}
	
	/**
	 * Get the Model at a given path and add the observer to the list of observers
	 * @param pathString - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the Model at the path
	 */
	public static Model getModel(String pathString, Observer observer) {
		return getModel(Path.make(pathString), observer);
	}
	
	/**
	 * Get the Model at a given path and add the observer to the list of observers
	 * @param <ModelType> - the model type to return, packed in the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @param observer - the observer to add
	 * @return the Model at the path
	 */
	public static <ModelType extends Model> ModelType 
			getModel(String pathString, Field<?,ModelType,?> field, 
			Observer observer) {
		
		return getModel(Path.make(pathString, field), observer);
	}
	
	/**
	 * Get the Model at a given paths
	 * @param path - the path to the data model to access
	 * @return the Model at the path
	 */
	public static <ModelType extends Model> ModelType
			getModel(Path<?, ModelType, ?> path) {
		
		return getModel(path, null);
	}
	
	/**
	 * Get the Model at a given path and add the observer to the list of observers
	 * @param <ModelType> - the model type to return, packed in the path
	 * @param path - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the Model at the path
	 */
	public static <ModelType extends Model> ModelType
			getModel(Path<?, ModelType, ?> path, Observer observer) {
		
		return dataRoot.getModel(path, observer);
	}
	
	//
	// Generic Accessors
	//
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param pathString - the path to the data model to access
	 * @return the value at the path
	 */
	public static Object get(String pathString) {
		return get(Path.make(pathString), null);
	}
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param <Type> - the expected type of return value, packed into the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @return the value at the path
	 */
	public static <Type> Type get(String pathString, Field<?,?,Type> field) {
		return get(Path.make(pathString, field), null);
	}
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param pathString - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static Object get(String pathString, Observer observer) {
		return get(Path.make(pathString), observer);
	}
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param <Type> - the expected type of return value, packed into the field
	 * @param pathString - the path to the data model to access
	 * @param field - the field past the path to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static <Type> Type get(String pathString, Field<?,?,Type> field, 
			Observer observer) {
		
		return get(Path.make(pathString, field), observer);
	}
	
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param <Type> - the expected type of return value, packed into the path
	 * @param path - the path to the data model to access
	 * @return the value at the path
	 */
	public static <Type> Type get(Path<?,?,Type> path) {
		return get(path, null);
	}
	
	/**
	 * Get the value referenced by this path, either a value or a model
	 * @param <Type> - the expected type of return value, packed into the path
	 * @param path - the path to the data model to access
	 * @param observer - the observer to add
	 * @return the value at the path
	 */
	public static <Type> Type get(Path<?,?,Type> path, Observer observer) {
		return dataRoot.get(path, observer);
	}
	
	//
	//                     
	// Model Update Methods
	//                     
	//
	
	//
	// Set Value
	//
	
	/**
	 * Set the value of a data-model
	 * @param pathString - the path to the data
	 * @param value - the value to set
	 */
	public static void setValue(String pathString, Object value) {
		setValue(Path.make(pathString), value);
	}
	
	/**
	 * Set the value of a data-model
	 * @param <ValueType> - the expected value type, packed into the path
	 * @param pathString - the path to the data
	 * @param field - the field past the path to access
	 * @param value - the value to set
	 */
	public static <ValueType> void  
			setValue(String pathString, Field<ValueType,?,?> field, ValueType value) {
		
		setValue(Path.make(pathString, field), value);
	}
	
	/**
	 * Set the value of a data-model
	 * @param <ValueType> - the expected value type, packed into the path
	 * @param path - the path to the data
	 * @param value - the value to set
	 */
	public static <ValueType> void 
			setValue(Path<ValueType,?,?> path, ValueType value) {
		
		dataRoot.setValue(path, value);
	}
	
	//
	// Set Model
	//
	
	/**
	 * Set the model at a given path
	 * @param pathString - the path to the model
	 * @param model - the model to set
	 */
	public static void setModel(String pathString, Model model) {
		setModel(Path.make(pathString), model);
	}

	/**
	 * Set the model at a given path
	 * @param <ModelType> - the expected model type, packed into the path
	 * @param pathString - the path to the model
	 * @param model - the model to set
	 */
	public static <ModelType extends Model> void 
			setModel(String pathString, Field<?,ModelType,?> field, ModelType model) {
		
		setModel(Path.make(pathString, field), model);
	}
	
	/**
	 * Set the model at a given path
	 * @param <ModelType> - the expected model type, packed into the path
	 * @param path - the path to the model
	 * @param model - the model to set
	 */
	public static <ModelType extends Model> void 
			setModel(Path<?,ModelType,?> path, ModelType model) {
		
		dataRoot.setModel(path, model);
	}
	
	//
	// Delete Model
	//
	
	/**
	 * Delete the model at a given path
	 * @param pathString - the path to delete
	 */
	public static void deleteModel(String pathString) {
		deleteModel(Path.make(pathString));
	}
	
	/**
	 * Delete the model at a given path
	 * @param pathString - the path to delete
	 */
	public static void deleteModel(String pathString, Field<?,?,?> field) {
		deleteModel(Path.make(pathString, field));
	}
	
	/**
	 * Delete the model at a given path
	 * @param path - the path to delete
	 */
	public static void deleteModel(Path<?,?,?> path) {
		dataRoot.deleteModel(path);
	}
	
	//
	// Explicit Update Handling
	//
	
	/**
	 * Send an UnkownUpdate notification to the observers of a model
	 * @param pathString - the path to the model
	 */
	public static void update(String pathString) {
		update(Path.make(pathString));
	}
	
	/**
	 * Send an UnkownUpdate notification to the observers of a model
	 * @param pathString - the path to the model
	 * @param field - the field past the path to access
	 */
	public static void update(String pathString, Field<?,?,?> field) {
		update(Path.make(pathString, field));
	}
	
	/**
	 * Send an UnkownUpdate notification to the observers of a model
	 * @param path - the path to the model
	 */
	public static void update(Path<?,?,?> path) {
		dataRoot.update(path);
	}
	
	//
	// Generic Update Handling
	//
	
	/**
	 * Handle a single model update
	 * @param update - the update to apply
	 */
	public static void handleUpdate(ModelUpdate update) {
		dataRoot.handleUpdate(update);
	}
	
	/**
	 * Process a list of ModelUpdate objects
	 * @param updates - the updates to be applied
	 */
	public static void handleUpdates(List<ModelUpdate> updates) {
		for(ModelUpdate update : updates)
			handleUpdate(update);
	}
	
	//
	// Notification Dissemination
	//
	
	/**
	 * Notify any observers of a particular model of a particular change, the path
	 * will come from the ModelUpdate
	 * @param update - the update to notify about
	 * @param level - the level of the update to apply
	 */
	public static void notifyObservers(ModelUpdate update, 
			UpdateLevel level) {

		Set<Observer> toBeNotified = getAllAffected(update.getTarget(), level);
		for(Observer observer : toBeNotified)
			addNotify(observer, update);
	}
	
	/**
	 * Add an observer to be notified at the next run loop
	 * @param observer - the observer to be notified
	 * @param update - the update that caused this notification
	 */
	public static void addNotify(Observer observer, ModelUpdate update) {
		pendingNotifies.put(observer, update);
	}
	
	/**
	 * Add a set of observers to be notified at the next run loop
	 * @param observers - the set of observers
	 * @param update - the update that caused this notification
	 */
	public static void addNotify(Set<Observer> observers, ModelUpdate update) {
		for(Observer observer : observers)
			addNotify(observer, update);
	}
	
	//
	//                     
	//  Event Methods
	//                     
	//
	
	/**
	 * Notify a particular controller whenever a particular type of event is
	 * fired.
	 * @param eventType - the class of the event to listen to
	 * @param controller - the controller to notify when the event is fired.
	 */
	public static void subscribeToEvent(Class<? extends AppEvent> eventType, 
			Controller controller) {
		
		subscriptions.put(eventType.getName(), controller);
	}
	
	/**
	 * Fire an event to any subscribed controllers, on the next run-loop.
	 * @param event - the event to fire
	 */
	public static void fireEvent(AppEvent event) {
		pendingEvents.add(event);
	}
	
	/**
	 * Execute all controllers subscribed to a particular event
	 * @param event - the event to respond to
	 * @param request - the list of all requests encountered
	 */
	private static List<ServerRequest> handleEvent(AppEvent event) {
		List<ServerRequest> requests = new ArrayList<ServerRequest>();
		Collection<Controller> controllers = 
			subscriptions.get(event.getClass().getName());
		for(Controller controller : controllers)
			requests.add(controller.respondToEvent(event));
		return requests;
	}
	
	/**
	 * Perform the run-loop, should generally not be called explicitly
	 * @return the list of all exceptions encountered during the run-loop
	 */
	public static List<RunLoopException> runLoop() {
		List<ServerRequest> requests = new ArrayList<ServerRequest>();
		while(pendingEvents.size() > 0) {
			List<AppEvent> events = pendingEvents;
			pendingEvents = new ArrayList<AppEvent>();
			
			for(AppEvent event : events)
				requests.addAll(handleEvent(event));
		}
		
		//TODO - send out the requests, please
		
		MultiHashMap<Observer, ModelUpdate> freeNotifies = 
			new MultiHashMap<Observer, ModelUpdate>();
		
		List<RunLoopException> exceptions = new ArrayList<RunLoopException>();
		
		//PendingNotifies will build up with notifications as we edit values.
		int iteration = 0;
		while(pendingNotifies.size() > 0) {
			//Extracts the notifications we will handle now, and reset pending
			Set<Map.Entry<Observer, Collection<ModelUpdate>>> notifies = 
				pendingNotifies.entrySet();
			pendingNotifies = new MultiHashListMap<Observer, ModelUpdate>();
			
			//Iterate through all of the current notifications
			for(Map.Entry<Observer, Collection<ModelUpdate>> entry : notifies) {
				Observer observer = entry.getKey();
				
				if(!observer.hasObservers())
					freeNotifies.put(entry.getKey(), entry.getValue());
				else {
					//Notify the model of a change
					try { 
						observer.modelChanged(entry.getValue());
						//Cascade the update to its dependents (next loop)
						observer.notifyObservers(new Cascade(observer.getPath()), 
								Model.UpdateLevel.VALUE);
					} catch(Exception e) {
						//Cascade the exception update to its dependents (next loop)
						observer.notifyObservers(
								new ExceptionComputed( observer.getPath(), e), 
								Model.UpdateLevel.VALUE);
						exceptions.add(new RunLoopException(e, observer, iteration));
					}
				}
			}
			iteration++;
		}
		
		//Now just tie up the loose ends!
		for(Map.Entry<Observer, Collection<ModelUpdate>> free 
				: freeNotifies.entrySet()) {
			
			try {
				free.getKey().modelChanged(free.getValue()); 
			} catch(Exception e) {
				exceptions.add(new RunLoopException(e, free.getKey(), iteration));
			}
		}
	
		return exceptions;
	}
	
	/**
	 * Reset the state of the DDMVC to initialization
	 */
	public static void reset() {
		init();
	}
}
