package players.bots.utils;

import game.GamePanel;
import game.GameSetup;
import game.events.GameEvent;
import gamerules.*;
import gamerules.evaluation_functions.NeuralNetworkEval;
import gamerules.evaluation_functions.NormalizedSGD;
import gamerules.evaluation_functions.SimpleGoalDistance;
import players.HumanPlayer;
import players.Player;
import players.bots.AlphaBeta;
import players.bots.EvilPlayer;
import players.bots.GreedyMST;
import players.bots.NaivePlayer;
import players.bots.NaivePlayerGreedy;
import players.bots.RandomGreedy;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static gamerules.GameRules.SELECTED_GAMERULES;
import static players.bots.utils.NeuralNetwork.*;

public class DeepLearning {
    private static NeuralNetwork ann;
    private static Board game = new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
    private static int index = 0;
    private static double [] input;
    private static final double [] expected = new double[1];
    private static final NormalizedSGD heuristic = new NormalizedSGD();
    private static boolean loaded = false;

    private static boolean smart_learn = true;
    public static int limit = 2000;

    public static void main(String[] args) throws IOException {
    	loadANN();
    	NeuralNetwork old = ann.clone();
        associatedWithEuristic();
        storeANN();
        GameEvent.clearAll();
        Player newANN = new NaivePlayer(new NeuralNetworkEval(ann));
        newANN.setName("new ANN"); newANN.setColor(Color.green);
        Player oldANN = new NaivePlayer(new NeuralNetworkEval(old));
        oldANN.setName("old ANN"); oldANN.setColor(Color.red);
        runGame(oldANN, newANN);
        runGame(newANN, oldANN);
    }

    public static void associatedWithEuristic(){
        Supplier<double[][]> problem = () -> {
            test();
            return new double[][] {input, expected};
        };
        if (!loaded) {
	        int[] structure = {162, 81, 20, 20, 1};
	        // The different activation function use in the ANN
	        NeuralNetwork.Activation[] activations = {dSILU, dSILU, dSILU, SIGMOID};
	        ann = new NeuralNetwork(structure, activations);
	        ann.initializeRandomWeights(-1, 1);
	        System.out.println("Generated new ANN weights");
        } else {
        	System.out.println("loaded ANN from memory");
        }
        System.out.println("Starting up gradient descent...");
        GradientDescent GD = new GradientDescent(ann, HALF_SQUARE_ERROR, 0.0003, 5000);
        GD.start(problem);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e1) {}
        while (GD.isBusy()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            if (GD.hasNewData()) {
            	System.out.println("calculated "+GD.getIterations()+" iterations");
            	System.out.println("Loss = "+GD.getCurrentLoss());
            }
        }
    }

    private static double evaluate(GameState gameState, Player player){
        List<Integer> blackList = player.getOtherPlayersBase(gameState.getOriginalBoard());
        if(blackList.contains(gameState.lastMove().target_node) || blackList.contains(gameState.lastMove().start_node))
            return -Double.MAX_VALUE;
        return ann.forwardProp(gameState.getMatrix(player))[0];
    }

    public static void loadANN() {
    	try {
    		loadANN(getLatestID());
    	} catch(RuntimeException e) {
    		e.printStackTrace();
    	}
    }

    public static void loadANN(int ID) {
    	if (ID < 0) throw new RuntimeException("failed to load ANN (ID non-valid)");
    	File folder = new File(NeuralNetwork.networkPath+"DL-test");
    	File found = null;
    	folder.mkdir();
    	for (File file : folder.listFiles()) {
    		String num = file.getName().replaceAll("[^\\d]","");
    		if (!num.equals("")) {
    			int val = Integer.parseInt(num);
    			if (val == ID) {
    				found = file;
    				break;
    			}}}
    	if (found != null)
			try {
				ann = NeuralNetwork.readFromFile(found)[0];
				loaded = true;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("failed to load ANN (file corrupted)");
			}
    }

    public static void storeANN(int ID) throws IOException {
    	File new_file = new File(NeuralNetwork.networkPath+"DL-test"+File.separator+"ann-"+ID+".network");
    	ann.storeToFile(new_file);
    }

    public static void storeANN() throws IOException {
    	storeANN(getLatestID()+1);
    }

    private static int getLatestID() {
    	File folder = new File(NeuralNetwork.networkPath+"DL-test");
    	folder.mkdir();
    	int max = -1;
    	for (File file : folder.listFiles()) {
    		String num = file.getName().replaceAll("[^\\d]","");
    		if (!num.equals("")) {
    			int val = Integer.parseInt(num);
    			if (val > max) max = val;
    		}
    	}
    	return max;
    }

    private static void test(){
    	if (smart_learn) {
    		smartLearn();
    		return;
    	}
        if (game.noWinners()){
            GameState gs = new GameState(game);
            input = gs.getMatrix(game.currentPlayer());
            expected[0] = heuristic.apply(gs,game.currentPlayer());
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
            if(index++ > limit){
                game = new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
                index = 0;
                test();
            }
        }
        else {
            game = new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
            index = 0;
            test();
        }
    }

    private static List<Double> state_outputs = new LinkedList<>();
    private static List<double[]> state_inputs = new LinkedList<>();
    private static boolean player_order = true;

    private static void smartLearn() {
		if (state_outputs.isEmpty()) {
			Player player1 = new AlphaBeta();
			player1.setName("player1");
			Player player2 = new GreedyMST();
			player2.setName("player2");
			GameState gs;
			List<double[]> state1_inputs = new LinkedList<>();
			List<double[]> state2_inputs = new LinkedList<>();
			int iter = 0;
			int max_iter = limit;
			if (player_order) game = new Board(GameRules.SELECTED_GAMERULES, null, player1, player2);
			else game = new Board(GameRules.SELECTED_GAMERULES, null, player2, player1);
			while (game.noWinners() && iter++ <= max_iter) {
				gs = new GameState(game);
	            if (game.currentPlayer() == player1) state1_inputs.add(gs.getMatrix(player1));
	            if (game.currentPlayer() == player2) state2_inputs.add(gs.getMatrix(player2));
	            game.forceRequestMoveAndContinue();
			} gs = new GameState(game);
			//System.out.println("winner = "+game.getWinner());
			double output;
			if (iter >= max_iter) {
				output = heuristic.eval(gs, player1);
				double norm = heuristic.eval(gs, player2) + output;
				output /= norm;
			} else output = (game.getWinner() == player1)? 1:0;
			//System.out.println("game output = "+output);
			for (int i=0; i < state1_inputs.size(); i++) {
				state_outputs.add(output);
				state_inputs.add(state1_inputs.get(i));
			}
			for (int i=0; i < state2_inputs.size(); i++) {
				state_outputs.add(1 - output);
				state_inputs.add(state2_inputs.get(i));
			}
			System.out.println("Game output set to "+output);

			player_order = !player_order;
		}
		int random = (int) (Math.random() * state_inputs.size());
		input = state_inputs.remove(random);
		expected[0] = state_outputs.remove(random);
    }

    private static void runGame(Player...players) {
    	GameSetup gs = new GameSetup();
		for (int i=0; i < 6 && i < players.length; i++) {
			gs.addPlayer(players[i], players[i].getName(), players[i].getColor());
		}
		GamePanel.startGame(gs);
    }
}
