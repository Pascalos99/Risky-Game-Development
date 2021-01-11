package game;

import java.awt.image.BufferedImage;

import game.events.GameEvent;
import game.events.MoveEvent;
import game.events.TurnEvent;
import game.events.WinEvent;
import gamerules.Board;
import gamerules.BoardNode;
import gamerules.GameRules;
import gamerules.Move;
import gamerules.evaluation_functions.NeuralNetworkEval;
import graphics.BoardGraphics;
import players.*;
import players.bots.*;
import static players.bots.utils.EveryoneShouldHaveMachineLearning.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

public class GamePanelAdapted extends GamePanel {
    private static final boolean DEBUG = false;
    public static boolean finishedGame = false;
    private static final String[] playerNames = new String[]{"Henry", "Melissa", "Frank", "Jessica", "Dave",
            "Paola"};
    private static final Color[] pieceColors = new Color[]{Color.cyan, Color.red, Color.green, Color.gray, Color.black, Color.blue};

    private static final long serialVersionUID = -5452925014639836146L;

    public static void main(String[] args) {
        finishedGame = false;
        if(args.length>=2){
            GameSetup gs = new GameSetup();
            for(int player=0; player<args.length;player++){
                gs.addPlayer(str2p(args[player]), playerNames[player], pieceColors[player]);
            }
            startGame(gs);
        }
    }

    public GamePanelAdapted(Board board, BoardGraphics graphics) {
        super(board, graphics);
    }

    private static Player str2p(String playerAsString){
        return switch (playerAsString) {
            case "np" -> new NaivePlayer();
            case "npnndls" -> new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-simple")));
            default -> new HumanPlayer();
        };
    }

    private Image selectedNodes = null;

    public void eventTick() {
        while (GameEvent.hasPending()) {
            GameEvent e = GameEvent.getNext();
            if (e instanceof TurnEvent) {
                Player player = ((TurnEvent) e).getPlayer();
                if (((TurnEvent) e).isEndOfTurn() && DEBUG) System.out.println("----Turn Ended----\n");
                else{
                    if(DEBUG){
                        System.out.format("=~=~ Now it's %s [%s]'s (%d) turn! ~=~=\n",
                                player.getName()+" ("+player.getTypeName()+")", player.getColorName(), player.turnCounter.getCount());
                        System.out.println("perspective of player:");
                    }
//					double[] perspective = new GameState(game).getMatrix(player);
//					for (int i=0; i < 9; i++) {
//						System.out.print("[ ");
//						for (int j=0; j < 9; j++)
//							System.out.format("% f ", perspective[i*9 + j]);
//						System.out.println("]");
//					}
                }
            } else if (e instanceof MoveEvent) {
                MoveEvent m = (MoveEvent) e;

                selectedNodes = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = selectedNodes.getGraphics();
                for(Move move: m.getMoves()){
                    BoardNode node = move.getTarget(game);
                    Point pos = graphics.getScreenPositionOfNode(node);
                    g.setColor(show_move_color);
                    int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
                    g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
                }
            } else if (e instanceof WinEvent) {
                System.out.println(e.getMessage());
                finishedGame = true;
            }
            else {
                //System.out.println(e);
            }
        }
    }

    private BoardNode previousMoveDisplay = null;

    public void generateMoveDisplay(BoardNode at) {
        if (at == previousMoveDisplay || at.isEmpty()) return;
        selectedNodes = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = selectedNodes.getGraphics();
        for(Move move: GameRules.SELECTED_GAMERULES.getAllPossibleMoves(game, at.getCurrentPawn())){
            BoardNode node = move.getTarget(game);
            Point pos = graphics.getScreenPositionOfNode(node);
            g.setColor(show_move_color);
            int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
            g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
        }
        previousMoveDisplay = at;
    }

    // graphics loop:
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(background_color);
        g.fillRect(0, 0, getWidth(), getHeight());
        graphics.setSize(getWidth(), getHeight());
        g.drawImage(graphics.getImage(), 0, 0, null);

        BoardNode pointer = HumanPlayer.GLOBAL_INPUT.getSelectedNode();

        if (pointer != null) {
            boolean highlight = false;
            // deciding highlight and move-display
            if (pointer.getCurrentPawn() != null && pointer.getCurrentPawn().getOwner() == game.currentPlayer()) {
                highlight = true;
                generateMoveDisplay(pointer);
            }

            // drawing selected node:
            Point pos = graphics.getScreenPositionOfNode(pointer);
            g.setColor(highlight? highlight_color : selection_color);
            int diameter = (int) (BoardGraphics.default_node_diameter * graphics.getScale());
            if (highlight) diameter *= 1.2;
            g.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
            // draw the selected nodes:
            if (selectedNodes != null && pointer.isOccupied() && pointer.getCurrentPawn().getOwner() == game.currentPlayer())
                g.drawImage(selectedNodes, 0, 0, null);
        }

        if (game.getPlayerCount() <= 0) {
            g.drawImage(graphics.createDebugImage(), 0, 0, null);
        }
    }
}
