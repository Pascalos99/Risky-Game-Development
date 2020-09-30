package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import game.events.GameEvent;
import game.events.MoveEvent;
import game.events.TurnEvent;
import game.events.WinEvent;
import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import graphics.BoardGraphics;
import players.*;
import players.bots.*;

public class GamePanel extends JPanel {

	private static final long serialVersionUID = -5452925014639836146L;
	
	private BoardGraphics graphics;
	private Board game;
	private InputHandler input;
	
	public static int turn_time = 10; // in ms
	public static Color selection_color = new Color(150, 150, 150, 150);
	public static Color highlight_color = new Color(150, 150, 255, 100);
	public static Color show_move_color = new Color(255, 255, 255, 200);
	public static Color background_color= new Color(0,0,50);
	
	public static void main(String[] args) {
		Board board = new Board(null, null,new NaivePlayer(), new NaivePlayer(), new NaivePlayer(), new NaivePlayer());
		Image board_image = BoardGraphics.createBoardImage(board);
		Image[] pawns = BoardGraphics.createPawns(board);
		BoardGraphics graphics = new BoardGraphics(board, board_image, pawns);
		
		JPanel panel = new GamePanel(board, graphics);
		JFrame frame = new JFrame("Risky Checkers v.0.004");
		Dimension size = graphics.getSize();
		if (size == null)
			frame.setSize(600, 600);
		else frame.setSize(size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private Thread gameLoop;
	private Thread eventLoop;
	
	public GamePanel(Board board, BoardGraphics graphics) {
		this.graphics = graphics;
		game = board;
		graphics.setBoard(board);
		graphics.notifyUpdate();
		
		input = new InputHandler(this, board, graphics);
		addMouseListener(input);
		addKeyListener(input);
		addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				selectedNodes = null;
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});
		
		// substitute for a gameLoop
		gameLoop = new Thread(){
			private long start_time = System.currentTimeMillis();
			@Override
			public void run() {
				while (true) synchronized(this) {
					// gameTick: [we wait 'turn_time' milliseconds between turns]
					if (System.currentTimeMillis() - start_time > turn_time) {
						start_time = System.currentTimeMillis();
						
						game.forceRequestMoveAndContinue();
						
						repaint();
					}
				}
			}
		};
		
		JComponent parent = this;
		
		// loop seperate of gameLoop to handle all GameEvents
		eventLoop = new Thread() {
			
			@Override
			public synchronized void run() {
				while(true) synchronized(gameLoop) {// eventTick: no waits here, we want to respond as quickly as possible (if necessary)
					while (GameEvent.hasPending()) {
						GameEvent e = GameEvent.getNext();
						if (e instanceof TurnEvent) {
							selectedNodes = null;
							Player player = ((TurnEvent) e).getPlayer();
							if (((TurnEvent) e).isEndOfTurn()) System.out.println("----Turn Ended----\n");
							else System.out.format("=~=~ Now it's %s [%s]'s turn! ~=~=\n", player, player.getColorName());
						} else if (e instanceof MoveEvent) {
							MoveEvent m = (MoveEvent) e;
							
							selectedNodes = new BufferedImage(parent.getWidth(), parent.getHeight(), BufferedImage.TYPE_INT_ARGB);
							Graphics g = selectedNodes.getGraphics();
							for(Move move: m.getMoves()){
								BoardNode node = move.target;
								Point pos = graphics.getScreenPositionOfNode(node);
								g.setColor(show_move_color);
								int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
								g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
							}
						} else if (e instanceof WinEvent) {
							System.out.println(e.getMessage());
						}
						else {
							//System.out.println(e);
						}
					}
				}
			}
		};
		gameLoop.start();
		eventLoop.start();
	}
	
	private Image selectedNodes = null;
	
	// graphics loop:
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(background_color);
		g.fillRect(0, 0, getWidth(), getHeight());
		graphics.setSize(getWidth(), getHeight());
		g.drawImage(graphics.getImage(), 0, 0, null);
		
		BoardNode pointer = HumanPlayer.GLOBAL_INPUT.getSelectedNode();
		
		if (pointer != null) {
			Point pos = graphics.getScreenPositionOfNode(pointer);
			g.setColor( HumanPlayer.GLOBAL_INPUT.isNodeHighlighted()? highlight_color : selection_color );
			int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
			if (HumanPlayer.GLOBAL_INPUT.isNodeHighlighted()) diameter *= 1.2;
			g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
			// draw the selected nodes:
			if (selectedNodes != null && pointer.isOccupied() && pointer.getCurrentPawn().getOwner() == game.currentPlayer())
				g.drawImage(selectedNodes, 0, 0, null);
		}
		repaint();
	}

}
