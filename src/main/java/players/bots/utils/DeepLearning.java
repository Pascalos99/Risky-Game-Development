package players.bots.utils;

import game.GamePanel;
import gamerules.*;
import gamerules.evaluation_functions.NormalizedSGD;
import players.Player;
import players.bots.NaivePlayer;
import players.bots.NaivePlayerGreedy;
import java.util.HashMap;
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

    public static void main(String[] args) {
        associatedWithEuristic();
    }

    public static void associatedWithEuristic(){
        Supplier<double[][]> problem = () -> {
            test();
            return new double[][] {input, expected};
        };
        int[] structure = {162, 81, 20, 20, 1};
        // The different activation function use in the ANN
        NeuralNetwork.Activation[] activations = {dSILU, dSILU, dSILU, SIGMOID};
        ann = new NeuralNetwork(structure, activations);
        ann.initializeRandomWeights(-1, 1);
        //should be change
//        ann.loadWeights(new double[][][]{});
        GradientDescent GD = new GradientDescent(ann, HALF_SQUARE_ERROR, 0.003, 50000);
        GD.start(problem);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e1) {}
        while (GD.isBusy()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            System.out.println("Loss = "+GD.getCurrentLoss());
        }
    }

    private static double evaluate(GameState gameState, Player player){
        List<Integer> blackList = player.getOtherPlayersBase(gameState.getOriginalBoard());
        if(blackList.contains(gameState.lastMove().target_node) || blackList.contains(gameState.lastMove().start_node))
            return -Double.MAX_VALUE;
        return ann.forwardProp(gameState.getMatrix(player))[0];
    }

    private static void test(){
        if (game.noWinners()){
            GameState gs = new GameState(game);
            input = gs.getMatrix(game.currentPlayer());
            expected[0] = heuristic.apply(gs,game.currentPlayer());
            game.nextPlayer();
            game.forceRequestMoveAndContinue();
//            if(index++ > 2000){
//                game = new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
//                index = 0;
//                test();
//            }
        }
        else {
            game = new Board(GameRules.SELECTED_GAMERULES, null, new NaivePlayerGreedy(),new NaivePlayerGreedy());
            index = 0;
            test();
        }
    }
}
