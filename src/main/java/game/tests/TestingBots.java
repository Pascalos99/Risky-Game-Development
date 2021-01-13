package game.tests;

import game.GameSetup;
import gamerules.Board;
import gamerules.evaluation_functions.NeuralNetworkEval;
import gamerules.evaluation_functions.NormalizedSGD;
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
    protected static int TESTS_NUMBER;
    protected static int tests_completed;
    protected static final ArrayList<String> playerNames = new ArrayList<>(Arrays.asList("Henry", "Melissa", "Frank", "Jessica", "Dave", "Paola"));
    protected static final Color[] pieceColors = new Color[]{Color.cyan, Color.red, Color.green, Color.gray, Color.black, Color.blue};
    protected static String[] players;
    protected static int[] wins;
    protected static int ties;
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
    
    public static void oldMain(String[] args) {
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
            System.out.println(occupied);
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
            System.out.println(gameBoard.getPlayer(i).getTypeName()+": "+wins[i]);
        }
        System.out.println("GAMES TIED: "+ties);
        System.out.println("Time taken:"+(finishTime-startTime)+" nanoseconds");
    }
    
    public static void main(String[] args) throws InterruptedException {
    	MTRunnable.data_name = "new_testing";
    	MTRunnable.max_turns = 2000;
    	tests_completed = 0;
    	TESTS_NUMBER = 16;
    	players = new String[]{"rg","rgann"};
        wins = new int[players.length];
        ties = 0;
        Thread running = new Thread(() -> oldMain(args));
        running.start();
        running.join();
        tests_completed = 0;
    	TESTS_NUMBER = 16;
    	players = new String[]{"rgann","rg"};
        wins = new int[players.length];
        ties = 0;
        oldMain(args);
    }

    protected static Player str2p(String playerAsString){
        return switch (playerAsString) {
            case "np" -> new NaivePlayer();
            case "npann" -> new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-working test")));
            case "rp" -> new RandomPlayer();
            case "ep" -> new EvilPlayer();
            case "lbfsp" -> new LBFSPlayer();
            case "pmstp" -> new ProtoMST();
            case "gmstp" -> new GreedyMST();
            case "abp" -> new AlphaBeta();
            case "asp" -> new AStar();
            case "hp" -> new HumanPlayer();
            case "rg" -> new RandomGreedy(new NormalizedSGD(), 6);
            case "rgann" -> new RandomGreedy(new NeuralNetworkEval(loadNetwork("DL-working test")));
            default -> null;
        };
    }


}
