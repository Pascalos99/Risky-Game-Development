package gamerules;

import graphics.BoardGraphics;
import players.Player;

import java.awt.geom.Point2D;
import java.util.*;

public class Board {

	public static GameRules SELECTED_GAMERULES = new DefaultGameRules();
	
    private List<BoardNode> nodes;
    private Player [] players;
    private BoardGraphics graphics; // is used to sync game updates with graphics updates

    public Board(List<BoardNode> nodes, GameRules gamerules, Player[] players, BoardGraphics graphics) {
        this.nodes = nodes;
        this.players = players;
        this.graphics = graphics;
        SELECTED_GAMERULES = gamerules;
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
    
    public List<Player> getPlayers() {
        return List.of(players);
    }
}
