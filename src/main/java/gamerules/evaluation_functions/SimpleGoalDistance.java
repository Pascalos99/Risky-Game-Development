package gamerules.evaluation_functions;

import java.util.Arrays;

import gamerules.Board;
import gamerules.EvaluationFunction;
import gamerules.GameState;
import players.Player;
import players.bots.utils.NodeDistanceCalc;

public class SimpleGoalDistance implements EvaluationFunction {

	private double distance_weight;
	private double goal_weight;
	
	public SimpleGoalDistance() {
		this(1, 13);
	}
	public SimpleGoalDistance(double distance_weight, double goal_weight) {
		this.distance_weight = distance_weight;
		this.goal_weight = goal_weight;
	}
	
	@Override
	public Double apply(GameState state, Player player) {
		int playerID = state.original_board.getPlayerIndex(player);
		byte[] state_rep = state.getIntegerRepresentation();
		byte[] goal_nodes = Board.nodes_owned_per_player[Board.player_pairings[playerID]];
		boolean[] is_pawn_at_goal = new boolean[10];
		boolean[] occupied_goals = new boolean[10];
		boolean any_empty = false;
		boolean has_won = true;
		double bonus = 0;
		
		for (int i=0; i < goal_nodes.length; i++) {
			boolean found = false;
			for (int p=0; p < state.original_board.getPlayerCount() && !found; p++) {
				int res = Arrays.binarySearch(state_rep, p*10, (p+1)*10, goal_nodes[i]);
				found = res >= 0;
				if (p == playerID && found) {
					is_pawn_at_goal[res - p*10] = true;
					bonus += goal_weight - distance_weight *
							NodeDistanceCalc.getDistance(goal_nodes[i], Board.furthest_goal_nodes_per_player[playerID]);
				}
				else has_won = false;
			}
			if (!found) any_empty = true;
			occupied_goals[i] = found;
		}
		
		if (has_won) return 0D;
		
		int sum = 0;
		int distance;
		for (int i=0; i < 10; i++) {
			if (is_pawn_at_goal[i]) continue;
			int node = state_rep[i + playerID * 10];
			if (!any_empty) {
				distance = NodeDistanceCalc.getDistance(Board.furthest_goal_nodes_per_player[playerID], node);
			} else {
				int max = 0;
				for (int j=0; j < 10; j++) {
					distance = NodeDistanceCalc.getDistance(goal_nodes[j], state_rep[i + playerID * 10]);
					if (distance > max) max = distance;
				}
				distance = max;
			}
			sum += distance;
		}
		return Double.valueOf(bonus - distance_weight * sum);
	}
	
}
