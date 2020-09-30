package players.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamerules.Board;
import gamerules.GameRules;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

public class EvilPlayer extends Player implements DeterministicReturn {

	protected static class WeightedMove implements Comparable<WeightedMove> {
		public final Move move;
		public double weight;
		public WeightedMove(Move move, double weight) {
			this.move = move;
			this.weight = weight;
		}
		public WeightedMove(Move move) {
			this(move, 0);
		}
		@Override
		public int compareTo(WeightedMove o) {
			if (o.weight < weight) return -1;
			if (o.weight == weight) return 0;
			return 1;
		}
		public String toString() {
			return move+" with weight "+weight;
		}
	}
	
	public static boolean debug = false;
	
	private Map<Player, Double> max_move_scores;
	private Map<Move, Double> backup_weights;
	private Map<Pawn, Double> distances;
	private List<Pawn> not_goal_pawns;
	
	private Double longest_distance;
	
	@Override
	public Move returnMove(Board board) {
		max_move_scores = new HashMap<>();
		backup_weights = new HashMap<>();
		distances = null;
		not_goal_pawns = null;
		longest_distance = Double.NEGATIVE_INFINITY;
		
		GameRules rules = GameRules.SELECTED_GAMERULES;
		List<WeightedMove> possible_moves = new ArrayList<>();
		List<Pawn> pawns = board.getAllPawnsOf(this);
		
		for (Pawn pawn : pawns) {
			
			boolean goal_pawn = false;
			if (pawn.getPosition().isGoal(this, board)) {
				goal_pawn = true;
				if (distances == null) {
					distances = new HashMap<>();
					for (Pawn ppawn : pawns) {
						double distance = board.distanceToEnemy(ppawn);
						distances.put(ppawn, distance);
						if (distance > longest_distance) longest_distance = distance;
					}}
				if (not_goal_pawns == null) {
					not_goal_pawns = new ArrayList<>();
					for (Pawn ppawn : pawns)
						if (!ppawn.getPosition().isGoal(this, board)) not_goal_pawns.add(ppawn);
				}
			}
			
			List<Move> moves = rules.getAllPossibleMoves(pawn);
			for (Move move : moves) {
				WeightedMove weight = null;
				if (!goal_pawn) weight = normalWeights(move, board, rules);
				else {
					backup_weights.put(move, Double.valueOf(0));
					if (longest_distance <= 4 || not_goal_pawns.size() < 4) {
						List<Move> moves_after = board.getPossibleMovesAfterMove(not_goal_pawns, move);
						for (Move m : moves_after) {
							if (m.start == move.target || m.pawn == move.pawn)
							if (m.target.getOwner() == getEnemy(board)) {
								weight = new WeightedMove(move, 1000000 + board.calculateScoreAfterMove(this, move, m));
								break;
							}
						}
					}
					if (weight == null) weight = new WeightedMove(move, 0);
				}
			
				possible_moves.add(weight);
				if (weight.weight == Double.POSITIVE_INFINITY) {
					if (debug) System.out.println("Found goal reaching move "+weight.move);
					return weight.move;
				}
			}
		}
		Collections.shuffle(possible_moves);
		Collections.sort(possible_moves);
		if (possible_moves.size() <= 0) {
			if (debug) System.out.println("Couldn't find move");
			return null;
		}
		WeightedMove best = possible_moves.get(0);
		if (best.weight == 0) {
			for (WeightedMove m : possible_moves) m.weight = backup_weights.get(m.move);
			Collections.sort(possible_moves);
			best = possible_moves.get(0);
			if (debug) System.out.println("Found neutral move with weight "+best.weight);
		} else {
			if (debug) System.out.println("Found evil move with weight "+best.weight);
		}
		return best.move;
	}
	
	private WeightedMove normalWeights(Move move, Board board, GameRules rules) {
		WeightedMove weight = new WeightedMove(move);
		backup_weights.put(move, move.calculateScore(board));
		if (move.target.isGoal(this, board)) weight.weight = Double.POSITIVE_INFINITY;
		else {
			for (Player player : board.getPlayers()) {
				if (player == this) continue;
				if (!max_move_scores.containsKey(player)) {
					List<Move> player_moves_before = rules.getAllPossibleMoves(board.getAllPawnsOf(player));
					double max_score = Double.NEGATIVE_INFINITY;
					for (Move player_move : player_moves_before) {
						double score = player_move.calculateScore(board);
						if (score > max_score) max_score = score;
					}
					max_move_scores.put(player, max_score);
				}
				List<Move> player_moves_after = board.getPossibleMovesAfterMove(board.getAllPawnsOf(player), move);
				double max_score = Double.NEGATIVE_INFINITY;
				for (Move player_move : player_moves_after) {
					double score = player_move.calculateScore(board);
					if (score > max_score) max_score = score;
				}
				weight.weight = max_move_scores.get(player) - max_score;
			}
		}
		return weight;
	}

	@Override
	public String getTypeName() {
		return "EvilBot";
	}

	@Override
	public String getDescription() {
		return "In each move tries to maximize annoyance for other players";
	}
	
	public String toString() {
		return "EvilBot";
	}

}
