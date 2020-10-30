package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

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

public class GamePanel extends JPanel {

	private static final long serialVersionUID = -5452925014639836146L;
	
	private BoardGraphics graphics;
	private Board game;
	private InputHandler input;
	
	public static int turn_time = 500; // in ms
	public static Color selection_color = new Color(150, 150, 150, 150);
	public static Color highlight_color = new Color(150, 150, 255, 100);
	public static Color show_move_color = new Color(200, 200, 255, 200);
	public static Color background_color= new Color(193, 154, 107, 255);
	
	public static void main(String[] args) {
		GameSetup gs = new GameSetup();
		gs.addPlayer("Naive player", "Henry", Color.WHITE);
		gs.addPlayer("Naive player", "Melissa", new Color(78,0,0));
		
		JPanel panel = gs.build();
		JFrame frame = new JFrame("Risky Checkers v.0.004");
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	private Thread gameLoop;
	
	public GamePanel(Board board, BoardGraphics graphics) {
		this.graphics = graphics;
		game = board;
		graphics.setBoard(board);
		graphics.notifyUpdate();
		
		boolean only_bots = true;
		for (Player p : board.getPlayers())
			if (p instanceof HumanPlayer) only_bots = false;
		if (only_bots) turn_time = 50;
		
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
			@Override
			public void run() {
				while (game.noWinners()) synchronized(this) {
					// gameTick: [we wait 'turn_time' milliseconds between turns]
					Thread turn = new Thread() {
						public void run() {
							gameLoop();
						}
					};
					Player next = game.nextPlayer();
					try {
						if (!(next instanceof HumanPlayer)) {
							turn.start();
							Thread.sleep(turn_time);
						} else
							turn.start();
						turn.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		gameLoop.start();
		main_panel = this;
	}
	
	private Image selectedNodes = null;
	
	private void gameLoop() {
		game.forceRequestMoveAndContinue();
		repaint();
	}
	
	public static void forceEventUpdate() {
		if (main_panel != null) main_panel.eventLoop();
	}
	
	private static GamePanel main_panel;
	
	public void eventLoop() {
		while (GameEvent.hasPending()) {
			GameEvent e = GameEvent.getNext();
			if (e instanceof TurnEvent) {
				selectedNodes = null;
				Player player = ((TurnEvent) e).getPlayer();
				if (((TurnEvent) e).isEndOfTurn()) System.out.println("----Turn Ended----\n");
				else System.out.format("=~=~ Now it's %s [%s]'s turn! ~=~=\n", player.getName()+" ("+player.getTypeName()+")", player.getColorName());
			} else if (e instanceof MoveEvent) {
				MoveEvent m = (MoveEvent) e;
				
				selectedNodes = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
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
	
	// graphics loop:
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(background_color);
		g.fillRect(0, 0, getWidth(), getHeight());
		graphics.setSize(getWidth(), getHeight());
		g.drawImage(graphics.getImage(), 0, 0, null);
		
		eventLoop();
		
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
		
		if (game.getPlayerCount() <= 0) {
			g.drawImage(graphics.createDebugImage(), 0, 0, null);
		}
	}

}
