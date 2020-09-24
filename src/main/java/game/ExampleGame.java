package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;
import graphics.BoardGraphics;
import players.HumanPlayer;
import players.bots.RandomPlayer;

public class ExampleGame extends JComponent {
	
	private static final long serialVersionUID = -561676103755122717L;
	
	private BoardGraphics graphics;
	private Board game;
	private JFrame frame;
	private InputHandler input;
	
	public static void main(String[] args) {		
		Board board = new Board(null, null,new HumanPlayer(), new HumanPlayer(), new RandomPlayer(), new RandomPlayer(), new RandomPlayer(), new RandomPlayer());
		Image board_image = BoardGraphics.createBoardImage(board);
		Image[] pawns = BoardGraphics.createPawns(40);
		BoardGraphics graphics = new BoardGraphics(board, board_image, pawns);
		
		//board.addDebugPawns();
		//board.debugPawnShuffle(0.3);
		
		new ExampleGame(board, graphics);
	}
	
	private Thread gameLoop;
	private Thread eventLoop;
	
	public static int turn_time = 1000; // in ms
	public static Color selection_color = new Color(150,150,150,150);
	public static Color highlight_color = new Color(150,150,255,100);
	
	public ExampleGame(Board board, BoardGraphics graphics) {
		this.graphics = graphics;
		game = board;
		graphics.setBoard(board);
		graphics.notifyUpdate();
		frame = new JFrame("Risky Checkers v.0.003");
		Dimension size = graphics.getSize();
		if (size == null)
			frame.setSize(500, 400);
		else frame.setSize(size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.add(this);
		frame.setVisible(true);
		
		input = new InputHandler(frame, board, graphics);
		frame.addMouseListener(input);
		frame.addKeyListener(input);
		
		// substitute for a gameLoop
		gameLoop = new Thread(new Runnable() {
			private long start_time = System.currentTimeMillis();
			@Override
			public void run() {
				while (true) {
					// stop-condition:
					if (frame == null || !frame.isVisible()) return;
					
					// gameTick: [we wait 'turn_time' milliseconds between turns]
					if (System.currentTimeMillis() - start_time > turn_time) {
						start_time = System.currentTimeMillis();
						
						game.forceRequestMoveAndContinue();
						
						frame.repaint();
					}
				}
			}
		});
		// loop seperate of gameLoop to handle all GameEvents
		eventLoop = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while(true) {
					// stop-condition:
					if (frame == null || !frame.isVisible()) return;
					
					// eventTick: no waits here, we want to respond as quickly as possible (if necessary)
					while (GameEvent.hasPending()) {
						GameEvent e = GameEvent.getNext();
						if (e instanceof TurnEvent)
							System.out.format("----Turn Ended----\n\n=~=~ Now it's %s [%s]'s turn! ~=~=\n", ((TurnEvent) e).player, ((TurnEvent) e).player_ID);
						else if (e instanceof MoveEvent) {
							// yet to be implemented
							// TODO display all moves on screen
							MoveEvent m = (MoveEvent) e;
							System.out.println("Possible Moves: "+m.getMoves());
						}
						else {
							System.out.println(e);
						}
					}
				}
			}
		});
		gameLoop.start();
		eventLoop.start();
	}
	
	// graphics loop:
	@Override
	public void paintComponent(Graphics g) {
		graphics.setSize(frame.getWidth() - frame.getInsets().left - frame.getInsets().right, frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom);
		g.drawImage(graphics.getImage(), 0, 0, null);
		
		BoardNode pointer = HumanPlayer.GLOBAL_INPUT.getSelectedNode();
		
		if (pointer != null) {
			Point pos = graphics.getScreenPositionOfNode(pointer);
			g.setColor( HumanPlayer.GLOBAL_INPUT.isNodeHighlighted()? highlight_color : selection_color );
			
			int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
			if (HumanPlayer.GLOBAL_INPUT.isNodeHighlighted()) diameter *= 1.2;
			g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
		}
		repaint();
	}

}
