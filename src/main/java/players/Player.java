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
    
    public static final Player NONE = new Player() {

		@Override
		public Move returnMove(Board gameBoard) {
			throw new RuntimeException("Trying to get move from absent player, please check if player == Player.NONE");
		}
		public String toString() {
			return "None";
		}
	};
     
}
