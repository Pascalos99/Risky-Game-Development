package players.bots;

/**
 * Player-types that implement this interface always return the same Move given the same board, meaning that if it returns an invalid move once, it
 * will keep returning the same invalid move.
 * 
 * Let your bot implement this interface if it is deterministic in this way, meaning that {@link gamerules.Board#forceRequestMoveAndContinue()} will
 * get stuck if executed on this bot like normal.
 */
public interface DeterministicReturn {

	/*
	 * 
	 */
	
}
