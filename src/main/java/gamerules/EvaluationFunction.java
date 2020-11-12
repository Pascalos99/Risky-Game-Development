package gamerules;

import java.util.function.BiFunction;
import gamerules.GameState;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.Player;

/**
 * It is best for the integrity of the rest of the program, if the implementor of this interface tries to avoid using methods
 *  from GameState that requires {@linkplain GameState#dummy_board} to be altered. (for example, use the 
 *  {@linkplain GameState#getIntegerRepresentation()} instead)
 * @author anema
 *
 */
@FunctionalInterface
public interface EvaluationFunction extends BiFunction<GameState, Player, Double> {
	
	public static final EvaluationFunction SIMPLE_GOAL_DISTANCE = new SimpleGoalDistance();
	
	/**
	 * Evaluates the GameState for the given player<br><br>
	 * Depending on the implementation, the return value may or may not differ from {@link #eval(GameState)} called in a previous turn where 
	 * it was still the given player's turn.
	 * @param state the GameState to be evaluated
	 * @param player the player for which the score needs to be evaluated
	 * @return
	 */
	default double eval(GameState state, Player player) {
		return apply(state, player).doubleValue();
	}
	
	/**
	 * Evaluates the GameState for the current Player in that GameState
	 * @param state the GameState to be evaluated
	 * @return apply(state, state.currentPlayer()).doubleValue();
	 */
	default double eval(GameState state) {
		return apply(state, state.currentPlayer()).doubleValue();
	}
	
}
