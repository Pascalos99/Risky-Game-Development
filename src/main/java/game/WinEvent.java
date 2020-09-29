package game;

import players.Player;

public class WinEvent extends GameEvent {
	
	private volatile Player player;

	public WinEvent(Player player) {
		super(Urgency.Warning, "#!#! Player "+player+" has WON the game !#!#");
	}
	
	public synchronized Player getPlayer() {
		return player;
	}

}
