package graphics;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Pawn;

import java.awt.*;
import java.awt.geom.Point2D;

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
        	Point2D coord = board.getCoordinateOfNode(b);
        	g.drawImage(pawn, (int) (coord.getX() * board_img.getWidth(null)), (int) (coord.getY() * board_img.getHeight(null)), null);
        }
    }
    
    public void notifyUpdate(){
    	request_update = true;
    }
}
