package com.pas.cosc322.amazon.board;

import static com.pas.cosc322.amazon.main.ProjectConstants.minMemory;
import static com.pas.cosc322.amazon.main.ProjectConstants.searchTime;

import com.pas.cosc322.amazon.search.CutoffTest;

/**
 * The CutoffTest for Amazons.  Consists only for memory and time checking.
 * 
 * @author Paul
 * @author Sam
 * @author Andrew
 */
public class AmazonCutoffTest implements CutoffTest<AmazonBoard>
{
	@Override
	public boolean cutoffTest(AmazonBoard state, int depth, long startTime)
	{
		//  watch memory usage
		if (Runtime.getRuntime().freeMemory() <= minMemory)
		{
			return true;
		}
		
		// watch the time
		long time = System.currentTimeMillis() - startTime;
		if (time >= searchTime)
		{
			return true;
		}
		
		return false;
	}
}
