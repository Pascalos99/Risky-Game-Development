package players;

import gamerules.Board;
import gamerules.Move;
import players.bots.DeterministicReturn;

import static gamerules.GameRules.SELECTED_GAMERULES;

public class HumanPlayer extends Player 
	implements DeterministicReturn { // as of right now at least <-- remove this line when HumanPlayer is properly implemented
	
	private static int human_count = 0;
	private int ID;
	
	public HumanPlayer() {
		ID = ++human_count;
		setName("Human-"+ID);
	}
	
	@Override
    public Move returnMove(Board gameBoard) {
        return null;
    }
	
	@Override
	public String getTypeName() {
		return "Human";
	}

	@Override
	public String getDescription() {
		return "Just a normal human being";
	}
    
    public String toString() {
    	if (getName().matches("Human-.*")) return "Human-"+ ID;
    	else return super.toString();
    }
}