package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import players.Player;
import static gamerules.DirectedAdjacencyMap.*;

public class BoardNode {

	public static final int TOPLEFT = INDEX_TOPLEFT;
	public static final int TOPRIGHT = INDEX_TOPRIGHT;
	public static final int LEFT = INDEX_LEFT;
	public static final int RIGHT = INDEX_RIGHT;
	public static final int BOTTOMLEFT = INDEX_BOTTOMLEFT;
	public static final int BOTTOMRIGHT = INDEX_BOTTOMRIGHT;
	
    private int nodeID;
    private Pawn occupying_pawn;
    private List<BoardNode> adjacent_nodes;
    private List<Integer> adjn_directions;
    private Player player_home;
    private double gScore;
    private double fScore;
    private BoardNode parent;

    public BoardNode getParent() {
        return parent;
    }

    public void setParent(BoardNode parent) {
        this.parent = parent;
    }

    public double getgScore() {
        return gScore;
    }

    public void setgScore(double gScore) {
        this.gScore = gScore;
    }

    public double getfScore() {
        return fScore;
    }

    public void setfScore(double fScore) {
        this.fScore = fScore;
    }

    public BoardNode(int nodeID, Pawn occupying_pawn, Player player_home) {
        this.nodeID = nodeID;
        this.occupying_pawn = occupying_pawn;
        this.player_home = player_home;
        adjacent_nodes = new ArrayList<>();
        adjn_directions = new ArrayList<>();
        if (occupying_pawn != null) occupying_pawn.setPosition(this);
    }

    protected boolean addNeighbour(BoardNode node, int direction) {
    	if (adjacent_nodes.contains(node)) return false;
    	boolean result = adjacent_nodes.add(node);
    	if (result) adjn_directions.add(direction);
    	return result;
    }
    
    protected boolean removeNeighbour(BoardNode node) {
    	int index_of = adjacent_nodes.indexOf(node);
    	boolean result = adjacent_nodes.remove(node);
    	if (result) adjn_directions.remove(index_of);
    	return result;
    }
    public boolean isNeighbour(BoardNode node) {
    	return adjacent_nodes.contains(node);
    }
    
    protected boolean addPawn(Pawn newPawn) {
        if (isOccupied()) return false;
        occupying_pawn = newPawn;
        if (newPawn.getPosition() != this) newPawn.setPosition(this);
        return true;
    }

    protected boolean removePawn(){
        if (isEmpty()) return false;
        occupying_pawn = null;
        return true;
    }

    /**
     * @return returns the pawn currently occupying this tile (node)
     */
    public Pawn getCurrentPawn() {
        return occupying_pawn;
    }
    
    /**
     * {@code true} if there is no pawn on this tile
     * @return getCurrentPawn() == null
     */
    public boolean isEmpty() {
    	return getCurrentPawn() == null;
    }
    
    /**
     * {@code true} if there is a pawn on this tile
     * @return !isEmpty()
     */
    public boolean isOccupied() {
    	return !isEmpty();
    }

    public List<BoardNode> getNeighbours() {
        return Collections.unmodifiableList(adjacent_nodes);
    }
    public List<Integer> getDirections() {
    	return Collections.unmodifiableList(adjn_directions);
    }
    
    /**
     * @param node a neighbouring node
     * @return the direction of the given node relative to this one, or {@code -1} if the given
     *   node does not neighbour this node.
     */
    public int getDirectionOf(BoardNode node) {
    	int index = adjacent_nodes.indexOf(node);
    	if (index < 0) return -1;
    	return adjn_directions.get(index);
    }
    
    public BoardNode getNeighbourToThe(int direction) {
    	int index = adjn_directions.indexOf(direction);
    	if (index < 0) return null;
    	return adjacent_nodes.get(index);
    }

    /**
     * @return the owner of this Node (is the color of the node on the board)
     * {@code null} if this is a plain tile
     */
    public Player getOwner() {
        return player_home;
    }
    
    public boolean isGoal(Player player, Board board) {
    	return board.isGoalNode(player, this);
    }
    
    public int getID() {
    	return nodeID;
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder("Node["+nodeID+"]:{owner=");
    	if (player_home == null) sb.append("None");
    	else sb.append(player_home);
    	sb.append("; neighbours: ");
    	
    	for (BoardNode node : adjacent_nodes)
    		sb.append(node.nodeID+" ("+DirectedAdjacencyMap.DIRECTION_NAMES[getDirectionOf(node)]+"), ");
    	
    	sb.delete(sb.length() - 2, sb.length());
    	
    	sb.append("; pawn = ");
    	if (isEmpty()) sb.append("None");
    	else sb.append(occupying_pawn);
    	sb.append("}");
    	return sb.toString();
    }
}
