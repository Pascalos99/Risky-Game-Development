package main.java.players;

import java.util.List;

import main.java.gamerules.Board;
import main.java.gamerules.BoardNode;
import main.java.gamerules.Move;

public interface Player {
     
	Move returnMove(Board gameBoard);
     
    default List<BoardNode> getGoalNodes(Board gameBoard) {
    	 return gameBoard.getGoal(this);
    }
    
    default Player getEnemy(Board gameBoard) {
    	return gameBoard.getEnemy(this);
    }
     
}
