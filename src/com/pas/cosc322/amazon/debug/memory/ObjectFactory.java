package com.pas.cosc322.amazon.debug.memory;

/**
 * Basic object factory for object debugging.
 * 
 * @author Paul
 *
 * @param <E> the type of object this factory produces
 */
public interface ObjectFactory <E>
{
	/**
	 * Generates a new instance of the specified class.
	 * 
	 * @return a new instance of the class
	 */
	public E createObject ();
}
