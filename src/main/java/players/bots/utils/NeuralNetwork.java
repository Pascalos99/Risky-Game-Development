package players.bots.utils;

import java.util.function.Function;

public class NeuralNetwork {
	
	static class Layer {
		
		private double[][] weights;
		
		private Activation activation;
		
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
	}
	
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
	
	private int num_inputs;
	private Layer[] hidden_layers;

}
