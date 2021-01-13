package game.tests;

import java.util.ArrayList;
import java.util.List;

import game.GameSetup;
import gamerules.Board;
import gamerules.GameState;
import players.bots.utils.DataLoader;
import players.bots.utils.DataLoader.Data;
import players.bots.utils.DataLoader.DataPoint;

public class MTRunnable implements Runnable {

	public MTRunnable(Integer state_id) {
		this.state_id = state_id;
	}

	private static int last_state_id = 0;
	public Integer state_id = null;
	
	public static int max_turns = 2000;

    @Override
    public void run() {
        try {
            //For checking if the number of threads ran is correct
            // System.out.println("Firing Thread"+Thread.currentThread().getId());
            // secure dummy board safety:
            synchronized(MTRunnable.class) {
	            GameState.thread_dummy_ids.put(Thread.currentThread(),
	            		(state_id == null)? ++last_state_id : state_id);
            }
            GameSetup gameSetup = new GameSetup();
            for (int i = 0; i < TestingBots.players.length; i++) {
                gameSetup.addPlayer(TestingBots.str2p(TestingBots.players[i]), TestingBots.playerNames.get(i), TestingBots.pieceColors[i]);
            }
            Board gameBoard = gameSetup.getBoard();
            List<DataPoint> data = new ArrayList<>();
            int iter = 0;
            while (gameBoard.noWinners() || ++iter > max_turns) {
            	data.add(new DataPoint(new GameState(gameBoard)));
                gameBoard.forceRequestMoveAndContinue();
            }
            TestingBots.wins[TestingBots.playerNames.indexOf(gameBoard.getWinner().getName())]++;
            TestingBots.tests_completed++;
            
            storeData(data, gameBoard.currentPlayerID());
            
            TestingBots.occupied.remove(Integer.valueOf(state_id));
            //For checking if the number of threads ran is correct
            //System.out.println("Finished thread"+Thread.currentThread().getId());
        }
        catch(Exception e){
            System.out.println ("Exception "+e+" is caught");
            e.printStackTrace();
        }
        synchronized (TestingBots.waitingObject){
            if(TestingBots.tests_completed == TestingBots.TESTS_NUMBER){
                TestingBots.waitingObject.notify();
            }
        }
        synchronized (TestingBots.threadWait){
            TestingBots.current_threads--;
            TestingBots.threadWait.notify();
        }
        Thread.currentThread().interrupt();
    }

    private void storeData(List<DataPoint> data, int winning_player){
    	Data resulting_data = new Data();
    	for (int i=0; i < data.size(); i++) {
    		data.get(i).winning_player = winning_player;
    		resulting_data.addData(data.get(i));
    	}
    	DataLoader.saveData("general_testing", resulting_data);
    }
}
