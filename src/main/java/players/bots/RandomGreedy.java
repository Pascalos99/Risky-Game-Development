package players.bots;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gamerules.Board;
import gamerules.EvaluationFunction;
import gamerules.GameState;
import gamerules.Move;
import gamerules.Pawn;
import players.Player;

public class RandomGreedy extends Player {

	private EvaluationFunction heuristic;
	
	public RandomGreedy(EvaluationFunction heuristic, double optimal_play) {
		this.heuristic = heuristic;
		this.optimal_play_factor = optimal_play;
	}
	public RandomGreedy(EvaluationFunction heuristic) {
		this(heuristic, 5);
	}
	
	public void setSeed(long seed) {
		random = new Random(seed);
	}
	
	/**
	 * @param x higher value means optimal play will be played more often (normal value is around 5)
	 */
	public void setOptimalPlayFactor(double x) {
		optimal_play_factor = x;
	}
	
	private double optimal_play_factor;
	private Random random;
	
	@Override
	public Move returnMove(Board game) {
		if (random == null) random = new Random();
		GameState state = new GameState(game);
		List<Pawn> pawns = state.getAllPawnsOf(this);
		Stream<ValueState> next = state.getAllPossibleMoves(pawns).stream().map(move -> new GameState(state, move))
				.map(s -> of(s, heuristic.eval(s, this) + 0.01 * Math.random()));
		Comparator<ValueState> comp = (s1, s2) -> {
			if (s1.value == s2.value) return 0;
			if (s1.value > s2.value) return -1;
			return 1;
		};
		List<Move> eval_order = next.sorted(comp).map(s -> s.state.lastMove()).collect(Collectors.toList());
		int index = eval_order.size();
		while (index >= eval_order.size())
			index = (int) Math.abs(random.nextGaussian() * eval_order.size() / optimal_play_factor);
		return eval_order.get(index);
	}

	@Override
	public String getTypeName() {
		return "Random Greedy ["+heuristic+"]";
	}

	@Override
	public String getDescription() {
		return "adds a slight randomness to the greedy Naive Bot";
	}

	@Override
	public Player getNewInstance() {
		return new RandomGreedy(heuristic, optimal_play_factor);
	}
	
	private static ValueState of(GameState state, double value) {
		return new ValueState(state, value);
	}
	
	static class ValueState {
		public GameState state;
		public double value;
		public ValueState(GameState state, double value) {
			this.state = state;
			this.value = value;
		}
	}

}
