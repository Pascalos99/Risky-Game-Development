package gamerules;

import java.util.List;

import players.Player;

public class DefaultGameRules implements GameRules {

	@Override
	public boolean allowMove(Board board, Pawn pawn, BoardNode target) {
		// TODO implement method
		return false;
	}

	@Override
	public List<Move> getAllPossibleMoves(Board board, Pawn pawn) {
		// TODO implement method
		return null;
	}

	@Override
	public boolean hasWon(Board board, Player Player) {
		// TODO implement method
		return false;
	}

}
