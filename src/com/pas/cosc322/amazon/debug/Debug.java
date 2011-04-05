package com.pas.cosc322.amazon.debug;

import com.pas.cosc322.amazon.debug.memory.MemoryUsage;
import com.pas.cosc322.amazon.debug.memory.amazon.AmazonBoardFactory;
import com.pas.cosc322.amazon.debug.memory.amazon.AmazonMoveFactory;

/**
 * Debugging class used for memory profiling.
 * 
 * @author Paul
 */
public class Debug
{
	public static void main (String[] args)
	{
		runMemoryChecking();
	}
	
	public static void runMemoryChecking ()
	{
		AmazonBoardFactory boardFactory = new AmazonBoardFactory();
		AmazonMoveFactory moveFactory = new AmazonMoveFactory();
		
		MemoryUsage.showMemoryUsage(boardFactory);
		MemoryUsage.showMemoryUsage(moveFactory);
	}
}
