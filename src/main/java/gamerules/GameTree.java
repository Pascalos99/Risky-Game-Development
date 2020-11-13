package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import players.Player;

public class GameTree {

    private final GameTreeNode root;
    private List<List<GameTreeNode>> all_layers;
    private EvaluationFunction evaluation;
    private boolean pruneCopyStates = false;

    public static final Function<GameState, Boolean> PRE_ALLOW_ALL = state -> true;
    public static final Function<Pawn, Boolean> POST_ALLOW_ALL = pawn -> true;

    public GameTree(GameState root) {
        this.root = new GameTreeNode(null, root);
        all_layers = new ArrayList<>();
        all_layers.add(new ArrayList<>());
        all_layers.get(0).add(this.root);
        evaluation = EvaluationFunction.SIMPLE_GOAL_DISTANCE;
    }
    
    public void setCopyPruning(boolean set) {
    	pruneCopyStates = set;
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
     * @see EvaluationFunction#eval(GameState)
     */
    public double evaluate(GameTreeNode node) {
    	return evaluate(node.getGameState());
    }
    
    /**
     * @see EvaluationFunction#eval(GameState, Player)
     */
    public double evaluate(GameTreeNode node, Player player) {
    	return evaluate(node.getGameState(), player);
    }
    
    /**
     * @return a list containing all nodes from all depths in this GameTree
     */
    public List<GameTreeNode> getAllNodes() {
    	ArrayList<GameTreeNode> result = new ArrayList<>();
    	for (List<GameTreeNode> layer : all_layers) result.addAll(layer);
    	return result;
    }
    
    /**
     * @return a list of lists of nodes ordered by depth, starting at depth 0 being the root
     */
    public List<List<GameTreeNode>> getAllLayers() {
    	List<List<GameTreeNode>> result = new ArrayList<>();
    	for (List<GameTreeNode> layer : all_layers) result.add(Collections.unmodifiableList(layer));
    	return result;
    }
    
    /**
     * @param depth
     * @return a list of all nodes at a given depth in this GameTree, or {@code null} if the given depth is an invalid index
     */
    public List<GameTreeNode> getNodesAtDepth(int depth) {
    	if (!isValidDepth(depth)) return null;
    	return Collections.unmodifiableList(all_layers.get(depth));
    }
    
    public GameTreeNode getRoot() {
    	return root;
    }
    
    public boolean containsNode(GameTreeNode node) {
    	for (List<GameTreeNode> layer : all_layers)
    		if (layer.contains(node)) return true;
    	return false;
    }
    
    /**
     * Creates a new GameTree object that is a copy of this one starting at the given root. <br>
     * The GameStates stored in this object will be deep-copied, even if the selected newRoot is equal to this root. <br>
     * Nodes from the sub-tree are not interchangable with nodes from the original tree, nor are their GameStates, although the
     * {@linkplain GameState#contentEquals(Object)} would be true for matching elements. <br>
     * There is no reflection of changes in either direction: changes in this GameTree do not affect the sub-tree and
     *  changes in the sub-tree do not change this GameTree
     * @param newRoot
     * @return {@code null} if the given new root is not an element of this GameTree
     */
    public GameTree createSubTree(GameTreeNode newRoot) {
    	if (!containsNode(newRoot)) return null;
    	GameState copyRoot = new GameState(newRoot.getGameState().getPrevious(), newRoot.getGameState().lastMove());
    	copyRoot.backupBoard();
    	GameTree subTree = new GameTree(copyRoot);
    	subTree.root.isExpanded = root.isExpanded;
    	addChildrenToSubTree(root, subTree.root, subTree);
    	subTree.pruneCopyStates = pruneCopyStates;
    	return subTree;
    }
    
    private void addChildrenToSubTree(GameTreeNode node, GameTreeNode corresponding_node, GameTree subTree) {
    	for (GameTreeNode child : node.children) {
    		GameTreeNode corresponding_child = corresponding_node.getStateAfterMove(child.getGameState().lastMove());
    		subTree.addChild(corresponding_child, child.isExpanded);
    		addChildrenToSubTree(child, corresponding_child, subTree);
    	}
    }
    
    /**
     * @param eval
     * @param player
     * @param from_depth inclusive
     * @param to_depth exclusive
     * @return the node with the highest evaluation score relative to the given player
     */
    public GameTreeNode getHighestEval(EvaluationFunction eval, Player player, int from_depth, int to_depth) {
    	double highest = Double.NEGATIVE_INFINITY;
    	GameTreeNode best = null;
    	for (int i = from_depth; i < to_depth && i <= maxDepth(); i++) {
    		List<GameTreeNode> layer = all_layers.get(i);
    		for (GameTreeNode node : layer) {
    			double val = eval.eval(node.getGameState(), player);
    			if (val > highest) {
    				best = node;
    				highest = val;
    			}
    		}
    	}
    	return best;
    }
    
    public GameTreeNode getHighestEval(EvaluationFunction eval, Player player) {
    	return getHighestEval(eval, player, 0, maxDepth() + 1);
    }
    
    public GameTreeNode getHighestEval(Player player) {
    	return getHighestEval(evaluation, player);
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
     * @param state The GameState for which to compute all moves
     * @return returns a list of all moves executable by the current player in the given GameState
     */
    public List<Move> getAllPossibleMoves(GameState state) {
    	return state.getAllPossibleMoves(state.getAllPawnsOf(state.currentPlayer()));
    }

    /**
     * expand all nodes at a given depth that satisfy the given pre-filter and add all nodes that satisfy the given post-filter
     * @param depth
     * @param pre_filter a filter that limits which nodes will be expanded based on the returnvalue of the pre_filter
     * @param post_filter a filter that limits which pawns will have their moves calculated and resulting GameState's added
     *  to the GameTree
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expand(int depth, Function<GameState, Boolean> pre_filter, Function<Pawn, Boolean> post_filter) {
        List<GameTreeNode> nodes = all_layers.get(depth).stream().filter(p -> pre_filter.apply(p.getGameState())).collect(Collectors.toList());

        List<GameTreeNode> childNodes = new ArrayList<>();
        // for all nodes in depth: if pre_filter.apply ands node is not expanded then expand
        for (GameTreeNode node : nodes) {
            if (!node.isExpanded) childNodes.addAll(expand(node, post_filter));
        }

    	return childNodes;
    }
    
    /**
     * expand all nodes at a given depth that satisfy the given filter
     * @param depth
     * @param pre_filter a filter that limits which nodes will be expanded based on the returnvalue of the pre_filter
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expand(int depth, Function<GameState, Boolean> pre_filter) {
    	return expand(depth, pre_filter, POST_ALLOW_ALL);
    }
    
    /**
     * expand all nodes at a given depth
     * @param depth
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expand(int depth) {
    	return expand(depth, PRE_ALLOW_ALL);
    }

    /**
     * expand all nodes at all depths that satisfy the filters
     * @see #expand(int, Function, Function)
     */
    public List<GameTreeNode> expandAll(Function<GameState, Boolean> pre_filter, Function<Pawn, Boolean> post_filter) {
    	List<GameTreeNode> all_added = new ArrayList<>();
    	for (int d = maxDepth(); d >= 0; d--)
    		all_added.addAll(expand(d, pre_filter, post_filter));
    	return all_added;
    }
    
    /**
     * expand all nodes at all depths that satisfy the filter
     * @see #expand(int, Function)
     */
    public List<GameTreeNode> expandAll(Function<GameState, Boolean> pre_filter) {
    	return expandAll(pre_filter, POST_ALLOW_ALL);
    }
    
    /**
     * expand all nodes at all depths
     * @see #expand(int)
     */
    public List<GameTreeNode> expandAll() {
    	return expandAll(PRE_ALLOW_ALL);
    }
    
    /**
     * expand all nodes at the deepest depth that satisfy the filters
     * @param pre_filter a filter that limits which nodes will be expanded based on the returnvalue of the pre_filter
     * @param post_filter a filter that limits which pawns will have their moves calculated and resulting GameState's added
     *  to the GameTree
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expandDeepest(Function<GameState, Boolean> pre_filter, Function<Pawn, Boolean> post_filter) {
    	return expand(maxDepth(), pre_filter, post_filter);
    }
    
    /**
     * expand all nodes at the deepest depth that satisfy the filter
     * @param pre_filter a filter that limits which nodes will be expanded based on the returnvalue of the pre_filter
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expandDeepest(Function<GameState, Boolean> pre_filter) {
    	return expand(maxDepth(), pre_filter);
    }
    
    /**
     * expand all nodes at the deepest depth
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expandDeepest() {
    	return expandDeepest(PRE_ALLOW_ALL);
    }

    /**
     * expand a single node already contained in the tree at all pawns that satisfy the filter<br><br>
     * Assumes the given state is contained in this GameTree
     * @return {@code null} if the given node has already been expanded
     */
    public List<GameTreeNode> expand(GameTreeNode node, Function<Pawn, Boolean> post_filter) {
    	if (node.isExpanded) return null;
    	List<GameTreeNode> to_add = getAllPossibleMoves(node.getGameState(), post_filter).stream().map
				(m -> node.getStateAfterMove(m)).collect(Collectors.toList());
    	boolean expanded = post_filter == POST_ALLOW_ALL || post_filter.apply(null);
    	addChildren(to_add, expanded);
    	return to_add;
    }

    /**
     * expand a single node already contained in the tree at all pawns<br><br>
     * Assumes the given state is contained in this GameTree
     */
    public List<GameTreeNode> expand(GameTreeNode node) {
    	List<GameTreeNode> to_add = getAllPossibleMoves(node.getGameState()).stream().map
    			(m -> node.getStateAfterMove(m)).collect(Collectors.toList());
    	addChildren(to_add, true);
    	return to_add;
    }

    private void addChild(GameTreeNode node, boolean setExpanded) {
    	if (node.getParent() == null) {
    		throw new RuntimeException("can't add child to GameTree which does not have a parent");
    	}
    	
    	// if we already have it in the tree, we won't add it (if copy pruning is activated)
    	if (pruneCopyStates && existsInTree(node.getGameState())) return;
    	
    	node.addToParent();
    	int depth = node.getGameState().getDepth();
    	while (maxDepth() < depth) all_layers.add(new ArrayList<>());
    	all_layers.get(depth).add(node);
    }

    private void addChildren(List<GameTreeNode> nodes, boolean setExpanded) {
    	for (GameTreeNode node : nodes) addChild(node, setExpanded);
    }
    
    public boolean existsInTree(GameState state) {
    	int depth = state.getDepth();
    	int[] intervals = {
    			depth - 1	, depth + 2,
    			depth + 2	, maxDepth() + 1, 
    			0			, depth - 1  };
    	
    	for (int i = 0; i < intervals.length; i+= 2)
    		for (int d = intervals[i]; d < intervals[i+1]; d++)
    			if (isValidDepth(d))
        			for (GameTreeNode node : all_layers.get(d))
        				if (node.getGameState().contentEquals(state)) return true;
    	return false;
    }

    public boolean isValidDepth(int depth) {
    	return depth >= 0 && depth <= maxDepth();
    }
    
    /**
     * gives the maximum depth that is valid in this GameTree [NOTE that looping through all depths would include a <= check on maxDepth()]
     * @return all_layers.size() - 1
     */
    public int maxDepth() {
    	return all_layers.size() - 1;
    }

}
