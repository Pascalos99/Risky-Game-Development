package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import players.Player;

public class BoardNode {

    private int nodeID;
    private Pawn occupying_pawn;
    private List<BoardNode> adjacent_nodes;
    private Player player_home;

    public BoardNode(int nodeID, Pawn occupying_pawn, Player player_home) {
        this.nodeID = nodeID;
        this.occupying_pawn = occupying_pawn;
        this.player_home = player_home;
        adjacent_nodes = new ArrayList<>();
        if (occupying_pawn != null) occupying_pawn.setPosition(this);
    }

    protected boolean addNeighbour(BoardNode node) {
    	if (adjacent_nodes.contains(node)) return false;
    	return adjacent_nodes.add(node);
    }
    
    protected boolean removeNeighbour(BoardNode node) {
    	return adjacent_nodes.remove(node);
    }
    
    protected boolean addPawn(Pawn newPawn) {
        if (occupying_pawn != null) return false;
        occupying_pawn = newPawn;
        newPawn.setPosition(this);
        return true;
    }

    protected boolean removePawn(){
        if (occupying_pawn == null) return false;
        occupying_pawn = null;
        return true;
    }

    public void setAdjacent_nodes(List<BoardNode> adjacent_nodes) {
        this.adjacent_nodes = List.copyOf(adjacent_nodes);
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
    	sb.append(adjacent_nodes.get(0).nodeID);
    	for (int i=1; i < adjacent_nodes.size(); i++)
    		sb.append(", "+adjacent_nodes.get(i).nodeID);
    	sb.append("; pawn = ");
    	if (occupying_pawn == null) sb.append("None");
    	else sb.append(occupying_pawn);
    	sb.append("}");
    	return sb.toString();
    }
}
