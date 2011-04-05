package com.pas.cosc322.amazon.search;


/**
 * Simple struct for search results
 * 
 * @author Paul
 */
class SearchResult <A extends Action>
{
	/** the value associated with an action */
	public int v;
	/** the action (of some depth) related to the value */
	public A action;

	public SearchResult (int v, A action)
	{
		this.v = v;
		this.action = action;
	}
	
	/**
	 * Modifies this instance to contain the max of the two SearchResults.
	 * If the other instance has a higher v value, this instance will carry
	 * that new v value and corresponding action.
	 * 
	 * @param other the other result
	 */
	public void max (SearchResult<A> other)
	{
		if (other.v > v)
		{
			v = other.v;
			action = other.action;
		}
	}
}
