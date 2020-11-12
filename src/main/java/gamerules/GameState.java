package gamerules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import players.Player;

public abstract class GameState {
	
	public static final byte PLAYER_PAWNS = 10;
	
	public final Board original_board;
	
	public static Board dummy_board;
	public static GameState dummy_state;
	
	/*         /a---b---*p*
	 *    /r--<
	 * G-<     \c---d---%q%
	 *    \---...
	 */ 
	
	private byte[] pawn_positions;
	/*
	 * Example of pawn positions:
	 * [0,1,2,5,8,10,29,31,45,67, 7,9,12,13,17,21,51,62,91,100]
	 * means: player 0 has pawns on nodes 0, 1, 2, 5, 8, 10, 29, 31, 45, 67
	 *   and: player 1 has pawns on nodes 7, 9, 12, 13, 17, 21, 51, 62, 91, 100
	 */
	
	private Move last_move;
	private GameState parent;
	private int depth;
	
	private final int player_count;
	
	/**
	 * Makes sure the data in the dummy board matches this GameState
	 */
	public void setDummyBoard() {
		if (dummy_board != null && this.equals(dummy_state)) return;
		
		if (dummy_board == null) dummy_board = original_board.clone();
		else
			for (GameState state = dummy_state; state != null; state = state.parent)
				if (state.last_move != null) dummy_board.reverseMove(state.last_move);
		
		LinkedList<Move> moves = new LinkedList<>();
		
		for (GameState state = this; state != null; state = state.parent)
			moves.addFirst(state.last_move);
		
		for (Move move : moves) move.execute(dummy_board);
		
		dummy_state = this;
	}
	
	public List<Move> getAllMoves(Pawn pawn) {
		setDummyBoard();
		return GameRules.SELECTED_GAMERULES.getAllPossibleMoves(dummy_board.getEquivalent(pawn));
	};

	public Player currentPlayer() {
		return original_board.getPlayers().get(
				(depth + original_board.currentPlayerID()) % original_board.getPlayerCount());
	}
	
	public List<BoardNode> getAllnodes() {
		setDummyBoard();
		return dummy_board.getAllnodes();
	};
	// generate new BoardNode objects if different from original gamestate
	
	public List<BoardNode> getAllNodesOf(Player player) {
		setDummyBoard();
		return dummy_board.getAllNodesOf(player);
	};
	// generate new BoardNode objects if different from original gamestate

	public List<Pawn> getAllPawns() {
		setDummyBoard();
		return dummy_board.getAllPawns();
	};
	// generate new Pawn objects if different from original gamestate

	public List<Pawn> getAllPawnsOf(Player owner) {
		setDummyBoard();
		return dummy_board.getAllPawnsOf(owner);
	};
	// generate new Pawn objects if different from original gamestate
	
	public Player getEnemy(Player player) {
		return original_board.getEnemy(player);
	}

	public List<BoardNode> getGoal(Player player) {
		return original_board.getGoal(player);
		// might be problem if BoardNodes change
	}
	
	public final boolean isGoalNode(Player player, BoardNode node) {
    	return node.getOwner() == getEnemy(player);
    }
	
	public abstract boolean allowMove(Pawn pawn,BoardNode target);

    public abstract List<Move> getAllPossibleMoves(Pawn pawn);

    public abstract boolean hasWon(Player Player);
    
    public abstract boolean hasWinner();
    
    public final List<Move> getAllPossibleMoves(List<Pawn> pawns) {
		ArrayList<Move> result = new ArrayList<>();
		for (Pawn pawn : pawns)
			result.addAll(getAllPossibleMoves(pawn));
		return result;
	}
	
	public GameState(Board copyFrom) {
		original_board = copyFrom;
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
		original_board = parent.original_board;
		this.parent = parent;
		last_move = move;
		depth = parent.depth + 1;
		player_count = parent.player_count;
		pawn_positions = Arrays.copyOf(parent.pawn_positions, parent.pawn_positions.length);
		int player = original_board.getPlayerIndex(move.pawn.getOwner());
		int from = player * PLAYER_PAWNS;
		int to = from + PLAYER_PAWNS;
		int index = Arrays.binarySearch(pawn_positions, from, to, (byte)move.start.getID());
		System.out.println(Arrays.toString(Arrays.copyOfRange(pawn_positions, from, to)));
		pawn_positions[index] = (byte)move.target.getID();
		Arrays.sort(pawn_positions, from, to);
	}
	
	public GameState getStateAfterMove(Move move) {
		return new GameState(this, move);
	}
	
	public GameState getPrevious() {
		return parent;
	}
	
	public BigInteger gameStateID() {
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
		return new BigInteger(input);
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
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof GameState)) return false;
		GameState s = (GameState) o;
		if (s.parent.equals(parent) && s.last_move.equals(last_move)
				&& s.depth == depth && s.gameStateID().equals(gameStateID())) return true;
		return false;
	}

}
