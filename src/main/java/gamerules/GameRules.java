package gamerules;

import java.util.List;

import players.Player;

public interface GameRules {

    boolean allowMove(Pawn pawn,BoardNode target);

    List<Move> getAllPossibleMoves(Pawn pawn);

    boolean hasWon(Board board, Player Player);
}
