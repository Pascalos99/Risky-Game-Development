package main.java.gamerules;

import main.java.graphics.BoardGraphics;
import main.java.players.Player;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Keeps track of player turns, the current board state and the state of the game (has it ended? did anyone win?)
 */
public class Board {

	public static GameRules SELECTED_GAMERULES = new DefaultGameRules();
	
	/**
     * player[0] is paired against player[1]
     * player[2] is paired against player[3]
     * player[4] is paired against player[5]
     */
	private static final int[] player_pairings = {
			1, 0,
			3, 2,
			5, 4};
	
    private List<BoardNode> nodes;
    private Player [] players;
    private BoardGraphics graphics; // is used to sync game updates with graphics updates

    public Board(List<BoardNode> nodes, GameRules gamerules, Player[] players, BoardGraphics graphics) {
        this.nodes = nodes;
        this.players = players;
        this.graphics = graphics;
        SELECTED_GAMERULES = gamerules;
    }

    public Player getEnemy(Player player) {
    	return players[player_pairings[getPlayerIndex(player)]];
    }
    
    public List<BoardNode> getGoal(Player player) {
    	return getAllNodesOf(getEnemy(player));
    }
    
    //TODO implement the method
    public int[] getIntegerRep(){
        return null;
    }

    public List<BoardNode> getAllnodes() {
        return Collections.unmodifiableList(nodes);
    }
    
    public List<BoardNode> getAllNodesOf(Player player) {
    	ArrayList<BoardNode> result = new ArrayList<BoardNode>();
    	for (BoardNode node : nodes)
    		if (node.getOwner().equals(player)) result.add(node);
    	return result;
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
