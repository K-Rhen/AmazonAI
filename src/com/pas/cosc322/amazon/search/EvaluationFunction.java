package com.pas.cosc322.amazon.search;

/**
 * Evaluation function for a specific type of state.
 * 
 * @author Paul
 *
 * @param <S> the state type
 * @param <M> the minimax player type
 */
public interface EvaluationFunction <S extends State<?, ?>, M extends MinimaxPlayer>
{
	/**
	 * Evaluates the state for the given player and returns the numerical
	 * representation of the evaluation.  A higher value indicates a better
	 * score for the player.
	 * 
	 * @param state the state to evaluate
	 * @param player the minimax player to evaluate the state for
	 * @return the numerical representation of the evaluation
	 */
	public int evaluate (S state, M player);
}
