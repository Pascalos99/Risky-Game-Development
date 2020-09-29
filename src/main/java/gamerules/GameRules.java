package gamerules;

import java.util.List;

import players.Player;

public abstract class GameRules {

	public static GameRules SELECTED_GAMERULES = new DefaultGameRules();
	
    public abstract boolean allowMove(Pawn pawn,BoardNode target);

    public abstract List<Move> getAllPossibleMoves(Pawn pawn);

    public abstract boolean hasWon(Board board, Player Player);
}
