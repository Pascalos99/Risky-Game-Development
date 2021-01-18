package gamerules;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import game.GameSetup;
import graphics.sample.AssetFinder;
import players.Player;
import players.bots.NaivePlayer;
import players.bots.utils.BoardRep;
import players.bots.utils.Utils;

public class GameState {

	public static final byte PLAYER_PAWNS = 10;

	private Board original_board;
	
	public static void testEndCounts() {
		File end_file = new File(AssetFinder.assetsPath+"\\training_data\\complete_testing.end_data");
		try {
			int count = 0;
			int count_no_win = 0;
			String end_data = Files.readString(end_file.toPath());
			String[] end_lines = end_data.split("\n");
			for (int i=0; i < end_lines.length; i++) {
				double[] d_res = Utils.extractVector(Utils.parseMatrix(end_lines[i]));
				byte[] res = new byte[d_res.length-2];
				for (int j=0; j < d_res.length-2; j++) res[j] = (byte) d_res[j+2];
				System.out.println(end_lines[i]);
				System.out.println(Arrays.toString(res));
				System.out.println("winner is "+getWinner(res));
				count++;
				if (getWinner(res) == -1) count_no_win++;
			}
			System.out.println("count: "+count+", no-win: "+count_no_win);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getWinner(byte[] integer_rep) {
		for (int p=0; p < integer_rep.length / 10; p++) {
			byte[] goal = Board.nodes_owned_per_player[Board.player_pairings[p]];
			Arrays.sort(goal);
			boolean yes = true;
			for (int i=0; i < goal.length; i++)
				if (goal[i] != integer_rep[p*10 + i]) yes = false;
			if (yes) return p;
		}
		return -1;
	}
	
	/**
	 * Internal state of the GameState, is updated for most information requests from any GameState object. This object is
	 *  shared among them and thus will not keep its state the same at most times.
	 * Please inform me (@pascal) if you intend to directly use this field for anything, as there is likely a way to solve your
	 *  problem in a safer way.
	 */
	private static Map<Integer, Board> dummy_boards = new HashMap<>();
	private static Map<Integer, GameState> dummy_states = new HashMap<>();

	public static Map<Thread, Integer> thread_dummy_ids = new HashMap<>();
	private static int last_dummy_id = 0;

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

	private synchronized Integer getDummyKey() {
		Integer key = thread_dummy_ids.get(Thread.currentThread());
		if (key == null) {
			thread_dummy_ids.put(Thread.currentThread(), ++last_dummy_id);
			return last_dummy_id;
		}
		return key;
	}

	public Board getDummyBoard() {
		return dummy_boards.get(getDummyKey());
	}

	public void setDummyBoard(Board dummy_board) {
		GameState.dummy_boards.put(getDummyKey(), dummy_board);
	}

	public GameState getDummyState() {
		return dummy_states.get(getDummyKey());
	}

	public void setDummyState(GameState dummy_state) {
		GameState.dummy_states.put(getDummyKey(), dummy_state);
	}

	/**
	 * Makes sure the data in the dummy board matches this GameState
	 */
	public synchronized void setDummyBoard() {
		if (getDummyBoard() != null && this.equals(getDummyState())) return;

		if (getDummyBoard() == null || getDummyState() == null || !getDummyState().root.contentEquals(root)) setDummyBoard(getOriginalBoard().clone());
		else for (GameState state = getDummyState(); state != null; state = state.parent) {
				if (state == this) { setDummyState(this); return; }
				if (state.last_move != null) getDummyBoard().reverseMove(state.last_move);
		}

		LinkedList<Move> moves = new LinkedList<>();

		for (GameState state = this; state != null; state = state.parent)
			if (state.last_move != null) moves.addFirst(state.last_move);

		for (Move move : moves) move.execute(getDummyBoard());

		setDummyState(this);
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
		return GameRules.SELECTED_GAMERULES.getAllPossibleMoves(getDummyBoard(), getDummyBoard().getEquivalent(pawn));
	};


	public double currentScore(Player player){
		setDummyBoard();
		return getDummyBoard().currentScore(player);
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
		return getDummyBoard().getAllnodes();
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
		return getDummyBoard().getAllNodesOf(player);
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
		return getDummyBoard().getAllPawns();
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
		return getDummyBoard().getAllPawnsOf(owner);
	};

	public Player getEnemy(Player player) {
		return getOriginalBoard().getEnemy(player);
	}

	public List<BoardNode> getGoal(Player player) {
		return getOriginalBoard().getGoal(player).stream().map(getDummyBoard().getNodeMapper()).collect(Collectors.toList());
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
		return move.isValid(getDummyBoard());
	}

	/**
	 * This method requires {@link GameState#dummy_board} to be modified.<br><br>
	 */
    public boolean hasWon(Player Player) {
    	setDummyBoard();
    	return GameRules.SELECTED_GAMERULES.hasWon(getDummyBoard(), Player);
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
		original_board = getDummyBoard().clone();
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
		double[] result = new double[162];
		int playerID = original_board.getPlayerID(perspective);
		int enemy = Board.player_pairings[playerID];
		byte[] rep = getIntegerRepresentation();
		double[] my_matrix = fastMatrix1d(playerID, rep);
		double[] enemy_matrix = fastMatrix1d(enemy, rep);
		switch (boardRep) {
		case TwoNoNegatives:
			for (int i=0; i < my_matrix.length; i++)
				result[i] = my_matrix[i];
			for (int i=0; i < enemy_matrix.length; i++)
				result[my_matrix.length+i] = enemy_matrix[i];
			break;
		default:
			for (int i=0; i < my_matrix.length; i++)
				result[i] = my_matrix[i] - enemy_matrix[i];
			for (int i=0; i < enemy_matrix.length; i++)
				result[my_matrix.length + i] = (my_matrix[i] == 1 || enemy_matrix[i] == 1)? 1 : 0;
		}
		return result;
		//return Utils.unrollMatrix(getMatrix(perspective, boardRep));
	}

	public double[][][] getMatrix(Player perspective) {
		return getMatrix(perspective, BoardRep.Original);
	}

	public double[][][] getMatrix(Player perspective, BoardRep board_rep) {
		return getMatrix3d(original_board.getPlayerID(perspective),getIntegerRepresentation(),board_rep);
	}

	public static Map<Integer, Map<Byte, Integer>> pid_to_nid_to_mid;
	static {
		pid_to_nid_to_mid = new HashMap<>();
		for (byte node = 0; node < 121; node++) {
			byte[] integer_rep = new byte[60];
			Arrays.fill(integer_rep, node);
			for (int pid = 0; pid < 6; pid++) {
				double[] res = Utils.unrollMatrix(getMatrix3d(pid, integer_rep, BoardRep.TwoNoNegatives)[0]);
				int x = -1;
				for (int i=0; i < res.length; i++)
					if (res[i] == 1) { x = i; break; }
				if (node == 0)
					pid_to_nid_to_mid.put(pid, new HashMap<Byte, Integer>());
				pid_to_nid_to_mid.get(pid).put(node, x);
			}
		}
	}

	public static double[] fastMatrix1d(int playerID, byte[] integer_rep) {
		double[] unrolled = new double[81];
		for (int i=playerID*10; i < playerID*10 + 10; i++) {
			int index = pid_to_nid_to_mid.get(playerID).get(integer_rep[i]);
			if (index >= 0) unrolled[index] = 1;
		}
		return unrolled;
	}

	public static double [][][] getMatrix3d(int playerID,byte[] integer_rep, BoardRep board_rep) {
		List<Byte> blackList = new ArrayList<>();
		List<BoardNode> nodes = Board.getEmpty().getAllnodes();
		List<BoardNode> allready = new ArrayList<>();
		Queue<BoardNode> queue = new LinkedList<>();
		for(int i = 0;i < 6; i++){
			if(i != playerID &&
					i != Board.player_pairings[playerID]){
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
		List<Byte> pawnPositions = new ArrayList<Byte>();
		for (byte pid = 0; pid < 6;pid++) {
			if (pid != playerID) {
				for (byte i = 0; i < 10; i++) {
					if (i + pid * 10 < integer_rep.length) {
						pawnPositions.add(integer_rep[(i + pid * 10)]);
					}
					else{
						break;
					}
				}
			}
		}
		Byte[] pawn_positions = new Byte[pawnPositions.size()];
		pawnPositions.toArray(pawn_positions);
		queue.add(nodes.get(corner));
		byte [][] matrix = new byte[9][9];
		constructMatrix(queue,new ArrayList<>(),blackList,playerID,matrix,integer_rep,pawn_positions);
		queue.clear();
		queue.add(nodes.get(opositecorner));
		byte [][] opositeMatrix = new byte[9][9];
		constructMatrix(queue,new ArrayList<>(),blackList,playerID,opositeMatrix,integer_rep,pawn_positions);
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
							if(matrix[i][j] == ((k==0)? 1:-1)) result[k][i][j] = 1;
							break;
						default:
							if(matrix[i][j] != 0) {
								if (k==0) {
									result[k][i][j] = matrix[i][j];
								} else result[k][i][j] = Math.abs(matrix[i][j]);
							}}}}}
		return result;
	}

