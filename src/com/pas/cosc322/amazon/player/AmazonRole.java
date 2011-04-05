package com.pas.cosc322.amazon.player;

import com.pas.cosc322.amazon.search.MinimaxPlayer;


/**
 * Enumeration of an amazon role.
 * A role is also a minimax player.
 * 
 * @author Paul
 * @author Sam
 * @author Andrew
 */
public enum AmazonRole implements MinimaxPlayer
{
	/** white queens */
	WHITE("W"),
	/** black queens */
	BLACK("B");
	
	private String value;
	
	private AmazonRole (String value)
	{
		this.value = value;
	}
	
	/**
	 * @return the String value of the role
	 */
	public String getValue ()
	{
		return value;
	}
	
	/**
	 * @return the role opposite of this role.  BLACK -> WHITE and WHITE -> BLACK.
	 */
	public AmazonRole other ()
	{
		if (this == WHITE)
		{
			return BLACK;
		}
		else if (this == BLACK)
		{
			return WHITE;
		}
		return null;
	}
	
	/**
	 * Gets the role associated with the String value.
	 * 
	 * @param value the value
	 * @return the role given the String value
	 */
	public static AmazonRole getByValue (String value)
	{
		for (AmazonRole role : values())
		{
			if (role.getValue().equals(value))
			{
				return role;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString ()
	{
		return value;
	}
}
