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
	
	private int current_player_ID;
	private int player_count;
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
    	
        setGraphics(graphics);
        player_count = players.length;
        this.players = Arrays.copyOf(players, 6);
        
        nodes = List.of(constructNodes());
        
        updateGraphics();
    }
    
    /* 
     * // main method for testing - by Pascal//
     * // <--- bracket '/' after "*" to be placed or removed to activate method
    public static void main(String[] args) {
    	Board board = new Board(null, null, new HumanPlayer(), new HumanPlayer(), new HumanPlayer());
    	System.out.println("created "+board);
    } //*/

    public Player getEnemy(Player player) {
    	return players[player_pairings[getPlayerIndex(player)]];
    }
    
    public List<BoardNode> getGoal(Player player) {
    	return getAllNodesOf(getEnemy(player));
    }
    
    //TODO implement the method
    public int[] getIntegerRep(){
    	// I forgot what this is supposed to do... - Pascal
        return null;
    }

    public List<BoardNode> getAllnodes() {
    	if (nodes == null) return null;
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
    
    private List<Pawn> all_pawns = null;
    
    private void updateAllPawns() {
    	List<Pawn> pawns = new ArrayList<Pawn>();
    	for (int i=0; i < players.length; i++) {
    		if (players[i] == null) continue;
    		pawns.addAll(getAllPawnsOf(players[i]));
    	}
    	all_pawns = pawns;
    }
    
    public List<Pawn> getAllPawns() {
    	if (all_pawns == null) updateAllPawns();
    	ArrayList<Pawn> result = new ArrayList<>(all_pawns.size());
    	for (int i=0; i < all_pawns.size(); i++)
    		result.add(all_pawns.get(i));
    	return result;
    }

    public boolean hasTurn(Player player){
        return player.equals(currentPlayer());
    }
    
    public Player currentPlayer() {
    	return players[current_player_ID];
    }
    
    private void nextTurn() {
    	current_player_ID++;
    	if (current_player_ID >= player_count) current_player_ID = 0;
    }

    /**
     * @param player the player to get the ID of
     * @return the index of the given Player in the {@link #players} list or {@code -1} if it is not present
     */
    public int getPlayerIndex(Player player) {
    	for (int i=0; i < players.length; i++)
    		//TODO method equals don't exist in player
    		if (player.equals(players[i])) return i;
    	return -1;
    }
    
    public List<Player> getPlayers() {
        ArrayList<Player> list = new ArrayList<Player>();
        for (int i=0; i < players.length; i++)
        	if (players[i] != null) list.add(players[i]);
        return list;
    }
    
    public int getPlayerCount() {
    	return player_count;
    }
    
    public BoardGraphics getGraphics() {
		return graphics;
	}

	public void setGraphics(BoardGraphics graphics) {
		this.graphics = graphics;
		if (graphics != null && graphics.getBoard() != this) graphics.setBoard(this);
	}
	
	public void updateGraphics() {
		if (graphics != null) graphics.notifyUpdate();
	}
    
    public String toString() {
    	StringBuilder sb = new StringBuilder(
    			String.format(
    					"Board with parameters: {\n - rules defined by %s\n - players = %s\n - nodes = (\n",
    					SELECTED_GAMERULES, Arrays.toString(players)));
    	for (BoardNode node : nodes)
    		sb.append(String.format("    + %s\n", node));
    	sb.append("   )\n}\n");
    	return sb.toString();
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
    			player_per_node[nodes_owned_per_player[p][i]] = (players[p] == null)? Player.NONE : players[p];
    	
    	BoardNode[] nodes = new BoardNode[121];
    	for (int i=0; i < nodes.length; i++) {
    		Player owner = player_per_node[i];
    		nodes[i] = new BoardNode(i, (owner == null)? null : new Pawn(owner, null), owner);
    	}
    	Map<Integer, int[]> adjacency = AdjacencyMap.getAdjacencyMap();
    	for (int i=0; i < nodes.length; i++) {
    		int[] adjacent_nodes = adjacency.get(Integer.valueOf(i));
    		for (int j=0; j < adjacent_nodes.length; j++)
    			nodes[i].addNeighbour(nodes[adjacent_nodes[j]]);
    	}
    	return nodes;
    }
    
    /**
     * @param owner player of whom to fill the field with pawns with, {@code null} if it should be randomized
     */
    public void debugPawns(Player owner) {
    	for (BoardNode node : nodes)
    		if (node.isEmpty()) {
    			Player my_owner = owner;
    			if (my_owner == null) my_owner = players[new Random().nextInt(player_count)];
    			node.addPawn(new Pawn(my_owner, node));
    		}
    	graphics.notifyUpdate();
    }
    
    /**
     * @param portion probability for a single pawn to move to a random position
     */
    public void debugPawnShuffle(double shuffle_chance) {
    	Random rand = new Random();
    	for (Pawn pawn : getAllPawns()) {
    		if (rand.nextDouble() > shuffle_chance) continue;
    		List<Integer> random_order = new ArrayList<Integer>(nodes.size());
    		for (int i=0; i < nodes.size(); i++) random_order.add(Integer.valueOf(i));
    		Collections.shuffle(random_order);
    		for (int i=0; i < random_order.size(); i++) {
    			if (nodes.get(random_order.get(i)).isEmpty()) {
    				nodes.get(random_order.get(i)).addPawn(pawn);
    				break;
    			}
    		}
    	}
    }
    
    public void debugSingleRandomMove() {
    	List<Pawn> pawns = getAllPawnsOf(currentPlayer());
    	Collections.shuffle(pawns);
    	for (Pawn pawn : pawns) {
    		List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
    		if (moves.size() > 0) {
    			Collections.shuffle(moves);
    			moves.get(0).execute();
    			graphics.notifyUpdate();
    			nextTurn();
    			return;
    		}
    	}
    }
    
}
