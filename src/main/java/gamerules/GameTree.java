package gamerules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import players.Player;

public class GameTree {

    private final GameTreeNode root;
    private List<List<GameTreeNode>> all_layers;
    private EvaluationFunction evaluation;
    private boolean pruneCopyStates = false;

    public static final Predicate<GameTreeNode> PRE_ALLOW_ALL = node -> true;
    public static final Predicate<Pawn> POST_ALLOW_ALL = pawn -> true;
    
    private long max_expansion_time = Long.MAX_VALUE;
    private long expansion_time = 0;
    
    /**
     * Set the maximum time the GameTree is allowed to use when expanding; the tree will stop when this limit has been reached.
     * At this point, the call {@linkplain #limitReached()} will return {@code true} and the search will halt.<br>
     * To make the search continue, you need to call {@linkplain #clearTimeUsed()} and restart the search the same way it was started.
     * Nodes that have already been visited will not be visited again, but the search will start over from scratch for unvisited nodes.
     */
    public void setMaxExpansionTime(long max_expansion_time_ms) {
    	max_expansion_time = max_expansion_time_ms;
    }
    
    /**
     * @return The amount of time (in milliseconds)
     *  this GameTree has used to expand nodes since the last {@linkplain #clearTimeUsed()} call.
     */
    public long getExpansionTime() {
    	return expansion_time;
    }
    
    public boolean limitReached() {
    	return expansion_time >= max_expansion_time;
    }
    
    /**
     * Set the timer for counting whether too much time has been used for search back to 0.
     */
    public void clearTimeUsed() {
    	expansion_time = 0;
    }

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
     * Gives a pre-filter that filters based on the evaluation function result of the GameState being evaluated.<br>
     * Only lets through GameStates that get an evaluation value that is above or below the top x-percent of
     *  all GameStates from the same parent.
     * @param percent the percentage barrier from which the GameState population is cut
     * @param maximize if this is set to {@code true}, the filter will let through GameState's with evaluation ABOVE the 
     *  percent-point;<br>if this is set to {@code false}, the filter will let through GameState's with evaluation BELOW the 
     *  percent-point.
     * @param player the Player who's evaluation score needs to be filtered. {@code null} if this needs to always be the 
     *  currentPlayer() in the given GameState
     * @return A predicate that does this
     */
    public Predicate<GameTreeNode> preFilterBarrierEval(double percent, boolean maximize, Player player) {
    	final double percentage;
    	if (percent < 0) percentage = 0;
    	else if (percent > 1) percentage = 1;
    	else percentage = percent;
    	
    	GameTree tree = this;
		Map<GameTreeNode, Double> percentPoints = new HashMap<>();
		
		Predicate<GameTreeNode> result = new Predicate<>() {
			
			private double evaluate(GameTreeNode node) {
				if (player == null) return tree.evaluate(node);
				return tree.evaluate(node, player);
			}
			
			@Override
			public boolean test(GameTreeNode node) {
				
				if (percentage == 0) return !maximize;
				if (percentage == 1) return maximize;
				
				if (node.getParent() == null) return true;
				GameTreeNode parent = node.getParent();
				if (!percentPoints.containsKey(parent)) {
					
					int num_children = parent.children.size();
					List<Double> values = new ArrayList<>(num_children);
					
					for (GameTreeNode child : parent.children) {
						values.add(evaluate(child));
					}
					int index = (int)((1 - percentage) * num_children);
					Collections.sort(values);
					percentPoints.put(parent, values.get(index));
				}
				if (maximize)
					return evaluate(node) >= percentPoints.get(parent);
				return evaluate(node) <= percentPoints.get(parent);
			}
    	};
    	return result;
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
    public List<Move> getAllPossibleMoves(GameState state, Predicate<Pawn> post_filter) {
    	return state.getAllPossibleMoves(state.getAllPawnsOf(state.currentPlayer()).stream().
    			filter(post_filter).collect(Collectors.toList()));
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
    public List<GameTreeNode> expand(int depth, Predicate<GameTreeNode> pre_filter, Predicate<Pawn> post_filter) {
    	List<GameTreeNode> nodes = all_layers.get(depth).stream().filter(pre_filter).collect(Collectors.toList());

        List<GameTreeNode> childNodes = new ArrayList<>();
        // for all nodes in depth: if pre_filter.apply ands node is not expanded then expand
        long last = System.currentTimeMillis();
        for (GameTreeNode node : nodes) {
        	expansion_time += System.currentTimeMillis() - last;
        	last = System.currentTimeMillis();
        	if (expansion_time > max_expansion_time) return childNodes;
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
    public List<GameTreeNode> expand(int depth, Predicate<GameTreeNode> pre_filter) {
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
     * @see #expand(int, Predicate, Predicate)
     */
    public List<GameTreeNode> expandAll(Predicate<GameTreeNode> pre_filter, Predicate<Pawn> post_filter) {
    	List<GameTreeNode> all_added = new ArrayList<>();
    	for (int d = maxDepth(); d >= 0; d--)
    		all_added.addAll(expand(d, pre_filter, post_filter));
    	return all_added;
    }
    
    /**
     * expand all nodes at all depths that satisfy the filter
     * @see #expand(int, Predicate)
     */
    public List<GameTreeNode> expandAll(Predicate<GameTreeNode> pre_filter) {
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
    public List<GameTreeNode> expandDeepest(Predicate<GameTreeNode> pre_filter, Predicate<Pawn> post_filter) {
    	return expand(maxDepth(), pre_filter, post_filter);
    }
    
    /**
     * expand all nodes at the deepest depth that satisfy the filter
     * @param pre_filter a filter that limits which nodes will be expanded based on the returnvalue of the pre_filter
     * @return the list of nodes that were added to the GameTree
     */
    public List<GameTreeNode> expandDeepest(Predicate<GameTreeNode> pre_filter) {
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
    public List<GameTreeNode> expand(GameTreeNode node, Predicate<Pawn> post_filter) {
    	if (node.isExpanded) return null;
    	List<GameTreeNode> to_add = getAllPossibleMoves(node.getGameState(), post_filter).stream().map
				(m -> node.getStateAfterMove(m)).collect(Collectors.toList());
    	boolean expanded = post_filter == POST_ALLOW_ALL || post_filter.test(null);
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