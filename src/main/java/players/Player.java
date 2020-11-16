package players;

import java.awt.Color;
import java.util.List;

import gamerules.Board;
import gamerules.BoardNode;
import gamerules.GameState;
import gamerules.Move;
import gamerules.TurnCounter;

public abstract class Player {
	
	public final TurnCounter turnCounter;
	
	public Player() {
		turnCounter = new TurnCounter(this);
	}
	
	private String player_name = "";
	private Color color = Color.GRAY;
	
	public abstract Move returnMove(Board gameBoard);
	
	/**
	 * @return The name of this Player-type (not of the individual player)
	 */
	public abstract String getTypeName();
	
	public abstract String getDescription();
	
	/**
	 * The first instance of a Player will be the one in the {@link game.PlayerSetup#player_types} list;
	 * every instance after that (the players in the game) will be gathered from this method.
	 * Any specific settings of your bot may be determined in the constructor and they should be copied over here
	 * (since this is an instance method, that's possible)
	 * @return
	 */
	public abstract Player getNewInstance();
	
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
		if (color.equals(Color.green)) 	  return "Green";
		if (color.equals(Color.blue)) 	  return "Blue";
		if (color.equals(Color.yellow))   return "Yellow";
		if (color.equals(Color.magenta))  return "Magenta";
		if (color.equals(Color.orange))   return "Orange";
		if (color.equals(Color.red)) 	  return "Red";
		if (color.equals(Color.white)) 	  return "White";
		if (color.equals(Color.black)) 	  return "Black";
		if (color.equals(Color.cyan)) 	  return "Cyan";
		if (color.equals(Color.pink)) 	  return "Pink";
		if (color.equals(Color.gray))  	  return "Gray";
		if (color.equals(Color.darkGray)) return "Dark Gray";
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}
     
    public final List<BoardNode> getGoalNodes(Board gameBoard) {
    	 return gameBoard.getGoal(this);
    }
    
    public final boolean isGoalNode(Board gameBoard, BoardNode node) {
    	return gameBoard.isGoalNode(this, node);
    }
    
    public final Player getEnemy(Board gameBoard) {
    	return gameBoard.getEnemy(this);
    }
    
    public double currentScore(Board board) {
    	return board.currentScore(this);
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
		@Override
		public Player getNewInstance() {
			throw new RuntimeException("NONE player may not be duplicated");
		}
	};
     
}
