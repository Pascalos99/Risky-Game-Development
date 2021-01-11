package game.tests;

import game.GameSetup;
import gamerules.Board;
import gamerules.evaluation_functions.NeuralNetworkEval;
import players.HumanPlayer;
import players.Player;
import players.bots.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static players.bots.utils.EveryoneShouldHaveMachineLearning.loadNetwork;

/**
 * Class for testing bots performance against each other
 */
public class TestingBots {

    private static final int TESTS_NUMBER = 15;
    private static final ArrayList<String> playerNames = new ArrayList<>(Arrays.asList("Henry", "Melissa", "Frank", "Jessica", "Dave", "Paola"));
    private static final Color[] pieceColors = new Color[]{Color.cyan, Color.red, Color.green, Color.gray, Color.black,
            Color.blue};
    private static final String[] players = new String[]{"np","npnndls","ep"};

    public static void main(String[] args){
        int[] wins = new int[players.length];
        Board gameBoard = null;
        for(int game=0; game<TESTS_NUMBER; game++) {
            GameSetup gameSetup = new GameSetup();
            for (int i = 0; i < players.length; i++) {
                gameSetup.addPlayer(str2p(players[i]), playerNames.get(i), pieceColors[i]);
            }
            gameBoard = gameSetup.getBoard();
            while(gameBoard.noWinners()){
                gameBoard.forceRequestMoveAndContinue();
            }
            wins[playerNames.indexOf(gameBoard.getWinner().getName())]++;
        }
        System.out.println("GAMES WON:");
        for(int i=0; i<players.length;i++){
            System.out.println(gameBoard.getPlayer(i)+": "+wins[i]);
        }
    }

    private static Player str2p(String playerAsString){
        return switch (playerAsString) {
            case "np" -> new NaivePlayer();
            case "npnndls" -> new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-simple")));
            case "rp" -> new RandomPlayer();
            case "ep" -> new EvilPlayer();
            case "lbfsp" -> new LBFSPlayer();
            case "pmstp" -> new ProtoMST();
            case "gmstp" -> new GreedyMST();
            case "abp" -> new AlphaBeta();
            case "asp" -> new AStar();
            case "hp" -> new HumanPlayer();
            default -> null;
        };
    }
}
