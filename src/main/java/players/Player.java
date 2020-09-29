package players;

import java.awt.Color;
import java.util.List;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.Move;

public abstract class Player {
	
	private String player_name = "";
	private Color color = Color.GRAY;
	
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
	public final Color getColor() {
		return color;
	}
	public final void setColor(Color color) {
		this.color = color;
	}
	public String getColorName() {
		if (color.equals(Color.green)) 	 return "Green";
		if (color.equals(Color.blue)) 	 return "Blue";
		if (color.equals(Color.yellow))  return "Yellow";
		if (color.equals(Color.magenta)) return "Magenta";
		if (color.equals(Color.orange))  return "Orange";
		if (color.equals(Color.red)) 	 return "Red";
		return "?";
	}
     
    public final List<BoardNode> getGoalNodes(Board gameBoard) {
    	 return gameBoard.getGoal(this);
    }
    
    public final Player getEnemy(Board gameBoard) {
    	return gameBoard.getEnemy(this);
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
		public String toString() {
			return "None";
		}
	};
     
}
