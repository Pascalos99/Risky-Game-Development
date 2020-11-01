package gamerules;

import java.math.BigInteger;
import java.util.Arrays;

public class GameState {
	
	public static final byte PLAYER_PAWNS = 10;
	
	public final Board original_board;
	
	private byte[] pawn_positions;
	/*
	 * Example of pawn positions:
	 * [0,1,2,5,8,10,29,31,45,67, 7,9,12,13,17,21,51,62,91,100]
	 * means: player 0 has pawns on nodes 0, 1, 2, 5, 8, 10, 29, 31, 45, 67
	 * and:   player 1 has pawns on nodes 7, 9, 12, 13, 17, 21, 51, 62, 91, 100
	 */
	private final int player_count;
	
	public GameState(Board copyFrom) {
		original_board = copyFrom;
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
	
	public GameState(GameState copyFrom, Move move) {
		original_board = copyFrom.original_board;
		player_count = copyFrom.player_count;
		pawn_positions = Arrays.copyOf(copyFrom.pawn_positions, copyFrom.pawn_positions.length);
		int player = original_board.getPlayerIndex(move.pawn.getOwner());
		int from = player * PLAYER_PAWNS;
		int to = from + PLAYER_PAWNS;
		int index = Arrays.binarySearch(pawn_positions, from, to, (byte)move.start.getID());
		System.out.println(Arrays.toString(Arrays.copyOfRange(pawn_positions, from, to)));
		pawn_positions[index] = (byte)move.target.getID();
		Arrays.sort(pawn_positions, from, to);
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

}
