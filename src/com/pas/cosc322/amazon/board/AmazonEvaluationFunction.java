package com.pas.cosc322.amazon.board;

import static com.pas.cosc322.amazon.board.AmazonBoard.NUM_COLS;
import static com.pas.cosc322.amazon.board.AmazonBoard.NUM_ROWS;
import static com.pas.cosc322.amazon.board.AmazonBoard.dirs;
import static com.pas.cosc322.amazon.main.ProjectConstants.endGameTurnOver;
import static com.pas.cosc322.amazon.player.AmazonRole.BLACK;
import static com.pas.cosc322.amazon.player.AmazonRole.WHITE;

import java.util.Arrays;
import java.util.BitSet;

import com.pas.cosc322.amazon.player.AmazonRole;
import com.pas.cosc322.amazon.search.EvaluationFunction;

/**
 * A territory-mobility (TM) evaluation function.
 * A limited TM function is used early in the game due to game tree size.
 * 
 * @author Paul
 * @author Andrew
 * @author Sam
 */
public class AmazonEvaluationFunction implements EvaluationFunction<AmazonBoard, AmazonRole>
{
	private AmazonBoard board;
	
	private BitSet[] ourMobility, oppMobility;
	
	private byte[][] ourTerritory, oppTerritory;
	
	private BitSet ourOneHop, ourTwoHop, oppOneHop, oppTwoHop;
	
	/**
	 * Constructor
	 */
	public AmazonEvaluationFunction ()
	{
		ourMobility = new BitSet[4];
		oppMobility = new BitSet[4];
		for (byte i = 0; i < 4; i++)
		{
			ourMobility[i] = new BitSet();
			oppMobility[i] = new BitSet();
		}
		ourTerritory = new byte[NUM_ROWS][NUM_COLS];
		oppTerritory = new byte[NUM_ROWS][NUM_COLS];
		ourOneHop = new BitSet(NUM_ROWS * NUM_COLS);
		ourTwoHop = new BitSet(NUM_ROWS * NUM_COLS);
		oppOneHop = new BitSet(NUM_ROWS * NUM_COLS);
		oppTwoHop = new BitSet(NUM_ROWS * NUM_COLS);
		clear();
	}
	
	@Override
	public int evaluate (AmazonBoard board, AmazonRole role)
	{
		this.board = board;
		int eval;
		// using the amount of moves played, determine which heuristic to use
		if (board.getMarkedPos() < endGameTurnOver)
		{
			eval = earlyGameEvaluation(role);
		}
		else
		{
			eval = endGameEvaluation(role);
		}
		clear();
		return eval;
	}
	
	private int earlyGameEvaluation (AmazonRole role)
	{
		// create the one and two hop webs for both players
		initLimitedTerritoryWebs(role, ourOneHop, ourTwoHop);
		initLimitedTerritoryWebs(role.other(), oppOneHop, oppTwoHop);
		int score = 0;
		// one point for each one hop
		score += ourOneHop.cardinality();
		// an additional point for each one hop not reachable by the opponent in one hop
		ourOneHop.andNot(oppOneHop);
		score += ourOneHop.cardinality();
		// one point for each two hop not reachable in one hop by the opponent
		ourTwoHop.andNot(oppOneHop);
		score += ourTwoHop.cardinality();
		// an additional point for each two hop not reachable by the opponent in two hops
		ourTwoHop.andNot(oppTwoHop);
		score += ourTwoHop.cardinality();
		
		// weight the escape routes of the opponent
		score += Math.round(1.1 * closure(role.other()));
		// weight the escape routes of the player
		score -= closure(role);
		
		return score;
	}
	
	private int endGameEvaluation (AmazonRole role)
	{
		// construct the entire mobility and territory web
		initMobilityWebs(role, ourMobility);
		initMobilityWebs(role.other(), oppMobility);
		initTerritoryWebs(role, ourTerritory);
		initTerritoryWebs(role.other(), oppTerritory);
		int ourPoints = 0, oppPoints = 0;
		byte ourMob, oppMob, rmo;
		for (byte r = 0; r < NUM_ROWS; r++)
		{
			for (byte c = 0; c < NUM_COLS; c++)
			{
				// row-major order
				rmo = (byte) (r * NUM_ROWS + c);
				ourMob = 0;
				oppMob = 0;
				// determine how many of each player's queens can reach the square in one move
				for (byte i = 0; i < 4; i++)
				{
					if (ourMobility[i].get(rmo))
					{
						ourMob++;
					}
					if (oppMobility[i].get(rmo))
					{
						oppMob++;
					}
				}
				// weigh the territory ownership by mobility
				if (ourTerritory[r][c] < oppTerritory[r][c])
				{
					ourPoints += 4 + ourMob - oppMob;
				}
				else if (oppTerritory[r][c] < ourTerritory[r][c])
				{
					oppPoints += 4 + oppMob - ourMob;
				}
				// contested territory is decided by mobility to that territory
				else
				{
					ourPoints += ourMob;
					oppPoints += oppMob;
				}
			}
		}
		return ourPoints - oppPoints;
	}
	
