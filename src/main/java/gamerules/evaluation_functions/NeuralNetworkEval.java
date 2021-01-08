package gamerules.evaluation_functions;

import java.util.Arrays;

import gamerules.Board;
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
		Board original = t.getOriginalBoard();
		byte[] rep = t.getIntegerRepresentation();
		int id = original.getPlayerID(u);
		for (int i=0; i < 10; i++) {
			byte pos = rep[id*10 + i];
			for (int p=0; p < 6; p++) {
				if (p == id || p == Board.player_pairings[id]) continue;
				if (Arrays.binarySearch(Board.nodes_owned_per_player[p], pos) >= 0)
					return Double.NEGATIVE_INFINITY;
			}
		}
		return ann.forwardProp(t.getMatrix(u))[0];
	}
	
	public String toString() {
		return "ANN";
	}
	
}
