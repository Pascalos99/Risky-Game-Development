package gamerules;

import java.util.ArrayList;
import java.util.List;

import players.Player;

public abstract class GameRules {

	public static GameRules SELECTED_GAMERULES = new DefaultGameRules();
	
    public abstract boolean allowMove(Pawn pawn,BoardNode target);

    public abstract List<Move> getAllPossibleMoves(Pawn pawn);

    public abstract boolean hasWon(Board board, Player Player);    
    
    public final List<Move> getAllPossibleMoves(List<Pawn> pawns) {
		ArrayList<Move> result = new ArrayList<>();
		for (Pawn pawn : pawns)
			result.addAll(getAllPossibleMoves(pawn));
		return result;
	}
}
