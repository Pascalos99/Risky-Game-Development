package players;

import java.util.List;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;

public abstract class Player {
	
	private String player_name = "";
	
	public abstract Move returnMove(Board gameBoard);
	
	/**
	 * @return The name of this Player-type (not of the individual player)
	 */
	public abstract String getTypeName();
	
	public abstract String getDescription();
	
	public final String getName() {
		return player_name;
	}
	
	public final void setName(String name) {
		player_name = name;
	}
     
    public final List<BoardNode> getGoalNodes(Board gameBoard) {
    	 return gameBoard.getGoal(this);
    }
    
    public final Player getEnemy(Board gameBoard) {
    	return gameBoard.getEnemy(this);
    }
    
    public String toString() {
    	return player_name + " - "+getName();
    }
    
    public static final Player NONE = new Player() {
    	@Override
		public String getTypeName() {
			return "None";
		}
		@Override
		public String getDescription() {
			return "A placeholder to allow for different color tiles in homes of absent players";
		}
		@Override
		public Move returnMove(Board gameBoard) {
			throw new RuntimeException("Trying to get move from absent player, please check if player == Player.NONE");
		}
	};
     
}
