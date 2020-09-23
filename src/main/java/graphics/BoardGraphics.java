package main.java.graphics;

import java.awt.*;
import java.awt.geom.Point2D;

import main.java.gamerules.Board;
import main.java.gamerules.BoardNode;
import main.java.gamerules.Pawn;

public class BoardGraphics {
    private Image board_img;
    /** An array of length 2 to 6 for the different textures of pawns 
     *  (centre of the pawn image is going to be placed at the centre of a board position)  */
    private Image[] pawn_colors;
    private Image main_img;
    private Board board;
    
    private boolean request_update;

    public Image getImage() {
        if (request_update || main_img == null) { updateImage(); request_update = false; }
        return main_img;
    }
    
    private void updateImage() {
    	main_img = board_img.getScaledInstance(-1, -1, Image.SCALE_DEFAULT);
        Graphics g = main_img.getGraphics();
        for (BoardNode b : board.getAllnodes()) {
        	Pawn p = b.getCurrentPawn();
        	if (p == null) continue;
        	int index = board.getPlayerIndex(p.getOwner());
        	if (index == -1) continue;
        	Image pawn = pawn_colors[index];
        	Point2D coord = getCoordinateOfNode(b);
        	g.drawImage(pawn, (int) (coord.getX() * board_img.getWidth(null)), (int) (coord.getY() * board_img.getHeight(null)), null);
        }
    }
    
    /**
     * Gives the coordinate of a given node on the game-board. The coordinate lies within [0,1] x [0,1] and denotes the centre
     * of the node at the Board-image used in-game (this means it will not be affected by any changes done to the board-image after BoardGraphics)
     * @param node a boardnode
     * @return a Point2D object representing the node's coordinate (centre of the node)
     * invariant: x and y must lie in the range [0,1]
     */
    public Point2D.Double getCoordinateOfNode(BoardNode node) {
    	// not implemented yet
    	// TODO
    	Point2D.Double coord = new Point2D.Double();
    	
    	// invariant check function, do not remove
    	if (coord.x < 0 || coord.x > 1 || coord.y < 0 || coord.y > 1) throw new AssertionError("Coordinate does not fall within [0,1]x[0,1]");
    	return coord;
    }
    
    public void notifyUpdate(){
    	request_update = true;
    }
}
