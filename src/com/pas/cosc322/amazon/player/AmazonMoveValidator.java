package com.pas.cosc322.amazon.player;

import static com.pas.cosc322.amazon.main.Main.debug;

import com.pas.cosc322.amazon.board.AmazonBoard;

/**
 * Validation method for an AmazonMove
 * 
 * @author Paul
 * @author Andrew
 * @author Sam
 */
public class AmazonMoveValidator
{
	/**
	 * @param board the board
	 * @param move the action to validate for the board
	 * @return if the move is valid given the current board state
	 */
	public synchronized static boolean validateMove(AmazonBoard board, AmazonMove move)
	{
		try
		{
			// check the opponent has a queen at the starting position
			if (board.queenAt(move.role, move.qr, move.qc))
			{
				// validate the queen's dr and dc
				byte dr, dc;
				if (move.qfr < move.qr)
				{
					dr = -1;
				}
				else if (move.qfr > move.qr)
				{
					dr = 1;
				}
				else
				{
					dr = 0;
				}
				if (move.qfc < move.qc)
				{
					dc = -1;
				}
				else if (move.qfc > move.qc)
				{
					dc = 1;
				}
				else
				{
					dc = 0;
				}
				// walk to the queen's final position
				if (dr != 0 || dc != 0)
				{
					byte qr = (byte) (move.qr + dr), qc = (byte) (move.qc + dc);
					while (board.inBounds(qr, qc) && board.posFree(qr, qc))
					{
						if (qr == move.qfr && qc == move.qfc)
						{
							// validate the arrow's dr and dc
							if (move.ar < move.qfr)
							{
								dr = -1;
							}
							else if (move.ar > move.qfr)
							{
								dr = 1;
							}
							else
							{
								dr = 0;
							}
							if (move.ac < move.qfc)
							{
								dc = -1;
							}
							else if (move.ac > move.qfc)
							{
								dc = 1;
							}
							else
							{
								dc = 0;
							}
							if (dr != 0 || dc != 0)
							{
								// walk to the arrow's final position
								byte ar = (byte) (move.qfr + dr), ac = (byte) (move.qfc + dc);
								while (board.inBounds(ar, ac) && (board.posFree(ar, ac) || (ar == move.qr && ac == move.qc)))
								{
									if (ar == move.ar && ac == move.ac)
									{
										// all conditions are met
										return true;
									}
									ar += dr;
									ac += dc;
								}
								debug.warning("The arrow's path is not clear!");
								return false;
							}
							debug.warning("Invalid arrow direction!");
							return false;
						}
						qr += dr;
						qc += dc;
					}
					debug.warning("The queen's path is not clear!");
					return false;
				}
				debug.warning("Invalid queen direction!");
				return false;
			}
			debug.warning("No queen found at starting location!");
			return false;
		}
		catch (Exception e)
		{
			debug.warning("Exception occured while validating move, stack trace to follow.");
			e.printStackTrace();
		}
		return false;
	}
}
