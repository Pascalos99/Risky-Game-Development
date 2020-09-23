package players;

import java.util.List;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;

public interface Player {
     
	Move returnMove(Board gameBoard);
     
    default List<BoardNode> getGoalNodes(Board gameBoard) {
    	 return gameBoard.getGoal(this);
    }
    
    default Player getEnemy(Board gameBoard) {
    	return gameBoard.getEnemy(this);
    }
     
}
