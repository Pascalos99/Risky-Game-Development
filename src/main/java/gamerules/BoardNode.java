package gamerules;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import players.Player;

public class BoardNode {

    private int nodeID;
    private Pawn occupying_pawn;
    private Set<BoardNode> adjacent_nodes;
    private Player player_home;

    public BoardNode(int nodeID, Pawn occupying_pawn, Player player_home) {
        this.nodeID = nodeID;
        this.occupying_pawn = occupying_pawn;
        this.player_home = player_home;
        adjacent_nodes = new HashSet<>();
        if (occupying_pawn != null) occupying_pawn.setPosition(this);
    }

    protected boolean addNeighbour(BoardNode node) {
    	if (adjacent_nodes.contains(node)) return false;
    	return adjacent_nodes.add(node);
    }
    
    protected boolean removeNeighbour(BoardNode node) {
    	return adjacent_nodes.remove(node);
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

    public void setAdjacent_nodes(List<BoardNode> adjacent_nodes) {
        this.adjacent_nodes = Set.copyOf(adjacent_nodes);
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

    public Set<BoardNode> getNeighbours() {
        return Collections.unmodifiableSet(adjacent_nodes);
    }

    /**
     * @return the owner of this Node (is the color of the node on the board)
     * {@code null} if this is a plain tile
     */
    public Player getOwner() {
        return player_home;
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
    		sb.append(node.nodeID+", ");
    	
    	sb.delete(sb.length() - 2, sb.length());
    	
    	sb.append("; pawn = ");
    	if (isEmpty()) sb.append("None");
    	else sb.append(occupying_pawn);
    	sb.append("}");
    	return sb.toString();
    }
}
