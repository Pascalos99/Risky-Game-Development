package gamerules;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import players.Player;
import players.bots.utils.BoardRep;
import players.bots.utils.Utils;

public class GameState {
	
	public static final byte PLAYER_PAWNS = 10;
	
	private Board original_board;
	
	/**
	 * Internal state of the GameState, is updated for most information requests from any GameState object. This object is 
	 *  shared among them and thus will not keep its state the same at most times.
	 * Please inform me (@pascal) if you intend to directly use this field for anything, as there is likely a way to solve your
	 *  problem in a safer way.
	 */
	public static Board dummy_board;
	public static GameState dummy_state;
	
	/*         /a---b---*p*
	 *    /r--<
	 * G-<     \c---d---%q%
	 *    \---...
	 */ 
	
	/**
	 * A byte representation of the GameState holds the positions of all pawns in an efficient format<br>
	 * Every 10 elements represent the pawns of a single player in increasing order of the result
	 * from {@linkplain BoardNode#getID()} of its board position. This representation is applicable to different
	 * board objects as the ID's are the position identifying aspect of each {@linkplain BoardNode}.<br><br>
	 * The length of this array is equal to {@link Board#getPlayerCount()} {@code * 10} and the pawns per player
	 * are ordered by {@linkplain Board#getPlayerIndex(Player)} starting at 0.
	 * <br><br>
	 * Example of pawn positions:<br>
	 * [0,1,2,5,8,10,29,31,45,67, 7,9,12,13,17,21,51,62,91,100]<br>
	 * means: player 0 has pawns on nodes 0, 1, 2, 5, 8, 10, 29, 31, 45, 67<br>
	 *   and: player 1 has pawns on nodes 7, 9, 12, 13, 17, 21, 51, 62, 91, 100
	 * <br><br>
	 * Get a copy of this array through {@linkplain #getIntegerRepresentation()}
	 */
	private byte[] pawn_positions;
	
	private Move last_move;
	private GameState parent;
	private GameState root;
	private int depth;
	
	private final int player_count;
	
	/**
	 * Makes sure the data in the dummy board matches this GameState
	 */
	public void setDummyBoard() {
		if (dummy_board != null && this.equals(dummy_state)) return;
		
		if (dummy_board == null || !dummy_state.root.contentEquals(root)) dummy_board = getOriginalBoard().clone();
		else for (GameState state = dummy_state; state != null; state = state.parent) {
				if (state == this) { dummy_state = this; return; }
				if (state.last_move != null) dummy_board.reverseMove(state.last_move);
		}
		
		LinkedList<Move> moves = new LinkedList<>();
		
		for (GameState state = this; state != null; state = state.parent)
			if (state.last_move != null) moves.addFirst(state.last_move);
		
		for (Move move : moves) move.execute(dummy_board);
		
		dummy_state = this;
	}
	
	/**
	 * Returns the integer representation of this GameState.
	 * @return a byte array of length {@link #getPlayerCount()} {@code * 10} with a byte for each pawn
	 * @see #pawn_positions
	 */
	public byte[] getIntegerRepresentation() {
		return Arrays.copyOf(pawn_positions, pawn_positions.length);
	}
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
	public List<Move> getAllPossibleMoves(Pawn pawn) {
		setDummyBoard();
		return GameRules.SELECTED_GAMERULES.getAllPossibleMoves(dummy_board, dummy_board.getEquivalent(pawn));
	};


	public double currentScore(Player player){
		setDummyBoard();
		return dummy_board.currentScore(player);
	}

	public Player currentPlayer() {
		return getOriginalBoard().getPlayers().get(currentPlayerID());
	}
	public int currentPlayerID() {
		return (depth + getOriginalBoard().currentPlayerID()) % getOriginalBoard().getPlayerCount();
	}
	
	public GameState getRoot() {
		return root;
	}
	
