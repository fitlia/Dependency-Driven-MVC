package com.google.gwt.ddmvc;

/**
 * Some basic static utility methods for shared use
 * @author Kevin Dolan
 */
public class Utility {
	
	/**
	 * Returns true if class a is a descendant of class b
	 * @param a
	 * @param b
	 * @return true if a extends b
	 */
	public static boolean aExtendsB(Class<?> a, Class<?> b) {
		if(a.getName().equals(b.getName()))
			return true;
		
		if(a.getSuperclass() == null)
			return false;
		
		return aExtendsB(a.getSuperclass(), b);
	}
	
}
