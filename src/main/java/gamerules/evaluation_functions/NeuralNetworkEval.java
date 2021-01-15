package gamerules.evaluation_functions;

import java.util.Arrays;

import gamerules.Board;
import gamerules.EvaluationFunction;
import gamerules.GameState;
import players.Player;
import players.bots.utils.BoardRep;
import players.bots.utils.NeuralNetwork;

public class NeuralNetworkEval implements EvaluationFunction {

	private NeuralNetwork ann;
	
	private BoardRep boardrep;
	
	public NeuralNetworkEval(NeuralNetwork net, BoardRep board_rep) {
		boardrep = board_rep;
		if (net.getInputSize() != boardrep.input_size || net.getOutputSize() != boardrep.output_size)
			throw new IllegalArgumentException(String.format("network must be [%d] in and [%d] out", boardrep.input_size, boardrep.output_size));
		ann = net;
	}
	/**
	 * @param net a network with an input vector of 162 and an output vector of 1
	 */
	public NeuralNetworkEval(NeuralNetwork net) {
		this(net, BoardRep.Original);
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
		return ann.forwardProp(t.getMatrixUnrolled(u, boardrep))[0];
	}
	
	public BoardRep getBoardRep() {
		return boardrep;
	}
	
	public String toString() {
		return "ANN";
	}
	
}
