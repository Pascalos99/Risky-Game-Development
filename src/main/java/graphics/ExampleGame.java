package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;

import gamerules.Board;
import players.HumanPlayer;

public class ExampleGame extends JComponent {
	
	private static final long serialVersionUID = -561676103755122717L;
	
	private BoardGraphics graphics;
	private Board game;
	private JFrame frame;
	
	public static void main(String[] args) {
		Image[] pawns = new Image[6];
		Color[] colors = {Color.green, Color.blue, Color.yellow, Color.magenta, Color.orange, Color.red};
		Color[] home_colors = {new Color(150, 205, 113), new Color(133, 178, 205), new Color(250, 217, 73),
							new Color(205, 105, 164), new Color(237, 163, 6), new Color(209, 58, 34), new Color(181, 126, 63)};
		
		for (int i=0; i < pawns.length; i++) {
			BufferedImage pawn = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
			Graphics g = pawn.getGraphics();
			g.setColor(colors[i]);
			g.fillOval(5, 5, 40, 40);
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
	
	@Override
	public void paintComponent(Graphics g) {
		graphics.setSize(frame.getWidth(), frame.getHeight());
		g.drawImage(graphics.getImage(), 0, 0, null);
	}

}
