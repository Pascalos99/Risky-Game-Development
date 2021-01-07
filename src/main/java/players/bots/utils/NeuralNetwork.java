package players.bots.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class NeuralNetwork {
	
	// Testing code
	public static void main(String[] args) {
		int[] structure = {3, 10, 15, 8, 1};
		Activation[] activations = {dSILU, dSILU, dSILU, LINEAR};
		NeuralNetwork ann = new NeuralNetwork(structure, activations);
		ann.initializeRandomWeights(-1, 1);
		double[][][] weights = ann.getAllWeights();
		for (int l=0; l < ann.getLayerCount(); l++)
			Utils.printMatrix(weights[l]);
		double[] input = {1,0,-1};
		System.out.println("with input:");
		Utils.printMatrix(Utils.getColumnVector(input));
		System.out.println("generates output:");
		Utils.printMatrix(Utils.getColumnVector(ann.forwardProp(input)));
	}
	
	private Layer[] hidden_layers;
	
	private static boolean[] getFilled(int length) {
		boolean[] result = new boolean[length];
		Arrays.fill(result, true);
		return result;
	}
	
	public NeuralNetwork(int[] network_structure, Activation[] activation_functions) {
		this(network_structure, getFilled(activation_functions.length), activation_functions);
	}
	
	public NeuralNetwork(int[] network_structure, boolean[] bias_inclusion, Activation[] activation_functions) {
		hidden_layers = new Layer[network_structure.length - 1];
		
		if (activation_functions.length < hidden_layers.length || bias_inclusion.length < hidden_layers.length)
			throw new IllegalArgumentException("not enough information provided for number of layers (requires "
					+hidden_layers.length+", got "+activation_functions.length+" and "+bias_inclusion.length+")");
		
		for (int a=0, b=1; b < network_structure.length; a++, b++)
			hidden_layers[a] = new Layer(network_structure[a], network_structure[b], bias_inclusion[a], activation_functions[a]);
	}
	
	/**
	 * Initializes the weights of this neural network according to the given weight supplier.
	 * @param weight_supplier given the integer input of layer index (starting from 0, ending at {@link #getLayerCount()}{@code -1})
	 *  should generate a (possibly random) floating point number for some weight in that layer.
	 */
	public void initializeWeights(Function<Integer, Double> weight_supplier) {
		for (int l=0; l < hidden_layers.length; l++) {
			double[][] weights = hidden_layers[l].weights;
			for (int i=0; i < weights.length; i++)
				for (int j=0; j < weights[i].length; j++)
					weights[i][j] = weight_supplier.apply(l);
		}
	}
	
	/**
	 * Initialized the weights of this neural network with random weights between the given minimum and maximum values.
	 * @param min the minimum weight to be generated (inclusive)
	 * @param max the maximum weight to be generated (exclusive)
	 * @param seed the seed of randomness for the weight generation
	 */
	public void initializeRandomWeights(double min, double max, long seed) {
		Random random = new Random(seed);
		initializeWeights(l -> random.nextDouble() * (max-min) + min);
	}
	
	/**
	 * Initialized the weights of this neural network with random weights between the given minimum and maximum values.
	 * @param min the minimum weight to be generated (inclusive)
	 * @param max the maximum weight to be generated (inclusive)
	 */
	public void initializeRandomWeights(double min, double max) {
		initializeRandomWeights(min, max, System.currentTimeMillis());
	}
	
	/**
	 * This method allows modification of the actual weights of this neural network and should thus be used carefully
	 * @return the weights of every layer in the network, in order of forward propagation
	 */
	public double[][][] getAllWeights() {
		double[][][] result = new double[hidden_layers.length][][];
		for (int l=0; l < result.length; l++)
			result[l] = hidden_layers[l].getWeights();
		return result;
	}
	
	public double[] forwardProp(double[] input) {
		int input_size = getInputSize();
		if (input.length != input_size) throw new IllegalArgumentException
			("input size of "+input.length+" does not match input size of layer ("+input_size+")");
		double[] current = input;
		for (int l=0; l < hidden_layers.length; l++)
			current = hidden_layers[l].forwardProp(current);
		return current;
	}
	
	public int getInputSize() {
		return hidden_layers[0].getInputSize();
	}
	public int getOutputSize() {
		return hidden_layers[hidden_layers.length-1].getOutputSize();
	}
	public int getLayerCount() {
		return hidden_layers.length;
	}
	public int[] getLayerStructure() {
		int[] structure = new int[hidden_layers.length+1];
		structure[0] = getInputSize();
		for (int l=0; l < hidden_layers.length; l++)
			structure[l+1] = hidden_layers[l].getOutputSize();
		return structure;
	}
	
	static class Layer {
		
		public Layer(int input_size, int output_size, boolean include_bias, Activation activation_function) {
			bias = include_bias;
			activation = activation_function;
			
			int input = input_size + (bias? 1:0);
			weights = new double[output_size][input];
		}
		
		public Layer(int input_size, int output_size, Activation activation) {
			this(input_size, output_size, true, activation);
		}
		
		private boolean bias;
		
		private double[][] weights;
		
		private Activation activation;
		
		public Activation getActivation() {
			return activation;
		}
		
		public double[] forwardPropRaw(double[] input) {
			int input_size = getInputSize();
			if (input.length != input_size) throw new IllegalArgumentException
				("input size of "+input.length+" does not match input size of layer ("+input_size+")");
			
			double[] input_vector = new double[weights[0].length];
			for (int i=0; i < input_vector.length; i++)
				if (i==0 && bias) input_vector[i] = 1;
				else if (bias) input_vector[i] = input[i-1];
				else input_vector[i] = input[i];
			
			return Utils.extractVector(Utils.matrixVectorMul(weights, input_vector));
		}
		
		public double[] forwardProp(double[] input) {
			return activation.activate(forwardPropRaw(input));
		}
		
		public int getInputSize() {
			return weights[0].length - (bias? 1:0);
		}
		public int getOutputSize() {
			return weights.length;
		}
		
		/**
		 * This method allows modification of the actual weights of this layer and should thus be used carefully
		 * @return the weights of this layer
		 */
		public double[][] getWeights() {
			return weights;
		}
		
	}
	
	/** should convert a number from <-inf, inf> to some value for activating a neuron (more positive is more activated) */
	public static class Activation {
		
		public final Function<Double, Double> activation;
		public final Function<Double, Double> derivative;
		
		public Activation(Function<Double, Double> activation, Function<Double, Double> derivative) {
			this.activation = activation;
			this.derivative = derivative;
		}
		public double activate(double x) {
			return activation.apply(x);
		}
		public double derivate(double x) {
			return derivative.apply(x);
		}
		public double[] activate(double[] x) {
			double[] r = new double[x.length];
			for (int i=0; i < r.length; i++)
				r[i] = activate(x[i]);
			return r;
		}
	}
	
	public static final Activation LINEAR = new Activation(x -> x, x -> 1d);
	
	public static final Activation SIGMOID = new Activation(x -> 1 / (1 + Math.exp(-x)), x -> {
		double f = 1 / (1 + Math.exp(-x));
		return f * (1 - f);
	});
	
	// linear rectifier (ramp-function)
	public static final Activation RELU = new Activation(x -> Math.max(0, x), x -> (x < 0) ? 0d : 1d );
	
	// heaviside function
	public static final Activation STEP = new Activation(x -> (x > 0)? 1d : 0d, x -> 0d );
	
	// [credit to https://arxiv.org/pdf/1606.08415.pdf and https://arxiv.org/pdf/1702.03118.pdf]
	public static final Activation SILU = new Activation(x -> x * SIGMOID.activate(x), x -> {
		double s = SIGMOID.activate(x);
		return s * (1 + x * (1 - s));
	});
	
	// derivative of SILU, https://arxiv.org/pdf/1702.03118.pdf found this activation function is really good
	public static final Activation dSILU = new Activation(SILU.derivative, x -> {
		double s = SIGMOID.activate(x);
		// σ(x)(1 − σ(x))(2 + x(1 − σ(x)) − x*σ(x)) [credit to https://arxiv.org/pdf/1702.03118.pdf]
		return s * (1 - s) * (2 + x * (1 - s) - x * s);
	});

}
