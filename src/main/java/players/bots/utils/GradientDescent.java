package players.bots.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import players.bots.utils.NeuralNetwork.LossFunction;
import players.bots.utils.NeuralNetwork.Activation;
import static players.bots.utils.NeuralNetwork.*;

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
		loss_values.add(loss.calculate(output, target));
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
			while (!stop && (iteration_count <= max_iterations || max_iterations < 0)) {
				double[][] data_entry = data.get();
				adjustWeights(data_entry[0], data_entry[1]);
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
