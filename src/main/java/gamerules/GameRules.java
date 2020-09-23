package gamerules;

import java.util.List;

import players.Player;

public interface GameRules {

    boolean allowMove(Board board,Pawn pawn,BoardNode target);

    List<Move> getAllPossibleMoves(Board board,Pawn pawn);

    boolean hasWon(Board board, Player Player);
}
