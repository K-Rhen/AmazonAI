package com.pas.cosc322.amazon.search;

/**
 * Represents a cutoff test for a search.
 * 
 * @author Paul
 *
 * @param <S> the type of state
 */
public interface CutoffTest <S extends State<?, ?>>
{
	/**
	 * Evaluates if the search should end at the given state.
	 * 
	 * @param state the state to do the cutoff test for
	 * @param depth the current depth of the search
	 * @param startTime the start time of the search
	 * @return if the search should terminate
	 */
	public boolean cutoffTest (S state, int depth, long startTime);
}
