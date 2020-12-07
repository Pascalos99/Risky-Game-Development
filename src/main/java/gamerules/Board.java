package gamerules;

import graphics.BoardGraphics;
import players.Player;
import players.bots.DeterministicReturn;
import players.bots.utils.NodeDistanceCalc;

import java.util.*;
import java.util.function.Function;

import game.events.TurnEvent;
import game.events.WinEvent;

import static gamerules.GameRules.SELECTED_GAMERULES;

/**
 * Keeps track of player turns, the current board state and the state of the game (has it ended? did anyone win?)
 */
public class Board {

	/**
	 * player[0] is paired against player[1]
	 * player[2] is paired against player[3]
	 * player[4] is paired against player[5]
	 */
	public static final byte[] player_pairings = {
			1, 0,
			3, 2,
			5, 4};

	public static final byte[][] nodes_owned_per_player = {
			{0,1,2,3,4,5,6,7,8,9},
			{111,112,113,114,115,116,117,118,119,120},
			{19,20,21,22,32,33,34,44,45,55},
			{65,75,76,86,87,88,98,99,100,101},
			{74,84,85,95,96,97,107,108,109,110},
			{10,11,12,13,23,24,25,35,36,46}
	};
	
	public static final int[] central_goal_nodes_per_player = {116, 4, 87, 33, 24, 96};
	
	public static final int[] furthest_goal_nodes_per_player = {120, 0, 98, 22, 10, 110};

	private int current_player_ID;
	private int player_count;
	private List<BoardNode> nodes;
	private Player [] players;
	private Player winner = null;
	/** May be {@code null}; is used to sync game updates with graphics updates*/
	private BoardGraphics graphics;
	
	public static boolean preview_settings = false;

