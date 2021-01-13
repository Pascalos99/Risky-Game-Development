package game.tests;

import game.GameSetup;
import gamerules.Board;
import gamerules.evaluation_functions.NeuralNetworkEval;
import players.HumanPlayer;
import players.Player;
import players.bots.*;
import java.util.List;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static players.bots.utils.EveryoneShouldHaveMachineLearning.loadNetwork;

/**
 * Class for testing bots performance against each other
 */
public class TestingBots {

    protected static final Object waitingObject = new Object();
    protected static final Object threadWait = new Object();
    protected static final int TESTS_NUMBER = 200;
    protected static int tests_completed = 0;
    protected static final ArrayList<String> playerNames = new ArrayList<>(Arrays.asList("Henry", "Melissa", "Frank", "Jessica", "Dave", "Paola"));
    protected static final Color[] pieceColors = new Color[]{Color.cyan, Color.red, Color.green, Color.gray, Color.black,
            Color.blue};
    protected static final String[] players = new String[]{"np","npnndls","ep", "rp"};
    protected static final int[] wins = new int[players.length];
    private static final int LOGICAL_THREADS = Runtime.getRuntime().availableProcessors();
    protected static int current_threads = 0;

    //Initial main without threading

    /*public static void main(String[] args){
        long startTime = System.nanoTime();
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
        long finishTime = System.nanoTime();
        System.out.println("Time taken: "+(finishTime-startTime));
        System.out.println("GAMES WON:");
        for(int i=0; i<players.length;i++){
            System.out.println(gameBoard.getPlayer(i)+": "+wins[i]);
        }
    }*/

    public static List<Integer> occupied;
    
    public static void main(String[] args) throws InterruptedException {
        long startTime = System.nanoTime();
        GameSetup gameSetup = new GameSetup();
        for (int i = 0; i < players.length; i++) {
            gameSetup.addPlayer(str2p(players[i]), playerNames.get(i), pieceColors[i]);
        }
        Board gameBoard = gameSetup.getBoard();
        occupied = new ArrayList<>();
        for(int game=0; game<TESTS_NUMBER; game++){
        	int id = game;
        	for (int i=0; i < TESTS_NUMBER; i++)
        		if (!occupied.contains(i)) {
        			id = i;
        			break;
        		}
            Thread gameThread = new Thread(new MTRunnable(id));
            occupied.add(id);
            gameThread.start();
            current_threads++;
            if(current_threads==LOGICAL_THREADS) {
                synchronized (threadWait) {
                    try {
                        threadWait.wait();
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }

        synchronized (waitingObject){
            try{
                waitingObject.wait();
            }
            catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
        long finishTime = System.nanoTime();
        System.out.println("GAMES WON:");
        for(int i=0; i<players.length;i++){
            System.out.println(gameBoard.getPlayer(i)+": "+wins[i]);
        }
        System.out.println("Time taken:"+(finishTime-startTime)+" nanoseconds");
    }

    protected static Player str2p(String playerAsString){
        return switch (playerAsString) {
            case "np" -> new NaivePlayer();
            case "npnndls" -> new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-working test")));
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
