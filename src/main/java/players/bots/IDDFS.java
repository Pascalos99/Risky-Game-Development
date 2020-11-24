package players.bots;

import gamerules.*;
import players.Player;

import java.util.List;

public class IDDFS extends Player {

    private List<Move> depthFirstSearch(GameTreeNode treeNode) {
        List<GameTreeNode> children = treeNode.getChildren();

        for (GameTreeNode child : children) {
            child.getGameState().setDummyBoard();
            GameState childState = child.getGameState();

            if (childState.hasWon(childState.currentPlayer()) && childState.currentPlayer() == this) {
                return childState.getMoveSequence();
            } else {
                List<Move> moves = depthFirstSearch(child);
                if (moves != null) return moves;
            }
        }

        return null;
    }

    @Override
    public Move returnMove(Board gameBoard) {
        GameState root = new GameState(gameBoard);
        GameTree tree = new GameTree(root);
        Move moveToSolution = null;

        boolean solutionFound = false;
        while(!solutionFound) {
            List<Move> moveList = depthFirstSearch(tree.getRoot());

            if (moveList == null) {
                tree.expand(tree.maxDepth());
            } else {
                moveToSolution = moveList.get(0);
            }
            solutionFound = true;
        }
        return moveToSolution;
    }

    @Override
    public String getTypeName() {
        return "Iterative Deepening Depth-First Search";
    }

    @Override
    public String getDescription() {
        return "Iterative Deepening Depth-First Search returns the move leading to the optimal win state for the " +
                "current player in the current game state";
    }

    @Override
    public Player getNewInstance() {
        return new IDDFS();
    }
}
