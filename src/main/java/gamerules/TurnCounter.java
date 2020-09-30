package gamerules;

import players.Player;

public class TurnCounter {

	protected Player player;
	protected volatile int count;
	
	public TurnCounter(Player player) {
		this.player = player;
		count = 0;
	}
	
	public synchronized int getCount() {
		return count;
	}
	
}
