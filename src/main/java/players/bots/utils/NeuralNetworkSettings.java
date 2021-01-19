package players.bots.utils;

import java.util.Arrays;

import players.bots.utils.NeuralNetwork.Activation;

public class NeuralNetworkSettings {

	int[] structure;
	Activation[] activations;
	boolean[] bias;
	BoardRep boardRep;
	
	public NeuralNetworkSettings(int[] structure, Activation[] activations) {
		this(structure, activations, fill(structure.length-1, true), BoardRep.Original);
	}
	public NeuralNetworkSettings(int[] structure, Activation[] activations, boolean[] bias, BoardRep boardRep) {
		this.structure = structure;
		this.activations = activations;
		this.bias = bias;
		this.boardRep = boardRep;
	}
	private static boolean[] fill(int size, boolean value) {
		boolean[] x = new boolean[size];
		Arrays.fill(x, value);
		return x;
	}
	
	public NeuralNetwork getEmpty() {
		return new NeuralNetwork(structure, bias, activations);
	}
	public NeuralNetwork getRandomWeights(double min, double max) {
		return getRandomWeights(min, max, System.currentTimeMillis());
	}
	public NeuralNetwork getRandomWeights(double min, double max, long seed) {
		NeuralNetwork result = getEmpty();
		result.initializeRandomWeights(min, max, seed);
		return result;
	}
	
	public BoardRep getBoardRep() {
		return boardRep;
	}
	
	public boolean matches(NeuralNetwork net) {
		int[] struct = net.getLayerStructure();
		if (struct.length != structure.length) return false;
		for (int i=0; i < struct.length; i++) if (struct[i] != structure[i]) return false;
		Activation[] acts = net.getActivations();
		for (int i=0; i < acts.length; i++) if (acts[i] != activations[i]) return false;
		boolean[] bsi = net.getBiasInclusion();
		for (int i=0; i < bsi.length; i++) if (bsi[i] != bias[i]) return false;
		return true;
	}
	
}
