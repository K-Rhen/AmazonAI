package com.pas.cosc322.amazon.search;

import java.util.Queue;

/**
 * A representation of a game state
 * 
 * @author Paul
 *
 * @param <A> the Action type
 * @param <M> the MinimaxPlayer type
 */
public interface State <A extends Action, M extends MinimaxPlayer> extends Cloneable
{
	/**
	 * Generates all possible actions that will lead to successor states
	 * for this state.  It returns a queue of actions as the "best" actions
	 * should be examined first.  It is up to the implementation to order
	 * the queue into best-first.
	 * 
	 * @param minimaxPlayer the player to generate actions for
	 * @return a queue of all possible actions leading to successor states for this state
	 */
	public Queue<A> actions (M minimaxPlayer);
	
	/**
	 * Applies an action to the state.
	 * 
	 * @param action the action to apply
	 * @return the new state, or existing altered state
	 */
	public State<A, M> applyAction (A action);
	
	/**
	 * Undos an action to the state.  Results of undoing an action "out of order" is
	 * unspecified.
	 * 
	 * @param action the action to undo
	 * @return the new state, or existing altered state
	 */
	public State<A, M> undoAction (A action);
	
	public int evaluate (M minimaxPlayer);
	
	public boolean cutoffTest (int depth, long startTime);
	
	public State<A, M> clone ();
}
