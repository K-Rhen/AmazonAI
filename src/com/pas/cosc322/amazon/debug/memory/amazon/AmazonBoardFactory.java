package com.pas.cosc322.amazon.debug.memory.amazon;

import com.pas.cosc322.amazon.board.AmazonBoard;
import com.pas.cosc322.amazon.debug.memory.ObjectFactory;

public class AmazonBoardFactory implements ObjectFactory<AmazonBoard>
{
	@Override
	public AmazonBoard createObject ()
	{
		return new AmazonBoard();
	}
}
