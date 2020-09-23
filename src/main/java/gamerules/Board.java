package gamerules;

import graphics.BoardGraphics;
import players.Player;
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
	
	private static final int[][] nodes_owned_per_player = {
			{0,1,2,3,4,5,6,7,8,9},
			{111,112,113,114,115,116,117,118,119,120},
			{19,20,21,22,32,33,34,44,45,55},
			{65,75,76,86,87,88,98,99,100,101},
			{74,84,85,95,96,97,107,108,109,110},
			{10,11,12,13,23,24,25,35,36,46}
	};
	
    private List<BoardNode> nodes;
    private Player [] players;
    /** May be {@code null}; is used to sync game updates with graphics updates*/
    private BoardGraphics graphics;

    /**
     * Generates a Board with initial conditions based on the number of players given and sets the selected
     * gamerules if {@code gamerules} is non-null
     * @param gamerules
     * @param graphics
     * @param players
     */
    public Board(GameRules gamerules, BoardGraphics graphics, Player...players) {
    	
    	if (gamerules != null) SELECTED_GAMERULES = gamerules;
    	
        this.graphics = graphics;
        this.players = Arrays.copyOf(players, 6);
        
        nodes = List.of(constructNodes());
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
    
    /**
     * Generates all nodes with their correct indices, owners, connections and beginning pawns set.
     * This also instantiates all 10 * player_count pawns necessary for the game to start.
     * requires {@link #players} to be instantiated and filled
     * @return All nodes to be added to the board
     */
    private BoardNode[] constructNodes() {
    	Player[] player_per_node = new Player[121];
    	for (int p=0; p < nodes_owned_per_player.length; p++)
    		for (int i=0; i < nodes_owned_per_player[p].length; i++)
    			player_per_node[i] = players[p];
    	
    	BoardNode[] nodes = new BoardNode[121];
    	for (int i=0; i < nodes.length; i++) {
    		Player owner = player_per_node[i];
    		nodes[i] = new BoardNode(i, (owner == null)? null : new Pawn(owner, null), owner);
    	}
    	Map<Integer, int[]> adjacency = AdjacencyMap.getAdjacencyMap();
    	for (int i=0; i < nodes.length; i++) {
    		int[] adjacent_nodes = adjacency.get(Integer.valueOf(i));
    		for (int j=0; j < adjacent_nodes.length; j++)
    			nodes[i].addNeighbor(nodes[j]);
    	}
    	return nodes;
    }
    
}
