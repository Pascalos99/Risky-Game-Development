package game;

import players.Player;

public class TurnEvent extends GameEvent {

	private final Player player;
	private final boolean beginning_of_turn;
	
	public TurnEvent(Player player, boolean beginning_of_turn) {
		super(Urgency.Note, "Notify player "+player+" that "+(beginning_of_turn? "it's their turn" : "their turn has ended"));
		synchronized(this) {
			this.player = player;
			this.beginning_of_turn = beginning_of_turn;
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	public boolean isEndOfTurn() {
		return !beginning_of_turn;
	}
	public boolean isBeginning_of_turn() {
		return beginning_of_turn;
	}

}
