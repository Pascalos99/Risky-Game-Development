package graphics;

import java.awt.*;
import javafx.geometry.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Pawn;

public class BoardGraphics {
    private Image board_img;
    /** An array of length 2 to 6 for the different textures of pawns 
     *  (centre of the pawn image is going to be placed at the centre of a board position)  */
    private Image[] pawn_colors;
    private Image main_img;
    private Board board;
    
    private int width = -1;
    private int height = -1;
    private int scaled_width, scaled_height;
    private double scale;
    private boolean scaled_by_x;
    
    private boolean request_update;
    
    public BoardGraphics(Board board, Image board_image, Image...pawn_images) {
    	board_img = board_image;
    	pawn_colors = Arrays.copyOf(pawn_images, 6);
    	setBoard(board);
    }

    public Image getImage() {
        if (request_update || main_img == null) { updateImage(); request_update = false; }
        return main_img;
    }
    
    public Board getBoard() {
    	return board;
    }
    
    public void setBoard(Board board) {
    	this.board = board;
    	if (board != null && board.getGraphics() != this) board.setGraphics(this);
    }
    
    public Dimension getOriginalSize() {
    	if (board_img == null) return null;
    	return new Dimension(board_img.getWidth(null), board_img.getHeight(null));
    }
    
    public Dimension getSize() {
    	if (main_img == null) return null;
    	return new Dimension(scaled_width, scaled_height);
    }
    
    public void setSize(int width, int height) {
    	if (width == this.width && height == this.height) return;
    	this.width = width;
    	this.height = height;
    	updateImage();
    }
    
    public double getScale() {
    	return scale;
    }
    
    private void updateImage() {
    	if (width < 0 || height < 0) {
	    	width = board_img.getWidth(null);
	    	height = board_img.getHeight(null);
    	}
    	
    	double scaleX = ((double)width) / board_img.getWidth(null);
    	double scaleY = ((double)height) / board_img.getHeight(null);
    	
    	scaled_by_x = true;
    	scale = scaleX;
    	if (scale > scaleY) { scale = scaleY; scaled_by_x = false;}

    	scaled_width = (int) (scale * board_img.getWidth(null));
    	scaled_height = (int) (scale * board_img.getHeight(null));
    	
    	main_img = new BufferedImage(scaled_width, scaled_height, BufferedImage.TYPE_INT_ARGB);
    	Graphics mg = main_img.getGraphics();
    	mg.drawImage(board_img, 0, 0, scaled_width, scaled_height, null);
    	
        Graphics g = main_img.getGraphics();
        for (BoardNode b : board.getAllnodes()) {
        	Pawn p = b.getCurrentPawn();
        	if (p == null) continue;
        	int index = board.getPlayerIndex(p.getOwner());
        	if (index == -1) continue;
        	Image pawn = pawn_colors[index];
        	Point2D coord = getCoordinateOfNode(b);
        	
        	double pawn_width = scale * pawn.getWidth(null);
        	double pawn_height = scale * pawn.getHeight(null);
        	
        	g.drawImage(pawn, (int) (coord.getX() * ((double) scaled_width) - pawn_width / 2d), (int) (coord.getY() * ((double) scaled_height) - pawn_height / 2d), 
        			(int) pawn_width, (int) pawn_height, null);
        }
    }
    
    /**
     * Gives the coordinate of a given node on the game-board. The coordinate lies within [0,1] x [0,1] and denotes the centre
     * of the node at the Board-image used in-game (this means it will not be affected by any changes done to the board-image after BoardGraphics)
     * @param node a boardnode
     * @return a Point2D object representing the node's coordinate (centre of the node)
     * invariant: x and y must lie in the range [0,1]
     */
    public Point2D getCoordinateOfNode(BoardNode node) {

    	Point2D coord = coords_per_node[node.getID()];
    	
    	// invariant check function, do not remove
    	if (coord.getX() < 0 || coord.getX() > 1 || coord.getY() < 0 || coord.getY() > 1) throw new AssertionError("Coordinate does not fall within [0,1]x[0,1]");
    	return coord;
    }
    
    /**
     * This result may change in real-time if the screen or board is resized
     * @param node a node
     * @return a Point storing the x and y screen positions of the *origin* of the given node
     */
    public Point getScreenPositionOfNode(BoardNode node) {
    	Point2D coord = getCoordinateOfNode(node);
    	return new Point((int) Math.round(scaled_width * coord.getX()), (int) Math.round(scaled_height * coord.getY()));
    }
    
