package gamerules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import players.Player;

public abstract class GameTree {

    private final GameTreeNode root;
    private GameTreeNode current;
    private List<List<GameTreeNode>> all_layers;
    private EvaluationFunction evaluation;

    public GameTree(GameState root) {
        this.root = new GameTreeNode(null, root);
        current = this.root;
        all_layers = new ArrayList<>();
        all_layers.add(new ArrayList<>());
        all_layers.get(0).add(this.root);
        evaluation = EvaluationFunction.SIMPLE_GOAL_DISTANCE;
    }

    public void setEvaluation(EvaluationFunction function) {
    	evaluation = function;
    }
    
    public EvaluationFunction getEvaluation() {
    	return evaluation;
    }
    
    /**
     * @see EvaluationFunction#eval(GameState)
     */
    public double evaluate(GameState state) {
    	return evaluation.eval(state);
    }
    /**
     * @see EvaluationFunction#eval(GameState, Player)
     */
    public double evaluate(GameState state, Player player) {
    	return evaluation.eval(state, player);
    }
    
    /**
     * @param state The GameState for which to compute all moves
     * @return returns a list of all moves executable by the current player in the given GameState
     */
    public List<Move> getAllPossibleMoves(GameState state) {
    	return state.getAllPossibleMoves(state.getAllPawnsOf(state.currentPlayer()));
    }
    
    /**
     * @param state The GameState for which to compute all moves
     * @param post_filter a filter that assigns a boolean value to each of the pawns in the GameState; essentially pruning
     *  which pawns are allowed to add the resulting GameStates from their moves to the GameTree
     * @return returns a list of all moves executable by the current player in the given GameState filtered by pawns that
     *  satisfy the filter
     */
    public List<Move> getAllPossibleMoves(GameState state, Function<Pawn, Boolean> post_filter) {
    	return state.getAllPossibleMoves(state.getAllPawnsOf(state.currentPlayer()).stream().
    			filter(p -> post_filter.apply(p)).collect(Collectors.toList()));
    }
    
    /**
     * expand all nodes at the deepest depth that satisfy the filter
     */
    public void expand(Function<GameState, Boolean> filter) {
    	// TODO
    }
    
    /**
     * expand all nodes at all depths that satisfy the filter
     */
    public void expandAll(Function<GameState, Boolean> filter) {
    	// TODO
    }
    
    /**
     * expand all nodes at the deepest depth
     */
    public void expand() {
    	expand(s -> true);
    }
    
    /**
     * expand all nodes at all depths
     */
    public void expandAll() {
    	expandAll(s -> true);
    }
    
    /**
     * expand a single node already contained in the tree at all pawns that satisfy the filter<br><br>
     * Assumes the given state is contained in this GameTree
     */
    private void expand(GameTreeNode node, Function<Pawn, Boolean> post_filter) {
    	List<GameTreeNode> to_add = getAllPossibleMoves(node.getGameState(), post_filter).stream().map
				(m -> node.getStateAfterMove(m)).collect(Collectors.toList());
    	addChildren(to_add);
    }
    
    /**
     * expand a single node already contained in the tree at all pawns<br><br>
     * Assumes the given state is contained in this GameTree
     */
    private void expand(GameTreeNode node) {
    	List<GameTreeNode> to_add = getAllPossibleMoves(node.getGameState()).stream().map
    			(m -> node.getStateAfterMove(m)).collect(Collectors.toList());
    	addChildren(to_add);
    }
    
    private void addChild(GameTreeNode node) {
    	if (node.getParent() == null) {
    		throw new RuntimeException("can't add child to GameTree which does not have a parent");
    	}
    	node.getParent().children.add(node);
    	int depth = node.getGameState().getDepth();
    	while (maxDepth() < depth) all_layers.add(new ArrayList<>());
    	all_layers.get(depth).add(node);
    }
    
    private void addChildren(List<GameTreeNode> nodes) {
    	for (GameTreeNode node : nodes) addChild(node);
    }
    
    public int maxDepth() {
    	return all_layers.size() - 1;
    }
    
    /*
    private void build() {
        for (Move move : current.getAllPossibleMoves(current.getAllPawnsOf(current.currentPlayer()))) {

        }
    }
    */

}
