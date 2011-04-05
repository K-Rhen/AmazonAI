package com.pas.cosc322.amazon.main;

import static com.pas.cosc322.amazon.main.ProjectConstants.DEFAULT_DEBUG_LEVEL;
import static com.pas.cosc322.amazon.main.ProjectConstants.DEFAULT_SERVER_ROOM;
import static com.pas.cosc322.amazon.main.ProjectConstants.DEFAULT_USER_NAME;
import static com.pas.cosc322.amazon.main.ProjectConstants.DEFAULT_USER_PASS;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import ubco.ai.GameRoom;
import ubco.ai.games.GameClient;

import com.pas.cosc322.amazon.player.AmazonPlayer;

/**
 * Main entry point for the Amazon's AI.
 * 
 * @author Paul
 * @author Andrew
 * @author Sam
 */
public class Main
{
	public static final Logger debug;
	private static Handler handler;
	
	private static GameClient client;
	
	static
	{
		debug = Logger.getLogger(Main.class.getPackage().getName());
		debug.setLevel(Level.ALL);
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);
		Calendar date = Calendar.getInstance();
		String fileName = date.get(Calendar.YEAR)+"-"+(date.get(Calendar.MONTH)+1)+"-"+date.get(Calendar.DATE)+"-"+date.get(Calendar.HOUR_OF_DAY)+""+date.get(Calendar.MINUTE);
		try
		{
			new File("logs").mkdir();
			handler = new FileHandler("logs/"+fileName+".xml", true);
			handler.setFormatter(new XMLFormatter());
			handler.setLevel(Level.ALL);
			debug.addHandler(handler);
		}
		catch (Exception e)
		{
			debug.warning("Could not add FileHandler to debug logger! Stack trace to follow:");
			e.printStackTrace();
		}
	}
	
	/**
	 * Main java entry point.
	 * 
	 * @param args the runtime arguments for the AI.  See the build properties file for a list of arguments.
	 */
	public static void main (String[] args)
	{
		debug.entering("Main", "main", Arrays.toString(args));
		
		// runtime arguments
		String userName;
		String userPass;
		String serverRoom;
		int maxThreads;
		long minMemory;
		long searchTime;
		int maxExplorations;
		int initialDepth;
		byte endGameTurnOver;
		Level level;
		boolean manualInput;
		
		try
		{
			userName = args[0].trim();
			userPass = args[1].trim();
			serverRoom = args[2].trim();
			minMemory = Long.parseLong(args[3].trim());
			if (minMemory < 1)
			{
				throw new IllegalArgumentException("minMemory argument must be > 0!");
			}
			searchTime = Long.parseLong(args[4].trim());
			if (searchTime < 1)
			{
				throw new IllegalArgumentException("searchTime argument must be > 0!");
			}
			maxExplorations = Integer.parseInt(args[5].trim());
			if (maxExplorations < 1)
			{
				throw new IllegalArgumentException("maxExplorations argument must be > 0!");
			}
			initialDepth = Integer.parseInt(args[6].trim());
			if (initialDepth < 1)
			{
				throw new IllegalArgumentException("initialDepth argument must be >= 1!");
			}
			endGameTurnOver = Byte.parseByte(args[7].trim());
			if (endGameTurnOver < 1 || endGameTurnOver >= 92)
			{
				throw new IllegalArgumentException("endGameTurnOver argument must be > 0 and < 92!");
			}
			maxThreads = Integer.parseInt(args[8].trim());
			if (maxThreads < 1)
			{
				throw new IllegalArgumentException("maxThreads argument must be > 0!");
			}
			level = Level.parse(args[9].trim());
			manualInput = Boolean.parseBoolean(args[10].trim());
			
			ProjectConstants.maxThreads = maxThreads;
			ProjectConstants.minMemory = minMemory;
			ProjectConstants.searchTime = searchTime;
			ProjectConstants.maxExplorations = maxExplorations;
			ProjectConstants.initialDepth = initialDepth;
			ProjectConstants.endGameTurnOver = endGameTurnOver;
			ProjectConstants.manualInput = manualInput;
		}
		catch (Exception e)
		{
			debug.logp(Level.SEVERE, "Main", "main", "Exception occurred while parsing arguments: "+e+" using default values.");
			userName = DEFAULT_USER_NAME;
			userPass = DEFAULT_USER_PASS;
			serverRoom = DEFAULT_SERVER_ROOM;
			level = DEFAULT_DEBUG_LEVEL;
		}
		
		debug.setLevel(level);
		
		AmazonPlayer player = new AmazonPlayer(userName);
		client = new GameClient(userName, userPass, player);
		GameRoom gameRoom = null;
		for (GameRoom room : client.getRoomLists())
		{
			if (room.roomName.equals(serverRoom))
			{
				gameRoom = room;
				break;
			}
		}
		if (gameRoom == null)
		{
			closeGame(-1, "Could not find correct game room:"+serverRoom);
		}
		// init the player and join the room if everything checks out
		player.init(client, gameRoom);
		client.joinGameRoom(serverRoom);
	}
	
	/**
	 * Terminates the AI (closes the running instance of the JVM).
	 * 
	 * @param code the exit code; typically, any non-zero exit code indicates an abnormal termination
	 * @param message the message to log with the exit
	 */
	public static void closeGame(int code, String message)
	{
		debug.logp(Level.SEVERE, "Main", "closeGame", "CLOSING GAME message:" + message + " exit code:" + code);
		try
		{
			handler.close();
		}
		catch (Exception ignored)
		{
		}
		System.exit(code);
	}
}
