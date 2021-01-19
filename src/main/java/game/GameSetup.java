package game;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gamerules.Board;
import gamerules.DefaultGameRules;
import gamerules.GameRules;
import graphics.BoardGraphics;
import players.*;
import players.bots.*;
import gamerules.evaluation_functions.NeuralNetworkEval;
import gamerules.evaluation_functions.PaperEval;

import static players.bots.utils.EveryoneShouldHaveMachineLearning.loadNetwork;

/**
 * [How-to-use]
 * This is like a class of the Builder design pattern to help create a GamePanel with the GUI
 * 
 * 1. create a new GameSetup object:
 * GameSetup gs = new GameSetup();
 * 
 * 2. add players to the game-setup using variables you can gather from user input:
 * gs.addPlayer(player_type_name, player_name, player_color);
 * - player_type_name is a string corresponding to any element in {@link #player_type_names} (this does not care about spaces and uppercases)
 * - player_name is a string the user puts in
 * - player_color is any java.awt.Color corresponding to the color of a player's pawns
 */
public class GameSetup {
	
	private static List<Player> player_types = List.of(new HumanPlayer(), new RandomPlayer(), new NaivePlayer(), 
			new NaivePlayer(new NeuralNetworkEval(loadNetwork("DL-working test"))), new NaivePlayer(new PaperEval()), 
			new EvilPlayer(), new LBFSPlayer(),new ProtoMST(),new GreedyMST(), new GreedyMST(new NeuralNetworkEval(loadNetwork("DL-working test"))), 
			new GreedyMST(new PaperEval()),new AlphaBeta(), new AlphaBeta(new NeuralNetworkEval(loadNetwork("DL-working test"))), 
			new AlphaBeta(new PaperEval()),new AStar());
	private static List<GameRules> gamerule_types = List.of(new DefaultGameRules());
	
	/** This list can be used for selection buttons: */
	public static List<String> player_type_names = player_types.stream().map(Player::getTypeName).collect(Collectors.toUnmodifiableList());
	/** This list can be used for extra info on selection buttons: */
	public static List<String> player_type_descr = player_types.stream().map(Player::getDescription).collect(Collectors.toUnmodifiableList());
	/** This list can be used for selection buttons: */
	public static List<String> gamerule_names = gamerule_types.stream().map(GameRules::getName).collect(Collectors.toUnmodifiableList());
	
	public static final int DEFAULT_BOARD_GRAPHICS = 0;
	public static final int SIMPLE_CIRCLE_GRAPHICS = 1;
	public static final int REALISTIC_BOARD_GRAPHICS = 2;
	
	private int board_graphics_setting = 0;
	private List<Player> players;
	
	public GameSetup() {
		players = new ArrayList<>();
	}
	
	public void setBoardGraphics(int setting) {
		board_graphics_setting = setting;
	}
	
	public void setGameRules(String gamerule_name) {
		GameRules.SELECTED_GAMERULES = gamerule_types.get(gamerule_names.indexOf(gamerule_name));
	}
	
	public GamePanel build() {
		Board board = getBoard();
		BoardGraphics graphics = getGraphics(board);
		return new GamePanel(board, graphics);
	}
	
	public Board getBoard() {
		return new Board(GameRules.SELECTED_GAMERULES, null, players.toArray(new Player[players.size()]));
	}
	
	public BoardGraphics getGraphics(Board board) {
		Image board_image; Image[] pawns;
		switch(board_graphics_setting) {
		case(SIMPLE_CIRCLE_GRAPHICS):
			board_image = BoardGraphics.createBoardImage(board);
			pawns = BoardGraphics.createPawns(board);
			break;
		case(REALISTIC_BOARD_GRAPHICS):
			board_image = BoardGraphics.createRealisticBoardImage(board);
			pawns = BoardGraphics.createShadowedPawns(board);
			break;
		default:
			board_image = BoardGraphics.createBoardImage(board);
			pawns = BoardGraphics.createPawns(board);
			break;
		}
		BoardGraphics graphics = new BoardGraphics(board, board_image, pawns);
		return graphics;
	}
	
	public void addPlayer(Player player_type, String player_name, Color player_color) {
		Player player = player_type.getNewInstance();
		if (player == player_type) throw new AssertionError("Player.getNewInstance of "+player_type+" has to return a new instance (it did not)");
		player.setColor(player_color);
		player.setName(player_name);
		players.add(player);
	}
	
	public void addPlayer(String player_type_name, String player_name, Color player_color) {
		int index = -1;
		for (int i=0; i < player_type_names.size(); i++)
			if (player_type_name.replaceAll(" *", "").toLowerCase().contentEquals(player_type_names.get(i).replaceAll(" *", "").toLowerCase())) {
				index = i; break;
			}
		if (index == -1) {
			new RuntimeException("added player-type does not exist").printStackTrace();
			return;
		}
		Player player_type = player_types.get(index);
		addPlayer(player_type, player_name, player_color);
	}
	
	public boolean hasValidPlayerCount() {
		if (players.size() == 2 || players.size() == 4 || players.size() >= 6) return true;
		return false;
	}
	
	public int playerCount() {
		return players.size();
	}
	
}
