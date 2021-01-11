package players.bots.utils;

public enum BoardRep {

	Original(162, 1), TwoNoNegatives(162, 1);
	
	public final int input_size;
	public final int output_size;
	
	BoardRep(int input, int output) {
		input_size = input;
		output_size = output;
	}
	
}
