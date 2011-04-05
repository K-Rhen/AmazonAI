package com.pas.cosc322.amazon.board;

import static com.pas.cosc322.amazon.main.ProjectConstants.maxExplorations;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import com.pas.cosc322.amazon.player.AmazonMove;
import com.pas.cosc322.amazon.player.AmazonRole;

/**
 * Generates the successor actions for a given board and role.
 * This function only returns a portion of all possible actions;
 * it was found that 'bad' moves generally do not turn out to be 'good'
 * moves in Amazons.
 * 
 * @author Paul
 * @author Sam
 * @author Andrew
 */
class AmazonSuccessorFunction
{
	private AmazonBoard board;
	private AmazonRole role;
	
	private PriorityQueue<AmazonMove> actions = new PriorityQueue<AmazonMove>();
	
	public Queue<AmazonMove> generateSuccessorActions (AmazonBoard board, AmazonRole role)
	{
		this.board = board;
		this.role = role;
		// calculate each queens possible moves
		for (byte[] queen : board.getQueens(role))
		{
			for (byte[] dir : AmazonBoard.dirs)
			{
				calculateQueenMoves(queen, dir[0], dir[1]);
			}
		}
		// to save time and memory, only return a portion of the actions
		Queue<AmazonMove> toReturn = new LinkedList<AmazonMove>();
		for (int i = 0; i < maxExplorations; i++)
		{
			if (actions.isEmpty())
			{
				break;
			}
			toReturn.add(actions.remove());
		}
		actions.clear();
		this.role = null;
		this.board = null;
		return toReturn;
	}
	
	private void calculateQueenMoves (byte[] queen, byte dr, byte dc)
	{
		byte row = (byte) (queen[0] + dr), col = (byte) (queen[1] + dc);
		// calculate the possible arrow shots
		while (board.inBounds(row, col) && board.posFree(row, col))
		{
			for (byte[] dir : AmazonBoard.dirs)
			{
				calculateArrowShots(queen, row, col, dir[0], dir[1]);
			}
			row += dr;
			col += dc;
		}
	}
	
	private void calculateArrowShots (byte[] queen, byte qfr, byte qfc, byte dr, byte dc)
	{
		byte row = (byte) (qfr + dr), col = (byte) (qfc + dc);
		// the last condition is for the special case where a queen an arrow to where it was initially
		while (board.inBounds(row, col) && (board.posFree(row, col) || (row == queen[0] && col == queen[1])))
		{
			// evaluate the actions v value for comparison purposes.  This way the search evaluates better moves first.
			AmazonMove move = new AmazonMove(role, queen[0], queen[1], qfr, qfc, row, col);
			board.applyAction(move);
			move.v = board.evaluate(role);
			board.undoAction(move);
			actions.add(move);
			row += dr;
			col += dc;
		}
	}
}
