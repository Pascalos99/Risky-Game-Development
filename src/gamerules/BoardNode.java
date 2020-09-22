package gamerules;

import players.Player;

public class BoardNode {

    private int nodeID;
    private Pawn occupying_Pawn;
    private BoardNode [] adjacent_node;
    private Player player_Home;

    BoardNode(int nodeID,Pawn occupying_Pawn,BoardNode [] adjacent_node,Player player_Home) {
        this.nodeID = nodeID;
        this.occupying_Pawn = occupying_Pawn;
        this.adjacent_node = adjacent_node;
        this.player_Home = player_Home;
    }

    //TODO implement the method
    protected boolean addPawn(Pawn newPawn) {
        return false;
    }

    //TODO implement the method
    protected boolean removePawn(){
        return false;
    }

    public Pawn getCurrentPawn() {
        return occupying_Pawn;
    }

    public BoardNode[] getNeighbors() {
        return adjacent_node;
    }

    public Player getOwner() {
        return player_Home;
    }
}
