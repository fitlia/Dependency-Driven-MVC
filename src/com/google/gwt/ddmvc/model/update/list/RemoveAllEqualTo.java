package com.google.gwt.ddmvc.model.update.list;

/**
 * Update to remove all elements from a list that are equal to the provided
 * data object.  Note - this uses the .equals(...) method to determine equality,
 * so if you would prefer to use something else, use RemoveAllThatMatch instead.
 * 
 * @author Kevin Dolan
 */
public class RemoveAllEqualTo extends RemoveAllThatMatch {
	
	/**
	 * The default RemoveAllEqualTo field, used for comparison
	 */
	public static final RemoveAllEqualTo DEFAULT = 
		new RemoveAllEqualTo(null, null);
	
	/**
	 * @param target
	 * @param data - the object to compare the other objects to
	 */
	public RemoveAllEqualTo(String target, Object data) {		
		super(target, new EqualsFilter(data));
	}
	
	private static class EqualsFilter implements ListFilter {

		private Object data;
		
		public EqualsFilter(Object data) {
			this.data = data;
		}
		
		@Override
		public boolean accept(int index, Object o) {
			return data.equals(o);
		}
		
	}
}