	/**
	 * @return the list of moves executed to get to this GameState (in order of execution) from this GameState's root state (this
	 *  is the furthest parent down from this GameState which is directly derived from a Board object)
	 */
	public List<Move> getMoveSequence() {
		LinkedList<Move> moves = new LinkedList<>();
		
		for (GameState state = this; state != null; state = state.parent) {
			if (state == root) break;
			moves.addFirst(state.last_move);
		}
		
		return moves;
	}
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
	public List<BoardNode> getAllnodes() {
		setDummyBoard();
		return dummy_board.getAllnodes();
	};
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
	public List<BoardNode> getAllNodesOf(Player player) {
		setDummyBoard();
		return dummy_board.getAllNodesOf(player);
	};
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
	public List<Pawn> getAllPawns() {
		setDummyBoard();
		return dummy_board.getAllPawns();
	};

	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
	public List<Pawn> getAllPawnsOf(Player owner) {
		setDummyBoard();
		return dummy_board.getAllPawnsOf(owner);
	};

	public Player getEnemy(Player player) {
		return getOriginalBoard().getEnemy(player);
	}

	public List<BoardNode> getGoal(Player player) {
		return getOriginalBoard().getGoal(player).stream().map(dummy_board.getNodeMapper()).collect(Collectors.toList());
	}
	
	public final boolean isGoalNode(Player player, BoardNode node) {
    	return node.getOwner() == getEnemy(player);
    }
	
	public int getDepth() {
		return depth;
	}
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 */
	public boolean allowMove(Pawn pawn, BoardNode target) {
		return allowMove(new Move(pawn, target));
	};
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 */
	public boolean allowMove(Move move) {
		setDummyBoard();
		return move.isValid(dummy_board);
	}

	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 */
    public boolean hasWon(Player Player) {
    	setDummyBoard();
    	return GameRules.SELECTED_GAMERULES.hasWon(dummy_board, Player);
	};
    
