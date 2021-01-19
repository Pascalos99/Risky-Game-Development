package players.bots.utils;

import players.bots.utils.NeuralNetwork.LossFunction;

public class GradientDescentSettings {
	
	private boolean dynamic_lr = false;
	private Double growth_rate;
	private Integer acceleration_interval;
	
	private Double minimum_lr;
	private Double maximum_lr;
	
	private boolean exploration = false;
	private Double exploration_magnitude;
	private Integer exploration_interval;
	private Double exploration_threshold;
	
	private Long seed;
	
	private Integer max_iterations;
	private Double learning_rate;
	private LossFunction loss;
	
	public GradientDescentSettings(LossFunction loss, double learning_rate, int max_iterations) {
		setLossLrMax(loss, learning_rate, max_iterations);
	}
	public GradientDescentSettings(LossFunction loss, double learning_rate) {
		setLossLrMax(loss, learning_rate, null);
	}
	public GradientDescentSettings(double learning_rate) {
		setLossLrMax(null, learning_rate, null);
	}
	public GradientDescentSettings() {}
	
	public GradientDescent get(Tunable model) {
		return set(new GradientDescent(model));
	}
	
	public GradientDescent set(GradientDescent GD) {
		GD.setDynamicLR(dynamic_lr, growth_rate, acceleration_interval);
		GD.setMinMaxLR(minimum_lr, maximum_lr);
		GD.setExploration(exploration, exploration_magnitude, exploration_interval, exploration_threshold, seed);
		GD.setLossLrMax(loss, learning_rate, max_iterations);
		return GD;
	}
	
	public GradientDescentSettings setDynamicLR(boolean set, Double growth_rate, Integer acceleration_interval) {
		dynamic_lr = set;
		this.growth_rate = growth_rate;
		this.acceleration_interval = acceleration_interval;
		return this;
	}
	
	public GradientDescentSettings setMinMaxLR(Double min_lr, Double max_lr) {
		minimum_lr = min_lr;
		maximum_lr = max_lr;
		return this;
	}
	
	public GradientDescentSettings setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval, Double exploration_threshold, Long seed) {
		exploration = set;
		this.exploration_interval = exploration_interval;
		this.exploration_magnitude = exploration_magnitude;
		this.exploration_threshold = exploration_threshold;
		this.seed = seed;
		return this;
	}
	public GradientDescentSettings setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval, Double exploration_threshold) {
		return setExploration(set, exploration_magnitude, exploration_interval, exploration_threshold, seed);
	}
	public GradientDescentSettings setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval) {
		return setExploration(set, exploration_magnitude, exploration_interval, exploration_threshold, seed);
	}
	
	public GradientDescentSettings setLossLrMax(LossFunction loss_function, Double learning_rate, Integer max_iterations) {
		this.loss = loss_function;
		this.learning_rate = learning_rate;
		this.max_iterations = max_iterations;
		return this;
	}
	
	public GradientDescentSettings setMaxIter(Integer max_iterations) {
		this.max_iterations = max_iterations;
		return this;
	}
	public GradientDescentSettings setLR(Double learning_rate) {
		this.learning_rate = learning_rate;
		return this;
	}
	public GradientDescentSettings setLoss(LossFunction loss) {
		this.loss = loss;
		// kid: mom, can we have loss?
		// mom: no, we have loss at home
		// loss at home: 
		//   I  | II
		//  ---------
		//   iI | I_
		return this;
	}
	
}
