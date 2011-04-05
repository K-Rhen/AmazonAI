package com.pas.cosc322.amazon.board;

import static com.pas.cosc322.amazon.player.AmazonRole.WHITE;

import java.util.Arrays;
import java.util.Queue;

import com.pas.cosc322.amazon.player.AmazonMove;
import com.pas.cosc322.amazon.player.AmazonRole;
import com.pas.cosc322.amazon.search.State;

/**
 * Amazon board representation.
 * 
 * @author Paul
 * @author Andrew
 * @author Sam
 */
public class AmazonBoard implements State<AmazonMove, AmazonRole>
{
	public static final byte NUM_ROWS = 10;
	public static final byte NUM_COLS = 10;
	
	public static final byte[][] dirs = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};
	
	public static final byte FREE = 0;
	public static final byte WHITE_QUEEN = 1;
	public static final byte BLACK_QUEEN = 2;
	public static final byte ARROW = 3;
	
	protected byte[][] whiteQueens = new byte[4][2];
	protected byte[][] blackQueens = new byte[4][2];
	
	protected byte[][] board = new byte[NUM_ROWS][NUM_COLS];
	
	private byte markedPos = 0;
	
	private AmazonSuccessorFunction successors = new AmazonSuccessorFunction();
	private AmazonEvaluationFunction eval = new AmazonEvaluationFunction();
	private AmazonCutoffTest cutoff = new AmazonCutoffTest();
	
	private AmazonBoard (boolean cloned)
	{
		// used only by clone() method
	}
	
	/**
	 * Constructor.
	 * Creates an initially empty board with queens at starting positions.
	 */
	public AmazonBoard ()
	{
		whiteQueens = new byte[4][2];
		blackQueens = new byte[4][2];
		
		whiteQueens[0][0] = 3;
		whiteQueens[0][1] = 0;
		whiteQueens[1][0] = 0;
		whiteQueens[1][1] = 3;
		whiteQueens[2][0] = 0;
		whiteQueens[2][1] = 6;
		whiteQueens[3][0] = 3;
		whiteQueens[3][1] = 9;
		
		blackQueens[0][0] = 6;
		blackQueens[0][1] = 0;
		blackQueens[1][0] = 9;
		blackQueens[1][1] = 3;
		blackQueens[2][0] = 9;
		blackQueens[2][1] = 6;
		blackQueens[3][0] = 6;
		blackQueens[3][1] = 9;
		
		board[whiteQueens[0][0]][whiteQueens[0][1]] = WHITE_QUEEN;
		board[whiteQueens[1][0]][whiteQueens[1][1]] = WHITE_QUEEN;
		board[whiteQueens[2][0]][whiteQueens[2][1]] = WHITE_QUEEN;
		board[whiteQueens[3][0]][whiteQueens[3][1]] = WHITE_QUEEN;
		
		board[blackQueens[0][0]][blackQueens[0][1]] = BLACK_QUEEN;
		board[blackQueens[1][0]][blackQueens[1][1]] = BLACK_QUEEN;
		board[blackQueens[2][0]][blackQueens[2][1]] = BLACK_QUEEN;
		board[blackQueens[3][0]][blackQueens[3][1]] = BLACK_QUEEN;
	}
	
	/**
	 * @return the number of non-empty spaces on the board
	 */
	public byte getMarkedPos ()
	{
		return markedPos;
	}
	
	/**
	 * @param row the row
	 * @param col the column
	 * @return if there is no queen or arrow at that position
	 */
	public boolean posFree (byte row, byte col)
	{
		return board[row][col] == FREE;
	}
	
	/**
	 * @param role the player role
	 * @param row the row
	 * @param col the column
	 * @return if a queen owned by the specified role is at that position
	 */
	public boolean queenAt (AmazonRole role, byte row, byte col)
	{
		return board[row][col] == getMarker(role);
	}
	
	/**
	 * @param row the row
	 * @param col the column
	 * @return the marker at that position
	 */
	public byte markAt (byte row, byte col)
	{
		return board[row][col];
	}
	
	/**
	 * @param role the player role
	 * @return the positions of all queens owned by role
	 */
	public byte[][] getQueens (AmazonRole role)
	{
		if (role == WHITE)
		{
			return whiteQueens;
		}
		return blackQueens;
	}
	
	/**
	 * @param role the player role
	 * @return the queen marker for role
	 */
	public byte getMarker (AmazonRole role)
	{
		if (role == WHITE)
		{
			return WHITE_QUEEN;
		}
		return BLACK_QUEEN;
	}
	
	/**
	 * @param row the row
	 * @param col the column
	 * @return if the row and column is a valid position on the board
	 */
	public boolean inBounds (byte row, byte col)
	{
		return row >= 0 && row < NUM_ROWS && col >= 0 && col < NUM_COLS;
	}
	
	@Override
	public Queue<AmazonMove> actions (AmazonRole minimaxPlayer)
	{
		return successors.generateSuccessorActions(this, minimaxPlayer);
	}

	@Override
	public AmazonBoard applyAction (AmazonMove action)
	{
		byte[][] queens = getQueens(action.role);
		byte marker = getMarker(action.role);
		
		for (byte[] queen : queens)
		{
			if (queen[0] == action.qr && queen[1] == action.qc)
			{
				queen[0] = action.qfr;
				queen[1] = action.qfc;
				break;
			}
		}
		
		board[action.qr][action.qc] = FREE;
		board[action.qfr][action.qfc] = marker;
		board[action.ar][action.ac] = ARROW;
		
		markedPos++;
		
		return this;
	}

	@Override
	public AmazonBoard undoAction (AmazonMove action)
	{
		byte[][] queens = getQueens(action.role);
		byte marker = getMarker(action.role);
		
		for (byte[] queen : queens)
		{
			if (queen[0] == action.qfr && queen[1] == action.qfc)
			{
				queen[0] = action.qr;
				queen[1] = action.qc;
				break;
			}
		}
		
		board[action.qfr][action.qfc] = FREE;
		board[action.ar][action.ac] = FREE;
		board[action.qr][action.qc] = marker;
		
		markedPos--;
		
		return this;
	}
	
	@Override
	public int evaluate (AmazonRole role)
	{
		return eval.evaluate(this, role);
	}
	
	@Override
	public boolean cutoffTest (int depth, long startTime)
	{
		return cutoff.cutoffTest(this, depth, startTime);
	}
	
	@Override
	public AmazonBoard clone ()
	{
		AmazonBoard newBoard = new AmazonBoard(true);
		for (byte row = 0; row < board.length; row++)
		{
			newBoard.board[row] = Arrays.copyOf(board[row], board[row].length); 
		}
		for (byte queen = 0; queen < whiteQueens.length; queen++)
		{
			newBoard.whiteQueens[queen] = Arrays.copyOf(whiteQueens[queen], whiteQueens[queen].length);
			newBoard.blackQueens[queen] = Arrays.copyOf(blackQueens[queen], blackQueens[queen].length);
		}
		return newBoard;
	}
	
	@Override
	public boolean equals (Object o)
	{
		boolean equal = false;
		if (o instanceof AmazonBoard)
		{
			AmazonBoard other = (AmazonBoard) o;
			equal = Arrays.deepEquals(board, other.board);
			equal &= Arrays.deepEquals(whiteQueens, other.whiteQueens);
			equal &= Arrays.deepEquals(blackQueens, other.blackQueens);
		}
		return equal;
	}
	
	/**
	 * @return if there is no contested territory (all queens are enclosed from opposing queens)
	 */
	public boolean isEnclosed ()
	{
		return eval.isEnclosed(this);
	}
}
