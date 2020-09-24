package game;

import players.Player;

public class TurnEvent extends GameEvent {

	public final Player player;
	public final int player_ID;
	
	public TurnEvent(Player player, int player_ID) {
		super(Urgency.Note, "Notify player "+player+" with ID "+player_ID+" that it's their turn");
		this.player = player;
		this.player_ID = player_ID;
	}

}
