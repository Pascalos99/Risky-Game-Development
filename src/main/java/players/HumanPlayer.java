package players;

import gamerules.Board;
import gamerules.Move;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class HumanPlayer implements Player{
	
	private static int human_count = 0;
	
	private int ID;
	
	public HumanPlayer() {
		ID = ++human_count;
	}
	
    @Override
    public Move returnMove(Board gameBoard) {
        return null;
    }
    
    public String toString() {
    	return "Human-"+ ID;
    }
}