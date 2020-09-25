package game;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import gamerules.Board;
import gamerules.BoardNode;
import graphics.BoardGraphics;
import players.HumanPlayer;

public class InputHandler extends MouseAdapter implements KeyListener {
	
	private Board board;
	private BoardGraphics graphics;
	private Frame frame;
	
	private List<Integer> keys_held_down;
	private List<Integer> keys_just_pressed;
	
	private volatile BoardNode selectedNode;
	private volatile boolean nodeIsHighlighted;
	
	public InputHandler(Frame frame, Board board, BoardGraphics graphics) {
		this.frame = frame;
		this.board = board;
		this.graphics = graphics;
		keys_held_down = new ArrayList<Integer>();
		keys_just_pressed = new ArrayList<Integer>();
		HumanPlayer.GLOBAL_INPUT = this;
	}

	@Override
	public void keyTyped(KeyEvent e) { }
	
	@Override
	public void keyPressed(KeyEvent e) {
		Integer code = Integer.valueOf(e.getKeyCode());
		if (!keys_held_down.contains(code)) {
			keys_held_down.add(code);
			keys_just_pressed.add(code);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Integer code = Integer.valueOf(e.getKeyCode());
		if (keys_held_down.contains(code))
			keys_held_down.remove(code);
	}
	
	public void tick() {
		keys_just_pressed.clear();
	}
	
	public boolean justPressedKey(int keyCode) {
		return keys_just_pressed.contains(Integer.valueOf(keyCode));
	}
	
	public boolean isKeyHeldDown(int keyCode) {
		return keys_held_down.contains(Integer.valueOf(keyCode));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		setSelectedNode(graphics.getNodeAtScreenPosition(e.getPoint(), BoardGraphics.default_node_diameter / 2d, frame.getInsets()));
	}
	
	/**
	 * @return the currently selected {@link BoardNode} on the board, or {@code null} if none is selected
	 */
	public BoardNode getSelectedNode() {
		return selectedNode;
	}
	
	public void setSelectedNode(BoardNode node) {
		if (node != selectedNode || node == null) nodeIsHighlighted = false;
		selectedNode = node;
	}
	
	public void setHighlighted(boolean set) {
		nodeIsHighlighted = set;
	}
	public boolean isNodeHighlighted() {
		return nodeIsHighlighted;
	}
	
}
