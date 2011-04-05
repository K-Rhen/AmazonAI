package com.pas.cosc322.amazon.board;

import static com.pas.cosc322.amazon.main.Main.debug;
import static com.pas.cosc322.amazon.player.AmazonRole.BLACK;
import static com.pas.cosc322.amazon.player.AmazonRole.WHITE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.pas.cosc322.amazon.player.AmazonMove;
import com.paulm.jsignal.ISignal;
import com.paulm.jsignal.Signal;

/**
 * A simple visual representation of the Amazon board.
 * Feeds input to an AmazonPlayer in manual input is enabled.
 * 
 * @author Sam
 * @author Paul
 * @author Andrew
 */
public class VisualAmazonBoard extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private ImageIcon blackQueen;
	private ImageIcon redQueen;
	private ImageIcon blackArrow;
	private ImageIcon redArrow;
	private AmazonMove move;
	private String title = "Welcome to The Game of the AMAZONS";
	private JFrame frame = new JFrame(title);
	private JButton board[][] = new JButton[10][10];
	private JPanel panel = new JPanel();

	
	private boolean arrowShot = false;

	private Signal madeMove = new Signal(AmazonMove.class);
	
	/**
	 * Constructor, sets up the Game Board.
	 */
	public VisualAmazonBoard ()
	{
		makeBoard();
		makePieces();
	}

	private void makeBoard ()
	{
		// creation of the Boarder for the game.
		panel.setBorder(BorderFactory.createTitledBorder("The Game of Amazons"));
		
		panel.setLayout(new GridLayout(10, 10));
		Graphics g = null;
		panel.paintComponents(g);
		// Creates the GameBoard Representation With out any pieces
		for (int row = 0; row < 10; row++)
		{
			for (int col = 0; col < 10; col++)
			{
				board[row][col] = new JButton();
				panel.add(board[row][col]);
				board[row][col].setActionCommand(Integer.toString(row) + Integer.toString(col));
				board[row][col].addActionListener(this);
				board[row][col].setToolTipText(String.valueOf((char)(col + 'a')) + String.valueOf(9 - row));
				board[row][col].setOpaque(true);
				board[row][col].setRolloverEnabled(true);
				if ((row % 2) == (col % 2))
				{
					board[row][col].setBackground(Color.orange);
				}
				else
				{
					board[row][col].setBackground(Color.white);
				}
			}
		} 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(650, 650));
		frame.setLayout(new BorderLayout());
		frame.setLayout(new BorderLayout());
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Creates the Pieces that are being used, the Queens and the arrows.
	 */
	private void makePieces ()
	{
		try
		{
			File blkQnFile = new File("resource/blkqn.gif");
			blackQueen = new ImageIcon(ImageIO.read(blkQnFile));
			File blkarrwFile = new File("resource/blkarrw.gif");
			blackArrow = new ImageIcon(ImageIO.read(blkarrwFile));
			File rdQnFile = new File("resource/rdqn.gif");
			redQueen = new ImageIcon(ImageIO.read(rdQnFile));
			File rdarrwFile = new File("resource/rdarrw.gif");
			redArrow = new ImageIcon(ImageIO.read(rdarrwFile));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			debug.logp(Level.WARNING, "VisualAmazonBoard", "makePieces", "Error occured while reading images:"+e.getMessage());
		}
		board[0][3].setIcon(blackQueen);
		board[0][6].setIcon(blackQueen);
		board[3][0].setIcon(blackQueen);
		board[3][9].setIcon(blackQueen);
		board[6][0].setIcon(redQueen);
		board[6][9].setIcon(redQueen);
		board[9][6].setIcon(redQueen);
		board[9][3].setIcon(redQueen);
	}

	/**
	 * Updates the board's display based on a move action.
	 * 
	 * @param move the move that was played
	 */
	public void update (AmazonMove move)
	{
		ImageIcon queen = move.role == WHITE ? redQueen : blackQueen;
		ImageIcon arrow = move.role == WHITE ? redArrow : blackArrow;
		board[9 - move.qr][move.qc].setIcon(null);
		board[9 - move.qfr][move.qfc].setIcon(queen);
		board[9 - move.ar][move.ac].setIcon(arrow);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String eventName = event.getActionCommand();
		byte r = Byte.parseByte(eventName.substring(0, 1));
		byte c = Byte.parseByte(eventName.substring(1, 2));
		if (move == null)
		 
		{
			move = new AmazonMove();
			move.role = board[r][c].getIcon() == redQueen ? WHITE : BLACK;
			move.qr = (byte) (9 - r);
			move.qc = c;
		}
		else
		{
			if (arrowShot)
			{
				move.ar = (byte) (9 - r);
				move.ac = c;
				board[9 - move.qfr][move.qfc].setText("");
				madeMove.dispatch(move);
				move = null;
				arrowShot = false;
				debug.logp(Level.INFO, "VisualAmazonBoard", "actionPerformed", "Accepting new input...");
			}
			else
			{
				move.qfr = (byte) (9 - r);
				move.qfc = c;
				arrowShot = true;
				board[r][c].setText("X");
			}
		}
	}
	
	/**
	 * @return the signal for a user made input
	 */
	public ISignal madeMove ()
	{
		return madeMove;
	}
}
