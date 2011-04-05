package com.pas.cosc322.amazon.debug.memory.amazon;

import com.pas.cosc322.amazon.debug.memory.ObjectFactory;
import com.pas.cosc322.amazon.player.AmazonMove;

public class AmazonMoveFactory implements ObjectFactory<AmazonMove>
{
	@Override
	public AmazonMove createObject ()
	{
		return new AmazonMove();
	}
}
