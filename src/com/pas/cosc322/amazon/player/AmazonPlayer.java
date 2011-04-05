package com.pas.cosc322.amazon.player;

import static com.pas.cosc322.amazon.main.Main.debug;
import static com.pas.cosc322.amazon.main.ProjectConstants.ACTION_SURRENDER;
import static com.pas.cosc322.amazon.main.ProjectConstants.endGameTurnOver;
import static com.pas.cosc322.amazon.main.ProjectConstants.initialDepth;
import static com.pas.cosc322.amazon.main.ProjectConstants.manualInput;
import static com.pas.cosc322.amazon.main.ProjectConstants.maxThreads;
import static com.pas.cosc322.amazon.player.AmazonRole.WHITE;
import static ubco.ai.games.GameMessage.ACTION_GAME_START;
import static ubco.ai.games.GameMessage.ACTION_MOVE;
import static ubco.ai.games.GameMessage.ACTION_ROOM_JOINED;
import static ubco.ai.games.GameMessage.MSG_GAME;

import java.util.logging.Level;

import net.n3.nanoxml.IXMLElement;
import ubco.ai.GameRoom;
import ubco.ai.connection.ServerMessage;
import ubco.ai.games.GameClient;
import ubco.ai.games.GameMessage;
import ubco.ai.games.GamePlayer;

import com.pas.cosc322.amazon.board.AmazonBoard;
import com.pas.cosc322.amazon.board.VisualAmazonBoard;
import com.pas.cosc322.amazon.main.Main;
import com.pas.cosc322.amazon.search.MinimaxSearch;
import com.pas.cosc322.amazon.search.StateSpaceSearch;

/**
 * Message delegate for the game client.
 * This class is responsible for game flow.
 * 
 * @author Paul
 * @author Sam
 * @author Andrew
 */
public class AmazonPlayer implements GamePlayer
{
	private GameClient client;
	private GameRoom room;

	private String userName;
	private AmazonRole role;

	private AmazonBoard board;
	private VisualAmazonBoard visualBoard;

	private MinimaxSearch<AmazonBoard, AmazonMove, AmazonRole> minimax;
	private StateSpaceSearch<AmazonBoard, AmazonMove, AmazonRole> statespace;
	
	private boolean myTurn = false;
	private boolean enclosed = false;

	/**
	 * Constructor
	 * 
	 * @param userName the user name of the player connected to the server
	 */
	public AmazonPlayer (String userName)
	{
		this.userName = userName;
	}

