package com.pas.cosc322.amazon.player;

import static com.pas.cosc322.amazon.player.AmazonRole.WHITE;
import static ubco.ai.games.GameMessage.ACTION_MOVE;
import net.n3.nanoxml.IXMLElement;

import com.pas.cosc322.amazon.search.Action;

/**
 * A simple struct representation of a move in Amazons.
 * A move is one movement of the queen and an arrow shot.
 * 
 * @author Paul
 * @author Andrew
 * @author Sam
 */
public class AmazonMove implements Action, Comparable<AmazonMove>
{
	public AmazonRole role;
	public byte qr, qc, qfr, qfc, ar, ac;
	public int v;

	/**
	 * Constructor.  Parses a move from the given xml element.
	 * 
	 * @param moveElement the xml element to parse
	 */
	public AmazonMove(IXMLElement moveElement, AmazonRole role)
	{
		this.role = role;
		String queen = ((IXMLElement) moveElement.getChildrenNamed("queen").firstElement()).getAttribute("move", null);
		qc = (byte) (queen.charAt(0) - 'a');
		qr = Byte.parseByte(queen.substring(1, 2));
		qfc = (byte) (queen.charAt(3) - 'a');
		qfr = Byte.parseByte(queen.substring(4, 5));
		String arrow = ((IXMLElement) moveElement.getChildrenNamed("arrow").firstElement()).getAttribute("move", null);
		ac = (byte) (arrow.charAt(0) - 'a');
		ar = Byte.parseByte(arrow.substring(1, 2));
	}

	public AmazonMove(AmazonRole role, byte qr, byte qc, byte qfr, byte qfc, byte ar, byte ac)
	{
		this.role = role;
		this.qr = qr;
		this.qc = qc;
		this.qfr = qfr;
		this.qfc = qfc;
		this.ar = ar;
		this.ac = ac;
	}

	public AmazonMove()
	{
		role = WHITE;
	}

	@Override
	public String toString()
	{
		String msg = "<action type='x'><queen move='xx-xx'></queen><arrow move='xx'></arrow></action>";

		msg = msg.replaceFirst("x", ACTION_MOVE);
		msg = msg.replaceFirst("x", String.valueOf((char) (qc + 'a')));
		msg = msg.replaceFirst("x", String.valueOf(qr));
		msg = msg.replaceFirst("x", String.valueOf((char) (qfc + 'a')));
		msg = msg.replaceFirst("x", String.valueOf(qfr));
		msg = msg.replaceFirst("x", String.valueOf((char) (ac + 'a')));
		msg = msg.replaceFirst("x", String.valueOf(ar));

		return msg;
	}

	@Override
	public AmazonMove clone()
	{
		//XXX might need to carry over v value
		return new AmazonMove(role, qr, qc, qfr, qfc, ar, ac);
	}

	@Override
	public boolean equals(Object o)
	{
		boolean equals = false;
		if (o instanceof AmazonMove)
		{
			AmazonMove other = (AmazonMove) o;
			equals = role == other.role;
			equals &= qr == other.qr;
			equals &= qc == other.qc;
			equals &= qfr == other.qfr;
			equals &= qfc == other.qfc;
			equals &= ar == other.ar;
			equals &= ac == other.ac;
		}
		return equals;
	}

	@Override
	public int compareTo (AmazonMove arg0)
	{
		// natural ordering of actions for priority queue
		if (v > arg0.v)
		{
			return -1;
		}
		if (arg0.v > v)
		{
			return 1;
		}
		return 0;
	}
}
