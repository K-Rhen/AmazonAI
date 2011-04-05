package com.pas.cosc322.amazon.debug.memory;

/**
 * A Java memory profiler.
 * 
 * @author Paul
 */
public class MemoryUsage
{
	/**
	 * Calculates (with varying degree of error) the memory used by a specific kind of object.
	 * It is best to call this method multiple times to get an accurate reading.
	 * 
	 * @param <E> the type of object
	 * @param factory the object factory
	 * @return the amount of memory (in bytes) one instance of a class uses
	 */
	public static <E> long calculateMemoryUsage (ObjectFactory<E> factory)
	{
		@SuppressWarnings("unused")
		// creating the first unused object pulls the class definition into memory if it already hasn't been.
		E handle = factory.createObject();
		long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		handle = null;
		
		// basically force garbage collection to run
		System.runFinalization();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();

		mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		handle = factory.createObject();
		
		System.runFinalization();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();
		System.gc();System.gc();System.gc();System.gc();

		mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return mem1 - mem0;
	}

	/**
	 * Prints the memory used by an object.
	 * 
	 * @param <E> the type of class
	 * @param factory the object factory
	 */
	public static <E> void showMemoryUsage (ObjectFactory<E> factory)
	{
		long mem = calculateMemoryUsage(factory);
		System.out.println(factory.getClass().getName() + " produced " + factory.createObject().getClass().getName() + " which took " + mem + " bytes");
	}
}
