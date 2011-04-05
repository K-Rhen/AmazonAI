package com.pas.cosc322.amazon.board;

import org.junit.Test;

import com.pas.cosc322.amazon.player.AmazonMove;
import com.pas.cosc322.amazon.player.AmazonMoveValidator;
import static com.pas.cosc322.amazon.board.AmazonBoard.*;
import junit.framework.TestCase;
import static com.pas.cosc322.amazon.player.AmazonRole.*;

public class AmazonBoardTest extends TestCase
{
	@Test
	public void test_initial_state_is_correct ()
	{
		AmazonBoard board = new AmazonBoard();
		assertTrue(board.queenAt(WHITE, (byte)3, (byte)0));
		assertTrue(board.queenAt(WHITE, (byte)0, (byte)3));
		assertTrue(board.queenAt(WHITE, (byte)0, (byte)6));
		assertTrue(board.queenAt(WHITE, (byte)3, (byte)9));
		assertTrue(board.queenAt(BLACK, (byte)6, (byte)0));
		assertTrue(board.queenAt(BLACK, (byte)9, (byte)3));
		assertTrue(board.queenAt(BLACK, (byte)9, (byte)6));
		assertTrue(board.queenAt(BLACK, (byte)6, (byte)9));
	}
	
	@Test
	public void test_applyAction_changes_board_correctly ()
	{
		AmazonBoard board = new AmazonBoard();
		AmazonMove move = new AmazonMove(WHITE, (byte)3, (byte)0, (byte)4, (byte)0, (byte)5, (byte)0);
		assertTrue(AmazonMoveValidator.validateMove(board, move));
		board.applyAction(move);
		assertTrue(board.queenAt(WHITE, (byte)4, (byte)0));
		assertEquals(WHITE_QUEEN, board.markAt((byte)4, (byte)0));
		assertEquals(ARROW, board.markAt((byte)5, (byte)0));
		assertTrue(board.posFree((byte)3, (byte)0));
	}
	
	@Test
	public void test_undoAction_changes_board_back_correctly ()
	{
		AmazonBoard board = new AmazonBoard();
		AmazonMove move = new AmazonMove(WHITE, (byte)3, (byte)0, (byte)4, (byte)0, (byte)5, (byte)0);
		board.applyAction(move);
		board.undoAction(move);
		assertTrue(board.posFree((byte)5, (byte)0));
		assertTrue(board.posFree((byte)4, (byte)0));
		assertTrue(board.queenAt(WHITE, (byte)3, (byte)0));
		assertEquals(WHITE_QUEEN, board.markAt((byte)3, (byte)0));
	}
	
	@Test
	public void test_cloned_board_does_not_affect_original ()
	{
		AmazonBoard original = new AmazonBoard();
		AmazonBoard clone = original.clone();
		AmazonMove move = new AmazonMove(WHITE, (byte)3, (byte)0, (byte)4, (byte)0, (byte)5, (byte)0);
		clone.applyAction(move);
		assertFalse(original.posFree((byte)3, (byte)0));
		assertFalse(original.queenAt(WHITE, (byte)4, (byte)0));
		assertTrue(original.posFree((byte)5, (byte)0));
	}
	
	@Test
	public void test_cloned_board_equals_original ()
	{
		AmazonBoard original = new AmazonBoard();
		AmazonBoard clone = original.clone();
		assertTrue(clone.equals(original));
		assertTrue(original.equals(clone));
	}
}