    /**
     * Gives the node that the given point is inside of (no guarantees are given when it is inside multiple nodes), based on the radius of the node given
     *   in board-image percentages (same way as the coord).
     * @param coord the [0,1]x[0,1] position of the point to be checked, where 0 denotes the most left or most top of the board-image
     *  and 1 denotes the most right or most bottom of the board-image.
     * @param node_radius the radius of the nodes in percentages of the board-image (so in [0,1])
     * @return {@code null} if the coordinate is not inside any node, a node that contains the given point if its radius is the given radius otherwise
     */
    public BoardNode getNodeAtCoordinate(Point2D coord, double node_radius) {
    	int best_ID = -1;
    	double best_distance = Double.POSITIVE_INFINITY;
    	for (int ID=0; ID < coords_per_node.length; ID++) {
    		double distance = coords_per_node[ID].distance(coord);
    		if (distance < node_radius && distance < best_distance) {
    			best_ID = ID;
    			best_distance = distance;
    		}
    	}
    	if (best_ID == -1) return null;
    	else return board.getNode(best_ID);
    }
    
    /**
     * @param screenpos The position on the screen to check (in pixel points)
     * @param node_radius The radius of the nodes (in the scale of pixels, but allowing in between values by double)
     * @param insets the insets of the screen of which the screenpos is being measured, also includes any offset of drawing the board anywhere else than (0,0)
     * @return {@code null} if the coordinate is not inside any node, a node that contains the given point if its radius is the given radius otherwise
     */
    public BoardNode getNodeAtScreenPosition(Point screenpos, double node_radius, Insets insets) {
    	Point2D coord = new Point2D(((double) (screenpos.x - insets.left)) / scaled_width, ((double) (screenpos.y - insets.top)) / scaled_height);
    	double scaled_side = scaled_by_x? scaled_width : scaled_height;
    	double scaled_radius = node_radius / scaled_side;
    	BoardNode result = getNodeAtCoordinate(coord, scaled_radius);
    	return result;
    }
    
    public void notifyUpdate(){
    	request_update = true;
    }
    
    public static double[][] d_coords_per_node = new double[121][2];
    
    private static Point2D[] coords_per_node;

    public static void updateCoordsForNodes() {
    	coords_per_node = new Point2D[d_coords_per_node.length];
    	for (int i=0; i < d_coords_per_node.length; i++)
    		coords_per_node[i] = new Point2D(d_coords_per_node[i][0], d_coords_per_node[i][1]);
    }
    
    static {
    	updateCoordsForNodes();
    }
    
    public static final int[] num_nodes_per_row = {
    		1, 2, 3, 4, 5 + 8, 6 + 6, 7 + 4, 8 + 2, 9,
    		8 + 2, 7 + 4, 6 + 6, 5 + 8, 4, 3, 2, 1
    };
    
    public static int default_board_width = 1000;
    public static int default_board_height = 1000;
    public static int default_node_diameter = 60;
    public static int default_node_spacing_x = 10;
    public static int default_node_spacing_y = -5;
    
    public static Image createBoardImage(Board board, Color[] player_colors) {
		int[] num_nodes = BoardGraphics.num_nodes_per_row;
		
		int width = default_board_width;
		int height = default_board_height;
		int circle_d = default_node_diameter;
		int space_x = default_node_spacing_x;
		int space_y = default_node_spacing_y;
		
		int tallest = num_nodes.length;
		int nodes_height = tallest * circle_d + (tallest - 1) * space_y;
		
		int offset_y = (height - nodes_height) / 2 + circle_d / 2;
		
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)(img.getGraphics());
		Color brown = new Color(151, 93, 26);
		g.setColor(brown);
		
		List<BoardNode> nodes = board.getAllnodes();
		int index = 0;
		for (int i=0; i < num_nodes.length; i++) {
			// loop through all rows
			int y = offset_y + i * (circle_d + space_y); // denotes the y-coordinate of the origin of the circle
			
			int length = num_nodes[i];
			for (int j=0; j < length; j++, index++) {
				// loop through all nodes in the row
				BoardNode node = nodes.get(index);
				Color color = brown;
				if (node.getOwner() != null) {
					int player_index = board.getPlayerIndex(node.getOwner());
					if (player_index == -1) color = player_colors[6];
					else color = player_colors[player_index];
				}
				
				int x = 0; // denotes the x-coordinate of the origin of the circle
				int deviation = j - (length - 1) / 2;
				
				if (length%2 == 1) // uneven amount of nodes in this row
					x = width / 2 + deviation * (circle_d + space_x);
				else // even amount of nodes in this row
					x = width / 2 - (circle_d + space_x) / 2 + deviation * (circle_d + space_x);
				
				g.setColor(color);
				g.fillOval(x - circle_d / 2, y - circle_d / 2, circle_d, circle_d);
				BoardGraphics.d_coords_per_node[index][0] = ((double) x) / ((double) width);
				BoardGraphics.d_coords_per_node[index][1] = ((double) y) / ((double) height);
			}
		}
		
		BoardGraphics.updateCoordsForNodes();
		return img;
	}
}
