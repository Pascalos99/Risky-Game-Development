package players.bots.utils;

import java.util.function.Function;

public class NeuralNetwork {
	
	static class Layer {
		private double[][] weights;
		private Activation activation;
	}
	
	/** should convert a number from <-inf, inf> to [-1,1] */
	public static interface Activation extends Function<Double, Double> {}
	
	private int num_inputs;
	private Layer[] hidden_layers;

}
