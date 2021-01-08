package gamerules.evaluation_functions;

import gamerules.EvaluationFunction;
import gamerules.GameState;
import players.Player;
import players.bots.utils.NeuralNetwork;

public class NeuralNetworkEval implements EvaluationFunction {

	private NeuralNetwork ann;
	
	/**
	 * @param net a network with an input vector of 162 and an output vector of 1
	 */
	public NeuralNetworkEval(NeuralNetwork net) {
		if (net.getInputSize() != 162 || net.getOutputSize() != 1)
			throw new IllegalArgumentException("network must be [162] in and [1] out");
		ann = net;
	}

	@Override
	public Double apply(GameState t, Player u) {
		return ann.forwardProp(t.getMatrix(u))[0];
	}
	
	public String toString() {
		return "ANN";
	}
	
}
