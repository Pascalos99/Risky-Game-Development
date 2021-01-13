package game.tests;

import game.GameSetup;
import gamerules.Board;

public class MTRunnable implements Runnable {

    @Override
    public void run() {
        try {
            //For checking if the number of threads ran is correct
            // System.out.println("Firing Thread"+Thread.currentThread().getId());
            GameSetup gameSetup = new GameSetup();
            for (int i = 0; i < TestingBots.players.length; i++) {
                gameSetup.addPlayer(TestingBots.str2p(TestingBots.players[i]), TestingBots.playerNames.get(i), TestingBots.pieceColors[i]);
            }
            Board gameBoard = gameSetup.getBoard();
            while (gameBoard.noWinners()) {
                gameBoard.forceRequestMoveAndContinue();
            }
            TestingBots.wins[TestingBots.playerNames.indexOf(gameBoard.getWinner().getName())]++;
            TestingBots.tests_completed++;
            //For checking if the number of threads ran is correct
            //System.out.println("Finished thread"+Thread.currentThread().getId());

            storeData();
        }
        catch(Exception e){
            System.out.println ("Exception is caught");
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
    }

    private void storeData(){
    }
}
