package players.bots.utils;

import gamerules.*;
import gamerules.evaluation_functions.NormalizedSGD;
import players.Player;
import players.bots.NaivePlayerGreedy;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static gamerules.GameRules.SELECTED_GAMERULES;
import static players.bots.utils.NeuralNetwork.*;

public class DeepLearning {
    private static NeuralNetwork ann;
    private static Board game =new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
    private static int index = 0;
    private static double [] input;
    private static final double [] expected = new double[1];
    private static final double [] result = new double[1];;
    private static final NormalizedSGD heuristic = new NormalizedSGD();


    public static void associatedWithEuristic(){
        Supplier<double[][]> problem = () -> {
            test();
            double[] distances = new double[1];
            double min = Double.POSITIVE_INFINITY;
            distances[0] = HALF_SQUARE_ERROR.calculate(input, expected);
            for (int i=0; i < result.length; i++)
                result[i] = expected[i] - input[i];
			/*System.out.println("input = "+Utils.matrixToString(Utils.getRowVector(input))+" gives "+
				Utils.matrixToString(Utils.getRowVector(result)));*/
            return new double[][] {input, result};
        };
        int[] structure = {162, 81, 20, 20, 1};
        // The different activation function use in the ANN
        NeuralNetwork.Activation[] activations = {dSILU, dSILU, dSILU, SIGMOID};
        ann = new NeuralNetwork(structure, activations);
        //should be change
        ann.loadWeights(new double[][][]{});
        int index = 0;
        GradientDescent GD = new GradientDescent(ann, HALF_SQUARE_ERROR, 0.0003, 10000000);
    }

    private static double evaluate(GameState gameState, Player player){
        List<Integer> blackList = player.getOtherPlayersBase(gameState.getOriginalBoard());
        if(blackList.contains(gameState.lastMove().target_node) || blackList.contains(gameState.lastMove().start_node))
            return -Double.MAX_VALUE;
        return ann.forwardProp(gameState.getMatrix(player))[0];
    }

    private static void test(){
        while (game.noWinners()){
            GameState gs = new GameState(game);
            input = gs.getMatrix(game.currentPlayer());
            expected[0] = heuristic.apply(gs,game.currentPlayer());
            result[0] = ann.forwardProp(input)[0];
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
            if(index++ > 2000) break;
        }
    }
}