	/**
	 * Generates a Board with initial conditions based on the number of players given and sets the selected
	 * gamerules if {@code gamerules} is non-null
	 * @param gamerules
	 * @param graphics
	 * @param players
	 */
	public Board(GameRules gamerules, BoardGraphics graphics, Player...players) {
		//modif for bloking the cheaters
		//if (gamerules != null) SELECTED_GAMERULES = new ;

		setGraphics(graphics);
		player_count = players.length;
		if(players.length > 6) player_count = 6;
		this.players = Arrays.copyOf(players, 6);

		nodes = List.of(constructNodes());

		updateGraphics();

		// initial new turn
		if (player_count > 0) new TurnEvent(currentPlayer(), true);
		
		// setup up dijkstra table
		if (!NodeDistanceCalc.isTableSetup() && player_count > 0) {
			if (!NodeDistanceCalc.isCalculatingTable()) {
				Thread t = new Thread() {
					public void run() {
						NodeDistanceCalc.setupTable(getAllnodes());
					}};
				t.start();
			} else if (!preview_settings) while (NodeDistanceCalc.isCalculatingTable())
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
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
	
	public final boolean isGoalNode(Player player, BoardNode node) {
    	return node.getOwner() == getEnemy(player);
    }
	
	public double currentScore(Player player) {
		double score = 0;
		for (Pawn pawn : getAllPawnsOf(player)) {
			if (pawn.getPosition().getOwner() == player) score -= 13;
			else if (pawn.getPosition().getOwner() == getEnemy(player)) score += 13;
			else score -= NodeDistanceCalc.getDistance(nodes.get(central_goal_nodes_per_player[getPlayerIndex(player)]), pawn.getPosition());
		}
		return score;
	}
	
	public double moveScore(Move move) {
		BoardNode goal = nodes.get(central_goal_nodes_per_player[move.getPlayerIndex(this)]);
		return NodeDistanceCalc.getDistance(goal, move.getStart(this)) - NodeDistanceCalc.getDistance(goal, move.getTarget(this));
	}
	
	/**
	 * @param pawn a pawn
	 * @return the dijkstra distance to the central boardnode of the enemy territory (this is not the shortest, nor the longest distance to the territory as a whole)
	 */
	public double distanceToEnemy(Pawn pawn) {
		BoardNode goal = nodes.get(central_goal_nodes_per_player[getPlayerIndex(pawn.getOwner())]);
		return NodeDistanceCalc.getDistance(goal, pawn.getPosition());
	}
	
	/**
	 * [WARNING] This is a dangerous operation in thread-unsafe environments
	 * @param pawns the pawns you want to get the possible moves from
	 * @param moves all the moves executed before calculating the possible moves
	 * @return {@code null} if any of the subsequent moves are not valid (does not regard turn order)
	 */
	public synchronized List<Move> getPossibleMovesAfterMove(List<Pawn> pawns, Move... moves) {
		ArrayList<Move> result = new ArrayList<>();
		int moves_done_until = moves.length;
		for (int i=0; i < moves.length; i++) {
			if (moves[i].isValid(this)) moves[i].execute(this);
			else {
				moves_done_until = i;
				break;
			}
		}
		move_calculation: {
			if (moves_done_until < moves.length) {
				result = null;
				break move_calculation;
			}
			for (Pawn pawn : pawns)
				result.addAll(SELECTED_GAMERULES.getAllPossibleMoves(pawn));
		}
		for (int i=0; i < moves_done_until; i++) moves[i].reverse(this);
		return result;
	}
	
	public void executeMove(Move m) {
		m.execute(this);
	}
	
	public void reverseMove(Move m) {
		m.reverse(this);
	}
	
	public BoardNode getEquivalent(BoardNode node) {
		return nodes.get(node.getID());
	}
	
	public Pawn getEquivalent(Pawn pawn) {
		return getEquivalent(pawn.getPosition()).getCurrentPawn();
	}
	
	public Function<BoardNode, BoardNode> getNodeMapper() {
		return n -> getEquivalent(n);
	}
	
	/**
	 * [WARNING] This is a dangerous operation in thread-unsafe environments
	 * @param player the player for whom to calculate the score
	 * @param moves all the moves executed before calculating the possible moves
	 * @return {@code null} if any of the subsequent moves are not valid (does not regard turn order)
	 */
	public Double calculateScoreAfterMove(Player player, Move...moves) {
		Double result = null;
		int moves_done_until = moves.length;
		for (int i=0; i < moves.length; i++) {
			if (moves[i].isValid(this)) moves[i].execute(this);
			else {
				moves_done_until = i;
				break;
			}
		}
		score_calculation: {
			if (moves_done_until < moves.length) {
				result = null;
				break score_calculation;
			}
			result = currentScore(player);
			
		}
		for (int i=0; i < moves_done_until; i++) moves[i].reverse(this);
		return result;
	}

	//TODO implement the method
	public int[] getIntegerRep(){
		// I forgot what this is supposed to do... - Pascal
		return null;
	}

	public BoardNode getNode(int ID) {
		return nodes.get(ID);
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public List<BoardNode> getAllnodes() {
		if (nodes == null) return null;
		return Collections.unmodifiableList(nodes);
	}

	public List<BoardNode> getAllNodesOf(Player player) {
		ArrayList<BoardNode> result = new ArrayList<BoardNode>();
		for (BoardNode node : nodes){
			if (node.getOwner()==(player)){
				result.add(node);
			}
		}
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

	public Player currentPlayer() {
		return players[current_player_ID];
	}
	public int currentPlayerID() {
		return current_player_ID;
	}
	
	/**
	 * @return the next player in order
	 */
	public Player nextPlayer() {
		int next_ID = current_player_ID + 1;
		if (next_ID >= player_count) next_ID = 0;
		return players[next_ID];
	}
	
	public boolean noWinners() {
		return winner == null;
	}

	/**
	 * calls the {@link Player#returnMove(Board)} method of the {@link #currentPlayer()} and ends the turn after the move is executed.
	 * If {@link Player#returnMove(Board)} returns an invalid move or {@code null}, the turn will not be ended
	 * @return {@code true} if the turn ended as a result of this call
	 */
	public boolean requestMoveAndContinue() {
		
		// this is for testing purposes
		//GameState state = new GameState(this);
		//System.out.println("game-state now is:\n"+state);
		//
		
		if (player_count <= 0) return true;
		Move move = currentPlayer().returnMove(this);
		if (move != null && move.isValid(this)) {
			executeMoveForReal(move);
			updateGraphics();
			nextTurn();
			return true;
		}
		return false;
	}

	/**
	 * Keeps requesting {@link Player#returnMove(Board)} from the {@link #currentPlayer()} until a valid move is returned, then executes the
	 * move and ends this turn.
	 * <br><br>
	 * If the {@link #currentPlayer()} is a {@link players.bots.DeterministicReturn} object, this will only request a move <b>once</b> and execute
	 * {@link #forceEndTurn()} if the given move is {@code null} or invalid.
	 */
	public void forceRequestMoveAndContinue() {
		
		if (player_count <= 0) return;
		if (currentPlayer() instanceof DeterministicReturn) {
			if (!requestMoveAndContinue()) forceEndTurn();
			return;
		}
		Move move = null;
		while (move == null || !move.isValid(this))
			move = currentPlayer().returnMove(this);
		executeMoveForReal(move);
		updateGraphics();
		nextTurn();
	}
	
	/**
	 * Executed a move that can come from any board from any state such that no Move mismatch can occur
	 * @param move the move to be executed
	 */
	private void executeMoveForReal(Move move) {
		new GameState(this).setDummyBoard();
		move.execute(this);
	}

	public boolean hasTurn(Player player){
		return player.equals(currentPlayer());
	}

	private void nextTurn() {
		if (player_count <= 0) return;
		currentPlayer().turnCounter.count++;
		new TurnEvent(currentPlayer(), false); // end previous turn
		if(!SELECTED_GAMERULES.hasWon(this,currentPlayer())){
			current_player_ID++;
			if (current_player_ID >= player_count) current_player_ID = 0;
			new TurnEvent(currentPlayer(), true); // start new turn
		} else if (winner == null) {
			winner = currentPlayer();
			new WinEvent(currentPlayer(), currentPlayer().turnCounter.count);
		}
	}

	/**
	 * executes a random move for the {@link #currentPlayer()} if any moves are available. Then ends the turn.
	 */
	public void forceEndTurn() {
		List<Pawn> pawns = getAllPawnsOf(currentPlayer());
		Collections.shuffle(pawns);
		for (Pawn pawn : pawns) {
			List<Move> moves = SELECTED_GAMERULES.getAllPossibleMoves(pawn);
			if (moves.size() > 0) {
				Collections.shuffle(moves);
				moves.get(0).execute(this);
				break;
			}
		}
		updateGraphics();
		nextTurn();
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
	
	/**
	 * @param player_index index of the player to be fetched
	 * @return {@code null} if the index is not valid; the corresponding player to the index otherwise.
	 */
	public Player getPlayer(int player_index) {
		if (player_index < 0 || player_index > players.length) return null;
		return players[player_index];
	}
	
	/**
	 * Alias for {@link Board#getPlayerIndex(Player)}
	 */
	public int getPlayerID(Player player) {
		return getPlayerIndex(player);
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
			nodes[i] = new BoardNode(i, (owner == null || owner == Player.NONE)? null : new Pawn(owner, null), owner);
		}
		Map<Integer, int[]> adjacency = DirectedAdjacencyMap.getAdjacencyMap();
		for (int i=0; i < nodes.length; i++) {
			int[] neighbours = adjacency.get(i);
			for (int j=0; j < neighbours.length; j++)
				if (neighbours[j] != DirectedAdjacencyMap.NULL)
					nodes[i].addNeighbour(nodes[neighbours[j]], j);
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
		updateGraphics();
	}

	/**
	 * @param shuffle_chance probability for a single pawn to move to a random position
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

	public Board clone() {
		Board clone = new Board(null, null, Arrays.copyOf(players, player_count));
		clone.current_player_ID = this.current_player_ID;
		clone.winner = this.winner;
		clone.player_count = this.player_count;

		for (BoardNode node : clone.nodes) {
			node.removePawn();
			if (getNode(node.getID()).isOccupied()) {
				node.addPawn(getNode(node.getID()).getCurrentPawn().clone(node));
			}
		}

		return clone;
	}

	public Player getWinner() {
		return winner;
	}

}
