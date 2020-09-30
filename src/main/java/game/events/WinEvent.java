package game.events;

import players.Player;

public class WinEvent extends GameEvent {
	
	private volatile Player player;

	public WinEvent(Player player, int turncount) {
		super(Urgency.Warning, "#!#! Player "+player+" ["+player.getColorName()+"] has WON the game after "+turncount+" turns !#!#");
	}
	
	public synchronized Player getPlayer() {
		return player;
	}

}
