package com.google.gwt.ddmvc.model;

/**
 * A SubModel is a Field that stores some type of Model (by means of a 
 * ModelModel)
 *  
 * @author Kevin Dolan
 * 
 * @param <ModelType> - the type of model to store
 */
public class SubModel<ModelType extends Model> 
		extends Field<Object, ModelType, ModelType> {
	
	/**
	 * Instantiate a new SubModel
	 * @param cls - the class of the SubModel
	 * @param key - the key of the field
	 */
	public static <ModelType extends Model> SubModel<ModelType>
			make(Class<ModelType> cls, String key) {
		
		return new SubModel<ModelType>(cls, key);
	}
	
	/**
	 * Instantiate a new SubModel
	 * @param cls - the class of the SubModel
	 * @param key - the key of the field
	 */
	private SubModel(Class<ModelType> cls, String key) {
		super(Object.class, cls, cls, FieldType.MODEL, key);
	}

	@Override
	public Model getModel() {
		return ModelModel.make(getModelType());
	}

	@Override
	public String getPathString() {
		return key;
	}

	@Override
	public boolean isValidModel(Model model) {
		//TODO - hmmm?
		return false;
	}
	
}
