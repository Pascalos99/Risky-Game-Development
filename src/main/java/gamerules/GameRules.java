package gamerules;

import java.util.ArrayList;
import java.util.List;

import players.Player;

public abstract class GameRules {

	public static GameRules SELECTED_GAMERULES = new DefaultGameRules();
	
    public abstract boolean allowMove(Board board, Pawn pawn,BoardNode target);

    public abstract List<Move> getAllPossibleMoves(Board board, Pawn pawn);
    
    public abstract List<Move> getAllPossibleMoves(byte[] integer_rep, int pawn_pos_ID);

    public abstract boolean hasWon(Board board, Player Player);  
    
    public abstract String getName();
    
    public final List<Move> getAllPossibleMoves(Board board, List<Pawn> pawns) {
		ArrayList<Move> result = new ArrayList<>();
		for (Pawn pawn : pawns)
			result.addAll(getAllPossibleMoves(board, pawn));
		return result;
	}
}