	/**
	 * This method may require {@link GameState#dummy_board} to be modified.<br><br>
	 */
    public boolean hasWinner() {
    	if(parent == null ){
    		if(getOriginalBoard().getWinner() != null) return true;
    		else return false;
		}
    	else if (parent.hasWinner() || getOriginalBoard().getWinner() != null) {
    		return true;
		}
    	else {
			return hasWon(parent.currentPlayer());
		}
	};
    
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * <b>[Warning]</b> results from this method from different GameStates will be modified 
	 * by any call of the following methods: <br>
	 * <l>
	 * <li>{@link #getAllnodes()}</li>
	 * <li>{@link #getAllPossibleMoves(List)}</li>
	 * <li>{@link #getAllPossibleMoves(Pawn)}</li>
	 * <li>{@link #getAllNodesOf()}</li>
	 * <li>{@link #getAllPawns()}</li>
	 * <li>{@link #getAllPawnsOf()}</li>
	 * <li>{@link #allowMove()}</li>
	 * <li>{@link #hasWon()}</li>
	 * <li>{@link #hasWinner()}</li>
	 * </l>
	 */
    public final List<Move> getAllPossibleMoves(List<Pawn> pawns) {
		ArrayList<Move> result = new ArrayList<>();
		for (Pawn pawn : pawns)
			result.addAll(getAllPossibleMoves(pawn));
		return result;
	}
	
	public GameState(Board copyFrom) {
		original_board = copyFrom;
		root = this;
		parent = null;
		last_move = null;
		depth = 0;
		player_count = copyFrom.getPlayerCount();
		pawn_positions = new byte[player_count * PLAYER_PAWNS];
		for (int i=0; i < pawn_positions.length; i++) pawn_positions[i] = -1;
		for (BoardNode node : copyFrom.getAllnodes()) {
			Pawn p = node.getCurrentPawn();
			if (p != null) {
				int id = copyFrom.getPlayerIndex(p.getOwner());
				int j = 0;
				while(pawn_positions[id * PLAYER_PAWNS + j] != -1) j++;
				pawn_positions[id * PLAYER_PAWNS + j] = (byte) node.getID();
			}
		}
	}
	
	public GameState(GameState parent, Move move) {
		original_board = parent.getOriginalBoard();
		this.parent = parent;
		this.root = parent.root;
		last_move = move;
		depth = parent.depth + 1;
		player_count = parent.player_count;
		pawn_positions = Arrays.copyOf(parent.pawn_positions, parent.pawn_positions.length);
		int player = move.getPlayerIndex(getOriginalBoard());
		int from = player * PLAYER_PAWNS;
		int to = from + PLAYER_PAWNS;
		int index = Arrays.binarySearch(pawn_positions, from, to, (byte)move.start_node);
		pawn_positions[index] = (byte)move.target_node;
		Arrays.sort(pawn_positions, from, to);
	}
	
	public GameState getStateAfterMove(Move move) {
		return new GameState(this, move);
	}
	
	public GameState getPrevious() {
		return parent;
	}
	
	public Move lastMove() {
		return last_move;
	}
	
	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 * Clones the dummy board at this GameState and stores it as this state's original board.<br>
	 * This allows for having multiple trees side by side, but it does not allow for multi-threading over these trees.
	 * (please still iterate over each tree one at a time)
	 */
	public synchronized void backupBoard() {
		setDummyBoard();
		original_board = dummy_board.clone();
		depth = 0;
	}
	
	public Board getOriginalBoard() {
		return original_board;
	}

	private BigInteger gameStateID;
	
	public BigInteger gameStateID() {
		if (gameStateID == null) {
			byte[] input = new byte[46]; // 3 bits per boardnode, for 121 boardnodes, gives 121*3/8 + 1 = 46 bytes
			for (int p=0; p < player_count; p++) {
				for (int i=0; i < PLAYER_PAWNS; i++) {
					int index = pawn_positions[p * PLAYER_PAWNS + i];
					int index_in_byte = index*3 % 8; // 3 bits per color
					byte part1 = input[index*3/8];
					byte part2 = (index*3/8+1 < 46)? input[index*3/8+1] : 0;
					//byte index is (i*3/8) and index within byte is (i*3%8)
					for (int j=0; j < 3; j++)
						if (index_in_byte + j < 8) part1 |= (((p+1) >> (2 - j)) & 1) << (index_in_byte + j);
						else part2 |= (((p+1) >> (2 - j)) & 1) << (index_in_byte + j - 8);
					
					input[index*3/8] = part1;
					if (index*3/8+1 < 46) input[index*3/8+1] = part2;
				}
			}
			gameStateID = new BigInteger(input);
		}
		
		return gameStateID;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GameState with id [");
		sb.append(gameStateID());
		sb.append("]:\n");
		for (int i=0; i < player_count; i++) {
			sb.append("  player ").append(i+1).append(": ").append(pawn_positions[i * PLAYER_PAWNS]);
			for (int j=1; j < PLAYER_PAWNS; j++) sb.append(", ").append(pawn_positions[i * PLAYER_PAWNS + j]);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public boolean contentEquals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GameState)) return false;
		GameState s = (GameState) o;
		return s.gameStateID().equals(gameStateID());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GameState)) return false;
		GameState s = (GameState) o;
		if ((s.parent != parent && s.parent == null) || (s.last_move != last_move && s.last_move == null)) return false;
		if ((s.parent == parent || s.parent.equals(parent)) && (s.last_move == last_move || s.last_move.equals(last_move))
				&& s.depth == depth && s.gameStateID().equals(gameStateID())) return true;
		return false;
	}

	public double [] getMatrixUnrolled(Player perspective){
		return getMatrixUnrolled(perspective, BoardRep.Original);
	}
	
	public double[] getMatrixUnrolled(Player perspective, BoardRep boardRep) {
		return Utils.unrollMatrix(getMatrix(perspective, boardRep));
	}
	
	public double[][][] getMatrix(Player perspective) {
		return getMatrix(perspective, BoardRep.Original);
	}
	
	public double[][][] getMatrix(Player perspective, BoardRep board_rep) {
		return getMatrix3d(original_board.getPlayerID(perspective),getIntegerRepresentation(),board_rep);
	}

	public static double [][][] getMatrix3d(int playerID,byte[] integer_rep, BoardRep board_rep) {
		Board empty = Board.empty_board;
		List<Byte> blackList = new ArrayList<>();
		List<BoardNode> nodes = empty.getAllnodes();
		List<BoardNode> allready = new ArrayList<>();
		Queue<BoardNode> queue = new LinkedList<>();
		for(int i = 0;i < 6; i++){
			if(i != playerID && i != Board.player_pairings[i]){
				for (int y = 0;y<Board.nodes_owned_per_player[i].length;y++){
					blackList.add(Board.nodes_owned_per_player[i][y]);
				}
			}
		}
		int corner;
		int opositecorner;
		if(playerID == 0){
			corner = 0;
			opositecorner = 120;
		}
		else if(playerID == 1){
			corner = 120;
			opositecorner = 0;
		}
		else if(playerID == 5){
			corner = 10;
			opositecorner = 110;
		}
		else if(playerID == 4){
			corner = 110;
			opositecorner = 10;
		}else if(playerID == 2){
			corner = 22;
			opositecorner = 98;
		}
		else{
			corner = 98;
			opositecorner = 22;
		}
		queue.add(nodes.get(corner));
		double [][] matrix = new double[9][9];
		constructMatrix(queue,allready,blackList,playerID,matrix,integer_rep);
		queue.clear();
		queue.add(nodes.get(opositecorner));
		double [][] opositeMatrix = new double[9][9];
		constructMatrix(queue,allready,blackList,playerID,opositeMatrix,integer_rep);
		for(int i = 0; i < matrix.length; i++){
			for(int j = 0; j < matrix[i].length; j++){
				if(matrix[i][j] == 0){
					matrix[i][j] = opositeMatrix[8-j][8-i];
				}
			}
		}
		double [][][] result = new double[2][9][9];
		for (int k=0; k < result.length; k++) {
			for(int i = 0; i < matrix.length; i++){
				for(int j = 0; j < matrix[i].length; j++){
					switch(board_rep) {
						case TwoNoNegatives:
							if(matrix[i][j] == ((k==0)? 1:-1) ) result[k][i][j] = 1;
							break;
						default:
							if(matrix[i][j]!=0) {
								if (k==0) {
									result[k][i][j] = matrix[i][j];
								} else result[k][i][j] = Math.abs(matrix[i][j]);
							}}}}}
		return result;
	}

	private static void constructMatrix(Queue<BoardNode> queue,List<BoardNode> allready,List<Byte> blackList,int playerID,double [][] matrix, byte [] getIntegerRepresentation){
		for(int j = 0;j < 9;j++) {
			Queue<BoardNode> Requeue = new LinkedList<>();
			int x = j;
			int y = 0;
			while (!queue.isEmpty()) {
				BoardNode node = queue.poll();
				boolean conditions = true;
				for (BoardNode check : allready) {
					if (check.getID() == node.getID()) {
						conditions = false;
						break;
					}
				}
				for (int v : blackList) {
					if (v == node.getID()) {
						conditions = false;
						break;
					}
				}
				if (conditions) {
					boolean conditions1 = false;
					boolean conditions2 = false;
					for (int i = 0; i < 10; i++) {
						if (((int) getIntegerRepresentation[i + playerID * 10]) == node.getID()) {
							matrix[x][y] = 1;
							conditions1 = true;
							break;
						}
					}
					if (!conditions1) {
						for (int pid = 0; pid < 6;pid++) {
							if (pid != playerID && !conditions2) {
								for (int i = 0; i < 10; i++) {
									if (i + pid * 10 < getIntegerRepresentation.length &&
											((int) getIntegerRepresentation[i + pid * 10]) == node.getID()) {
										matrix[x][y] = -1;
										conditions2 = true;
										break;
									}
								}
							}
						}
					}
					if (!conditions1 && !conditions2) {
						matrix[x][y] = 0;
					}
					x -=1 ;
					y +=1 ;
					allready.add(node);
					Requeue.addAll(node.getNeighbours());
				}
				if (x < 0 || y > 9) break;
			}
			queue = Requeue;
		}
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setParent(GameState parent) {
		this.parent = parent;
	}

	public void setRoot(GameState root) {
		this.root = root;
	}

}