	private static void constructMatrix(Queue<BoardNode> queue,List<Byte> allready,List<Byte> blackList,int playerID,byte [][] matrix, byte [] getIntegerRepresentation,Byte[] pawn_positions){
		for(int j = 0;j < 9;j++) {
			Queue<BoardNode> Requeue = new LinkedList<>();
			int x = j;
			int y = 0;
			while (!queue.isEmpty()) {
				BoardNode node = queue.poll();
				boolean conditions = true;
				for (Byte check : allready) {
					if (check == node.getID()) {
						conditions = false;
						break;
					}
				}
				if(conditions){
					for (Byte v : blackList) {
						if (v == node.getID()) {
							conditions = false;
							break;
						}
					}
				}
				if(conditions) {
					boolean conditions1 = false;
					boolean conditions2 = false;
					for (int i = 0; i < 10; i++) {
						if ((getIntegerRepresentation[i + playerID * 10]) == node.getID()) {
							matrix[x][y] = 1;
							conditions1 = true;
							break;
						}
					}
					if (!conditions1) {
						for(byte position : pawn_positions){
							if(position == node.getID()){
								matrix[x][y] = -1;
								conditions2 = true;
								break;
							}
						}
					}
					if (!conditions1 && !conditions2) {
						matrix[x][y] = 0;
					}
					x -=1 ;
					y +=1 ;
					allready.add((byte)node.getID());
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
