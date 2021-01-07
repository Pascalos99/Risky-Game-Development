package gamerules.evaluation_functions;

import gamerules.EvaluationFunction;
import gamerules.GameState;
import players.Player;

/**
 * {@linkplain SimpleGoalDistance} normalized to be within [0,1]
 */
public class NormalizedSGD implements EvaluationFunction {
	
	private double min = -140;
	private double max =  110;
	
	@Override
	public Double apply(GameState t, Player u) {
		return (SimpleGoalDistance.SIMPLE_GOAL_DISTANCE.apply(t, u) - min) / (max - min);
	}

}
