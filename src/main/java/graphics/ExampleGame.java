package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

import gamerules.Board;
import gamerules.BoardNode;
import players.HumanPlayer;

public class ExampleGame extends JComponent {
	
	private static final long serialVersionUID = -561676103755122717L;
	
	private BoardGraphics graphics;
	private Board game;
	private JFrame frame;
	
	public static int circle_diameter = 40;
	
	public static void main(String[] args) {
		Image[] pawns = new Image[6];
		Color[] colors = {Color.green, Color.blue, Color.yellow, Color.magenta, Color.orange, Color.red};
		Color[] home_colors = {new Color(150, 205, 113), new Color(133, 178, 205), new Color(250, 217, 73),
							new Color(205, 105, 164), new Color(237, 163, 6), new Color(209, 58, 34), new Color(181, 126, 63)};
		
		for (int i=0; i < pawns.length; i++) {
			BufferedImage pawn = new BufferedImage(circle_diameter + 10, circle_diameter + 10, BufferedImage.TYPE_INT_ARGB);
			Graphics g = pawn.getGraphics();
			g.setColor(colors[i]);
			g.fillOval(5, 5, circle_diameter, circle_diameter);
			pawns[i] = pawn;
		}
		
		Board board = new Board(null, null, new HumanPlayer(), new HumanPlayer(), new HumanPlayer(), new HumanPlayer());
		Image board_image = BoardGraphics.createBoardImage(board, home_colors);
		
		
		BoardGraphics graphics = new BoardGraphics(board, board_image, pawns);
		
		//board.addDebugPawns();
		//board.debugPawnShuffle(0.3);
		
		new ExampleGame(board, graphics);
	}
	
	private Thread timer;
	private int turn_time = 1000; // in ms
	
	public ExampleGame(Board board, BoardGraphics graphics) {
		this.graphics = graphics;
		game = board;
		graphics.setBoard(board);
		graphics.notifyUpdate();
		frame = new JFrame();
		Dimension size = graphics.getSize();
		if (size == null)
			frame.setSize(500, 400);
		else frame.setSize(size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.add(this);
		frame.setVisible(true);
		
		frame.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {pointer = graphics.getNodeAtScreenPosition(e.getPoint(), circle_diameter/2d, frame.getInsets());
				if (pointer != null) {
					Point pos = graphics.getScreenPositionOfNode(pointer);
				}
				repaint();
			}
		});
		
		timer = new Thread(new Runnable() {
			private long start_time = System.currentTimeMillis();
			JFrame f = frame;
			@Override
			public void run() {
				while (true)
					if (System.currentTimeMillis() - start_time > turn_time * 2) break;
				while (true) {
					if (f == null || !f.isVisible()) return;
					if (System.currentTimeMillis() - start_time > turn_time) {
						start_time = System.currentTimeMillis();
						game.debugSingleRandomMove();
						frame.repaint();
					}
				}
			}
		});
		timer.start();
	}
	
	private BoardNode pointer;
	
	@Override
	public void paintComponent(Graphics g) {
		graphics.setSize(frame.getWidth() - frame.getInsets().left - frame.getInsets().right, frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom);
		g.drawImage(graphics.getImage(), 0, 0, null);
		
		if (pointer != null) {
			Point pos = graphics.getScreenPositionOfNode(pointer);
			g.setColor(Color.BLACK);
			g.fillOval(pos.x - 10, pos.y - 10, 20, 20);
		}
	}

}