	private int closure (AmazonRole role)
	{
		byte[][] queens = board.getQueens(role);
		byte row, col;
		int score = 0;
		byte closure;
		for (byte[] queen : queens)
		{
			closure = 8;
			for (byte[] dir : dirs)
			{
				row = (byte) (queen[0] + dir[0]);
				col = (byte) (queen[1] + dir[1]);
				// if we can move that queen in that direction we score more points
				if (board.inBounds(row, col) && board.posFree(row, col))
				{
					closure--;
				}
			}
			// a blocked queen is bad (or good depending on the role)
			for (byte i = closure; i >= 6; i--)
			{
				score += 15;
			}
		}
		return score;
	}
	
	private void initLimitedTerritoryWebs (AmazonRole role, BitSet oneHop, BitSet twoHop)
	{
		byte row, col, row2, col2;
		for (byte[] queen : board.getQueens(role))
		{
			for (byte[] dir : dirs)
			{
				row = (byte) (queen[0] + dir[0]);
				col = (byte) (queen[1] + dir[1]);
				// for this direction update the one hop web
				while (board.inBounds(row, col) && board.posFree(row, col))
				{
					oneHop.set(row * NUM_ROWS + col);
					// at each position we can branch from here, finding the two hop web
					for (byte[] dir2 : dirs)
					{
						// no point in checking the cardinal direction we came from
						if (dir2 != dir && -dir2[0] != dir[0] && -dir2[1] != dir[1])
						{
							row2 = (byte) (row + dir2[0]);
							col2 = (byte) (row + dir2[1]);
							while (board.inBounds(row2, col2) && board.posFree(row2, col2))
							{
								twoHop.set(row2 * NUM_ROWS + col2);
								row2 += dir2[0];
								col2 += dir2[1];
							}
						}
					}
					row += dir[0];
					col += dir[1];
				}
			}
		}
		// remove any hops from the two hop that are in the one hop - they are redundant
		twoHop.andNot(oneHop);
	}
	
	private void initMobilityWebs (AmazonRole role, BitSet[] mobility)
	{
		// finds where all queens can visit in one move
		byte[][] queens = board.getQueens(role);
		byte[] queen;
		byte r, c;
		for (byte q = 0; q < 4; q++)
		{
			queen = queens[q];
			for (byte[] dir : dirs)
			{
				r = (byte) (queen[0] + dir[0]);
				c = (byte) (queen[1] + dir[1]);
				while (board.inBounds(r, c) && board.posFree(r, c))
				{
					mobility[q].set(r * NUM_ROWS + c);
					r += dir[0];
					c += dir[1];
				}
			}
		}
	}
	
	private void initTerritoryWebs (AmazonRole role, byte[][] territory)
	{
		// determines the minimum Manhattan distance for any queen to a square
		for (byte[] queen : board.getQueens(role))
		{
			for (byte[] dir : dirs)
			{
				territoryHelper(territory, (byte)(queen[0] + dir[0]), (byte)(queen[1] + dir[1]), (byte) 1);
			}
		}
	}
	
	private void territoryHelper (byte[][] territory, byte r, byte c, byte move)
	{
		if (board.inBounds(r, c) && board.posFree(r, c) && territory[r][c] > move)
		{
			territory[r][c] = move;
			for (byte[] dir : dirs)
			{
				territoryHelper(territory, (byte)(r + dir[0]), (byte)(c + dir[1]), (byte)(move + 1));
			}
		}
	}
	
	/**
	 * @param board the board
	 * @return if there is no square where each side can both reach
	 */
	protected boolean isEnclosed (AmazonBoard board)
	{
		this.board = board;
		initTerritoryWebs(WHITE, ourTerritory);
		initTerritoryWebs(BLACK, oppTerritory);
		for (byte r = 0; r < NUM_ROWS; r++)
		{
			for (byte c = 0; c < NUM_COLS; c++)
			{
				if (ourTerritory[r][c] < Byte.MAX_VALUE && oppTerritory[r][c] < Byte.MAX_VALUE)
				{
					clear();
					return false;
				}
			}
		}
		clear();
		return true;
	}
	
	private void clear ()
	{
		for (byte i = 0; i < 4; i++)
		{
			ourMobility[i].clear();
			oppMobility[i].clear();
		}
		for (byte i = 0; i < NUM_ROWS; i++)
		{
			Arrays.fill(ourTerritory[i], Byte.MAX_VALUE);
			Arrays.fill(oppTerritory[i], Byte.MAX_VALUE);
		}
		ourOneHop.clear();
		ourTwoHop.clear();
		oppOneHop.clear();
		oppTwoHop.clear();
		board = null;
	}
}