	/**
	 * Inits the player given a client and game room.
	 * 
	 * @param client the game client
	 * @param room the game room joined by the client
	 */
	public void init (GameClient client, GameRoom room)
	{
		debug.entering("AmazonPlayer", "init", client);
		this.client = client;
		this.room = room;
		board = new AmazonBoard();
		minimax = new MinimaxSearch<AmazonBoard, AmazonMove, AmazonRole>(maxThreads);
		minimax.setCutoffDepth(initialDepth);
		statespace = new StateSpaceSearch<AmazonBoard, AmazonMove, AmazonRole>(maxThreads);
		visualBoard = new VisualAmazonBoard();
		if (manualInput)
		{
			visualBoard.madeMove().add(this, "handleManualInput", false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ubco.ai.games.GamePlayer#handleMessage(java.lang.String)
	 */
	@Override
	public boolean handleMessage (String message) throws Exception
	{
		// most likely unused for this game
		debug.logp(Level.INFO, "AmazonPlayer", "handleMessage", message);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ubco.ai.games.GamePlayer#handleMessage(ubco.ai.games.GameMessage)
	 */
	@Override
	public boolean handleMessage (GameMessage gameMessage) throws Exception
	{
		debug.logp(Level.INFO, "AmazonPlayer", "handleMessage", "msgType:"+gameMessage.msgType+" msg:"+gameMessage.msg);
		try
		{
			// parse the game message
			IXMLElement element = ServerMessage.parseMessage(gameMessage.msg);
			String type = element.getAttribute("type", null);
			// if the opponent has made a move...
			if (type.equals(ACTION_MOVE))
			{
				AmazonMove move = new AmazonMove(element, role.other());
				// first validate the move!
				if (!myTurn && AmazonMoveValidator.validateMove(board, move))
				{
					myTurn = true;
					// apply the opponent's action
					board.applyAction(move);
					visualBoard.update(move);
					if (!manualInput)
					{
						makeMove();
					}
				}
				else
				{
					Main.closeGame(-1, "Opponent made an invalid move! " + move);
				}
				// suggest garbage collection during "down time"
				System.runFinalization();
				System.gc();
			}
			// or if the game has started...
			else if (type.equals(ACTION_GAME_START))
			{
				// search the element for our role
				for (Object o : element.getChildAtIndex(0).getChildren())
				{
					IXMLElement user = (IXMLElement) o;
					if (user.getAttribute("name", null).equals(userName))
					{
						role = AmazonRole.getByValue(user.getAttribute("role", null));
						debug.logp(Level.INFO, "AmazonPlayer", "handleMessage", "Role:"+role);
						myTurn = role == WHITE;
						break;
					}
				}
				if (myTurn && !manualInput)
				{
					makeMove();
				}
				// suggest garbage collection during "down time"
				System.runFinalization();
				System.gc();
			}
			// or if the opponent has surrendered...
			else if (type.equals(ACTION_SURRENDER))
			{
				debug.logp(Level.SEVERE, "AmazonPlayer", "handleMessage", "Opponent has surrendered!");
				debug.warning("Game ends with:"+board.getMarkedPos()+" arrows");
				//Main.closeGame(0, "Game complete - win");
			}
			// or if we have joined a room...
			else if (type.equals(ACTION_ROOM_JOINED))
			{
				debug.logp(Level.INFO, "AmazonPlayer", "handleMessage", "Joined room successfully.");
			}
			// otherwise the command is unknown...
			else
			{
				Main.closeGame(-1, "Unknown command! " + element.getContent());
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Main.closeGame(-1, "An Exception occured at AmazonPlayer#handleMessage!" + e.getMessage());
		}
		return false;
	}
	
	public AmazonRole getRole ()
	{
		return role;
	}
	
	public void handleManualInput (AmazonMove move)
	{
		debug.logp(Level.INFO, "AmazonPlayer", "handleManualInput", "Recieved manual input:"+move);
		if (myTurn && AmazonMoveValidator.validateMove(board, move))
		{
			board.applyAction(move);
			visualBoard.update(move);
			myTurn = false;
			client.sendToServer(ServerMessage.compileGameMessage(MSG_GAME, room.roomID, move.toString()), true);
		}
	}
	
	private void makeMove ()
	{
		if (board.getMarkedPos() == endGameTurnOver)
		{
			debug.info(endGameTurnOver+" moves have past, changing evaluation function for end game.");
			// reset the cutoff depths to avoid any potential problems with the search time cutoff for the new evaluations
			minimax.setCutoffDepth(1);
			statespace.setCutoffDepth(1);
		}
		// search for the next move to make
		AmazonMove nextMove;
		// if all queens are enclosed, the game becomes single player - do a state space search instead
		if (enclosed)
		{
			debug.info("Game is enclosed, using state space search.");
			nextMove = statespace.statespaceDecision(role, board);
		}
		else
		{
			statespace.setCutoffDepth(1);
			nextMove = minimax.minimaxDecision(role, role.other(), board);
		}
		// if we couldn't find a move, surrender
		if (nextMove == null)
		{
			surrender();
			return;
		}
		// finally, tell the opponent of the move
		client.sendToServer(ServerMessage.compileGameMessage(MSG_GAME, room.roomID, nextMove.toString()), true);
		debug.logp(Level.INFO, "AmazonPlayer", "makeMove", "Making move:"+nextMove);
		// apply the action to the board
		board.applyAction(nextMove);
		visualBoard.update(nextMove);
		myTurn = false;
		if (board.getMarkedPos() == endGameTurnOver)
		{
			debug.info(endGameTurnOver+" moves have past, changing evaluation function for end game.");
			minimax.setCutoffDepth(1);
		}
		enclosed = board.isEnclosed();
	}
	
	private void surrender ()
	{
		// send a surrender message to the opponent and close the game
		client.sendToServer(ServerMessage.compileGameMessage(MSG_GAME, room.roomID, "<action type='"+ACTION_SURRENDER+"'/>"));
		debug.logp(Level.SEVERE, "AmazonPlayer", "surrender", "I surrender!");
		debug.warning("Game ends with:"+board.getMarkedPos()+" arrows");
		//Main.closeGame(0, "Game complete - loss");
	}
}
