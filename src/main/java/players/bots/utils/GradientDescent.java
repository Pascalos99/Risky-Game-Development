package players.bots.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import players.bots.utils.NeuralNetwork.LossFunction;

public class GradientDescent {
	
	private Tunable model;
	private double learning_rate;
	private LossFunction loss;
	private int max_iterations;
	
	private List<Double> loss_values;
	private int last_loss_index;
	private boolean is_busy = false;
	private boolean stop = false;
	private int iteration_count;
	
	public static int min_data_interval = 250;
	
	private DescentThread thread;
	
	private boolean dynamic_lr = false;
	private double growth_rate = 1.25;
	private int acceleration_interval = 50;
	private double minimum_lr = 0.0001;
	private double maximum_lr = 1;
	
	private boolean exploration = false;
	private double exploration_magnitude = 0.5;
	private int exploration_interval = 25;
	private double min_explorer_loss_advantage = 0;
	private Random explorator = new Random();
	private Tunable exploration_model;
	private List<double[][]> stored_data_for_exploration = new ArrayList<>();
	
	/**
	 * @param model the model to be optimized 
	 * @param loss loss function being traversed
	 * @param learning_rate learning rate of the descent
	 * @param max_iterations maximum number of iterations before algorithm stops (set to -1 to raise restriction)
	 */
	public GradientDescent(Tunable model, LossFunction loss, double learning_rate, int max_iterations) {
		this.model = model;
		this.learning_rate = learning_rate;
		this.loss = loss;
		this.max_iterations = max_iterations;
		loss_values = new ArrayList<>();
		last_loss_index = 0;
	}
	public GradientDescent(Tunable ann, LossFunction loss, double learning_rate) {
		this(ann, loss, learning_rate, -1);
	}
	
	/**
	 * @param set
	 * @param growth_rate may be {@code null}: sets to default
	 * @param acceleration_interval may be {@code null}: sets to default
	 */
	public void setDynamicLR(boolean set, Double growth_rate, Integer acceleration_interval) {
		dynamic_lr = set;
		this.growth_rate = (growth_rate != null)? growth_rate : this.growth_rate;
		this.acceleration_interval = (acceleration_interval != null)? acceleration_interval : this.acceleration_interval;
	}
	
	public void setMinMaxLR(Double min_lr, Double max_lr) {
		minimum_lr = (min_lr != null)? min_lr : minimum_lr;
		maximum_lr = (max_lr != null)? max_lr : maximum_lr;
	}
	
	/**
	 * @param set
	 * @param exploration_interval may be {@code null}: sets to default
	 */
	public void setExploration(boolean set, Double exploration_magnitude, Integer exploration_interval, Long seed) {
		exploration = set;
		this.exploration_interval = (exploration_interval != null)? exploration_interval : this.exploration_interval;
		this.exploration_magnitude = (exploration_magnitude != null)? exploration_magnitude : this.exploration_magnitude;
		this.explorator = (seed != null)? new Random(seed) : this.explorator;
	}
	
	/**
	 * Starts the gradient descent if it hasn't started yet
	 * @param data a supplier of data points, which are as follows:<br>
		 *   data.get() = { input_array, target_array }<br>Data points are allowed to be repeated
		 *   and can be supplied randomly, this is up to the implementer.
	 * @return {@code true} if this call caused the algorithm to start, {@code false} otherwise
	 */
	public boolean start(Supplier<double[][]> data) {
		if (is_busy || data == null) return false;
		thread = new DescentThread(data);
		thread.start();
		return true;
	}
	
	private void trainingStep(double[] input, double[] target) {
		double[] output = model.computeOutput(input);
		double loss_value = loss.calculate(output, target);
		loss_values.add(loss_value);
		double[][][] gradients = model.calculateLossGradients(loss, target);
		double[][][] weights = model.getAllWeights();
		
		if (exploration) {
			stored_data_for_exploration.add(new double[][] {input, target});
			if (iteration_count % exploration_interval == 0) {
				setupExplorationModel();
				int size = stored_data_for_exploration.size();
				double sum_diff = 0;
				for (int i=0; i < size; i++) {
					double[][] datapoint = stored_data_for_exploration.get(i);
					double explorer_loss = loss.calculate(exploration_model.computeOutput(datapoint[0]), datapoint[1]);
					double normal_loss = loss_values.get(loss_values.size() + i - size);
					sum_diff += normal_loss - explorer_loss; // if normal_loss < explorer_loss on average, the sum will be < 0
				}
				if (sum_diff > min_explorer_loss_advantage) {
					// replace network by exploration network
					//System.out.format("summed loss advantage of exploration is %.3e; thus replacing current model with exploration\n", sum_diff);
					model.setAllWeights(exploration_model.getAllWeights());
					exploration_replacements++;
				}
			}
		}
		
		double[][][] clone_weights = null; // clone the weights when dynamic LR step is activated: (just in case we revert)
		if (dynamic_lr && (iteration_count % acceleration_interval == 0)) {
			clone_weights = new double[weights.length][][];
			for (int i=0; i < weights.length; i++) {
				clone_weights[i] = new double[weights[i].length][weights[i][0].length];
				for (int j=0; j < weights[i].length; j++)
					for (int k=0; k < weights[i][j].length; k++)
						clone_weights[i][j][k] = weights[i][j][k]; }}
		
		adjustWeights(weights, gradients, learning_rate);
		
		// dynamic learning_rate test:
		if (dynamic_lr && (iteration_count % acceleration_interval == 0)) {
			double expected = estimateNextLoss(gradients, loss_value);
			double actual_loss = loss.calculate(model.computeOutput(input), target);
			if (learning_rate > minimum_lr * growth_rate*growth_rate && actual_loss > loss_value) {
				model.setAllWeights(clone_weights); // go back to previous weights
				learning_rate /= growth_rate*growth_rate;
				//System.out.format("loss changed from %.3e to %.3e; learning rate thus lowered to %.3f\n", loss_value, actual_loss, learning_rate);
			} else if (learning_rate < maximum_lr / growth_rate && expected > actual_loss) {
				learning_rate *= growth_rate;
				//System.out.format("expected loss was %.3e, which is greater than the actual loss: %.3e; learning rate thus increased to %.3f\n",
				//		expected, actual_loss, learning_rate);
			} else {
				//System.out.print(".");
			}
		}
	}
	
