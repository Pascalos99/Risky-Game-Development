package game;

public class TestingBots {
    private static final int TESTS_NUMBER = 200;
    private static final String[] players = new String[]{"np","npnndls","np"};
    private static final Object waiting = new Object();
    public static void main(String[] args) throws InterruptedException {
        GamePanelAdapted.main(players);
        /*for(int i=0;i<TESTS_NUMBER;i++){
            GamePanelAdapted.main(players);
                while(!GamePanelAdapted.finishedGame) {
                    waiting.wait();
                }
            }*/
        }
    }
