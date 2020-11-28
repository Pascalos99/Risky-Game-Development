package players.bots.utils;

import gamerules.GameTreeNode;

public class ValueNode {

    private double value;
    private GameTreeNode gameTreeNode;

    public ValueNode(double value, GameTreeNode gameTreeNode) {
        this.value = value;
        this.gameTreeNode = gameTreeNode;
    }

    public double getValue() {
        return value;
    }

    public GameTreeNode getGameTreeNode() {
        return gameTreeNode;
    }

    public static ValueNode max(ValueNode node1, ValueNode node2) {
        if (node1.getValue() >= node2.getValue()) {
            return node1;
        }
        return node2;
    }

    public static ValueNode min(ValueNode node1, ValueNode node2) {
        if (node1.getValue() < node2.getValue()) {
            return node1;
        }
        return node2;
    }
}
