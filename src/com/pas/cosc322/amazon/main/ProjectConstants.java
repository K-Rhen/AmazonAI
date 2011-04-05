package com.pas.cosc322.amazon.main;

import static java.util.logging.Level.*;
import java.util.logging.Level;

/**
 * Project constants for the game.
 * 
 * @author Paul
 * @author Sam
 * @author Andrew
 */
public class ProjectConstants
{
	public static final String DEFAULT_USER_NAME = "defaultUser";
	public static final String DEFAULT_USER_PASS = "defaultPass";
	public static final String DEFAULT_SERVER_ROOM = "Okanagan Lake";
	public static final Level DEFAULT_DEBUG_LEVEL = OFF;
	
	public static final String ACTION_SURRENDER = "surrender";

	/** maximum threads that will be allocated for searches */
	public static int maxThreads = 1;
	/** minimum free memory (in bytes) before the search will cutoff */
	public static long minMemory = 5000000;
	/** maximum time (in ms) a search will take before it will cutoff */
	public static long searchTime = 25000;
	/** maximum amount of successors to analyse per depth of the search tree */
	public static int maxExplorations = 3;
	/** initial depth used by IDS */
	public static int initialDepth = 1;
	/** number of turns before the end game heuristic will come into effect */
	public static byte endGameTurnOver = 60;
	/** if true, the AI will not play, manual input will be enabled through the GUI */
	public static boolean manualInput = false;
	
	private ProjectConstants ()
	{
	}
}
