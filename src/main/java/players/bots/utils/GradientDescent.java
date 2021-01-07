package players.bots.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import players.bots.utils.NeuralNetwork.LossFunction;
import players.bots.utils.NeuralNetwork.Activation;
import static players.bots.utils.NeuralNetwork.*;

public class GradientDescent {

	public static void main(String[] args) {
		Random randomizer = new Random();
		int dimension = 2;
		List<double[]> goal_points = List.of(new double[][] {
			{0, 0},
			{2, 2}
		});
		// the dummy problem is:
		//  given your current position V, output a vector U such that the result
		//  V + U is as close as possible to the closest goal point
		Supplier<double[][]> problem = () -> {
			double[] input = new double[dimension];
			for (int i=0; i < input.length; i++)
				input[i] = randomizer.nextDouble() * 4 - 2;
			double[] distances = new double[goal_points.size()];
			double min = Double.POSITIVE_INFINITY;
			int best = -1;
			for (int i=0; i < goal_points.size(); i++) {
				distances[i] = HALF_SQUARE_ERROR.calculate(input, goal_points.get(i));
				if (distances[i] < min) {
					min = distances[i];
					best = i;
				}}
			double[] result = new double[dimension];
			for (int i=0; i < result.length; i++)
				result[i] = goal_points.get(best)[i] - input[i];
			/*System.out.println("input = "+Utils.matrixToString(Utils.getRowVector(input))+" gives "+
				Utils.matrixToString(Utils.getRowVector(result)));*/
			return new double[][] {input, result};
		};
		
		NeuralNetwork model = new NeuralNetwork(new int[] {2, 5, 2}, new Activation[] {RELU, LINEAR});
		model.initializeRandomWeights(-1, 1);
		GradientDescent GD = new GradientDescent(model, HALF_SQUARE_ERROR, 0.0003, 10000000);
		GD.start(problem);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {}
		while (GD.isBusy()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			System.out.println("Loss = "+GD.getCurrentLoss());
		}
		System.out.println("weights after all that training:");
		double[][][] w = model.getAllWeights();
		for (int l=0; l < w.length; l++) {
			Utils.printMatrix(w[l]);
		}
	}
	
	private Tunable model;
	private double learning_rate;
	private LossFunction loss;
	private int max_iterations;
	
	private double current_loss;
	private boolean is_busy = false;
	private boolean stop = false;
	private int iteration_count;
	
	private DescentThread thread;
	
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
	}
	public GradientDescent(Tunable ann, LossFunction loss, double learning_rate) {
		this(ann, loss, learning_rate, -1);
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
	
	private void adjustWeights(double[] input, double[] target) {
		double[] output = model.computeOutput(input);
		current_loss = loss.calculate(output, target);
		double[][][] gradients = model.calculateLossGradients(loss, target);
		double[][][] weights = model.getAllWeights();
		
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
				for (int j=0; j < weights[l][i].length; j++)
					if (Double.isNaN(weights[l][i][j]))
						problem = true;
		if (problem) {
			System.out.println("Model got out of bounds! try a smaller learning rate!");
			System.exit(0);
		}
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
			while (!stop && iteration_count <= max_iterations) {
				double[][] data_entry = data.get();
				adjustWeights(data_entry[0], data_entry[1]);
				iteration_count++;
			}
			is_busy = false;
		}
	}
	
	public double getCurrentLoss() {
		return current_loss;
	}
	
	public boolean isBusy() {
		return is_busy;
	}
	
	public void stop() {
		if (is_busy && !stop) stop = true;
	}
	
}
