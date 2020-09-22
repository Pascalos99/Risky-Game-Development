package gamerules;

import java.util.Collections;
import java.util.List;

import players.Player;

public class BoardNode {

    private int nodeID;
    private Pawn occupying_pawn;
    private List<BoardNode> adjacent_nodes;
    private Player player_home;

    protected BoardNode(int nodeID, Pawn occupying_pawn, BoardNode[] adjacent_nodes, Player player_home) {
        this.nodeID = nodeID;
        this.occupying_pawn = occupying_pawn;
        this.adjacent_nodes = List.of(adjacent_nodes);
        this.player_home = player_home;
    }

    protected boolean addPawn(Pawn newPawn) {
        if (occupying_pawn != null) return false;
        occupying_pawn = newPawn;
        return true;
    }

    protected boolean removePawn(){
        if (occupying_pawn == null) return false;
        occupying_pawn = null;
        return true;
    }

    public Pawn getCurrentPawn() {
        return occupying_pawn;
    }

    public List<BoardNode> getNeighbors() {
        return Collections.unmodifiableList(adjacent_nodes);
    }

    public Player getOwner() {
        return player_home;
    }
    
    public int getID() {
    	return nodeID;
    }
}
