package gamerules;

import graphics.BoardGraphics;
import players.Player;

import java.awt.geom.Point2D;
import java.util.*;

public class Board {

    private List<BoardNode> nodes;
    private GameRules gamerules; // is used to validate moves made by players and to decide when the game ends
    private Player [] players;
    private BoardGraphics graphics; // is used to sync game updates with graphics updates

    Board(List<BoardNode> nodes, GameRules gamerules, Player [] players,BoardGraphics graphics) {
        this.nodes = nodes;
        this.gamerules = gamerules;
        this.players = players;
        this.graphics = graphics;
    }

    //TODO implement the method
    public int[] getIntegerRep(){
        return null;
    }

    public List<BoardNode> getAllnodes() {
        return Collections.unmodifiableList(nodes);
    }
    
    public List<Pawn> getAllPawnsOf(Player owner) {
    	List<Pawn> pawns = new ArrayList<Pawn>();
    	for (BoardNode node : nodes)
    		if (node.getCurrentPawn() != null && node.getCurrentPawn().getOwner().equals(owner))
    			pawns.add(node.getCurrentPawn());
    	return pawns;
    }

    //TODO implement the method
    public boolean hasTurn(Player player){
        return false;
    }

    /**
     * @param player the player to get the ID of
     * @return the index of the given Player in the {@link #players} list or {@code -1} if it is not present
     */
    public int getPlayerIndex(Player player) {
    	for (int i=0; i < players.length; i++)
    		if (player.equals(players[i])) return i;
    	return -1;
    }
    
    /**
     * Gives the coordinate of a given node on the game-board. The coordinate lies within [0,1] x [0,1] and denotes the centre
     * of the node at the Board-image used in-game (this means it will not be affected by any changes done to the board-image after BoardGraphics)
     * @param node a boardnode
     * @return a Point2D object representing the node's coordinate
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
    
    public List<Player> getPlayers() {
        return List.of(players);
    }
}
