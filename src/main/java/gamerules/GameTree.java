package gamerules;

import java.util.List;

public abstract class GameTree {

    private final GameState root;
    private GameState current;

    public GameTree(GameState root) {
        this.root = root;
        current = root;
    }

    private void build() {
        for (Move move : current.getAllPossibleMoves(current.getAllPawnsOf(current.currentPlayer()))) {

        }
    }

}