	private int exploration_replacements = 0;
	
	private void setupExplorationModel() {
		exploration_model = model.clone();
		double[][][] weights = exploration_model.getAllWeights();
		for (int i=0; i < weights.length; i++)
			for (int j=0; j < weights[i].length; j++)
				for (int k=0; k < weights[i][j].length; k++)
					weights[i][j][k] += (2 * explorator.nextDouble() - 1) * exploration_magnitude;
	}
	
	private double estimateNextLoss(double[][][] gradients, double loss) {
		double sum = 0;
		for (int i=0; i < gradients.length; i++)
			for (int j=0; j < gradients[i].length; j++)
				for (int k=0; k < gradients[i][j].length; k++)
					sum -= learning_rate * gradients[i][j][k] * gradients[i][j][k];
		return loss + sum;
	}
	
	private void adjustWeights(double[][][] weights, double[][][] gradients, double learning_rate) {
		double[][][] old_weights = new double[weights.length][][];
		for (int i=0; i < weights.length; i++) {
			old_weights[i] = new double[weights[i].length][];
			for (int j=0; j < weights[i].length; j++)
				old_weights[i][j] = Arrays.copyOf(weights[i][j], weights[i][j].length);
		}
		
		for (int l=0; l < weights.length; l++)
			for (int i=0; i < weights[l].length; i++)
				for (int j=0; j < weights[l][i].length; j++)
					weights[l][i][j] -= learning_rate * gradients[l][i][j];
		
		boolean problem = false;
		for (int l=0; l < weights.length; l++)
			for (int i=0; i < weights[l].length; i++)
				for (int j=0; j < weights[l][i].length; j++) {
					if (Double.isNaN(weights[l][i][j]))
						problem = true;
				}
		if (problem) {
			System.out.println("Model got out of bounds! try a smaller learning rate!");
			System.exit(0);
		}
	}
	
	public double getLearningRate() {
		return learning_rate;
	}
	public int getExplorationReplacements() {
		return exploration_replacements;
	}
	
	private class DescentThread extends Thread {
		
		private Supplier<double[][]> data;
		/**
		 * @param data a supplier of data points, which are as follows:<br>
		 *   data.get() = { input_array, target_array }<br>Data points are allowed to be repeated
		 *   and can be supplied randomly, this is up to the implementer.
		 */
		public DescentThread(Supplier<double[][]> data) {
			this.data = data;
		}
		
		@Override
		public void run() {
			iteration_count = 0;
			is_busy = true;
			while (!stop && (iteration_count <= max_iterations || max_iterations < 0)) {
				double[][] data_entry = data.get();
				trainingStep(data_entry[0], data_entry[1]);
				iteration_count++;
			}
			is_busy = false;
		}
	}
	
	public boolean hasNewData() {
		return last_loss_index < loss_values.size();
	}
	
	public int getIterations() {
		return iteration_count;
	}
	
	private synchronized void updateLatestData() {
		if (!hasNewData()) return;
		double sum = 0;
		int size = loss_values.size();
		int start_index = last_loss_index;
		if (size - start_index < min_data_interval)
			start_index = size - min_data_interval;
		if (start_index < 0) start_index = 0;
		for (int i=start_index; i < size; i++)
			sum += loss_values.get(i);
		double avg = sum / size;
		double var = 0;
		for (int i=start_index; i < size; i++)
			var += Math.pow(loss_values.get(i) - avg, 2);
		var /= size;
		latest_loss_avg = avg;
		latest_loss_sd = Math.sqrt(var);
		last_loss_index = size;
	}
	
	private double latest_loss_avg;
	private double latest_loss_sd;
	
	public synchronized double getCurrentLoss() {
		updateLatestData();
		return latest_loss_avg;
	}
	public synchronized double getCurrentLossSD() {
		updateLatestData();
		return latest_loss_sd;
	}
	
	public boolean isBusy() {
		return is_busy;
	}
	
	public void stop() {
		if (is_busy && !stop) stop = true;
	}
	
}
