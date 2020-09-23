package gamerules;

import players.Player;

import java.util.List;

public interface GameRules {

    boolean allowMove(Board board,Pawn pawn,BoardNode target);

    List<Move> getAllPossibleMoves(Board board,Pawn pawn);

    boolean hasWon(Board board, Player Player);
}
