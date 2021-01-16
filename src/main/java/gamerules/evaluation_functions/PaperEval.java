package gamerules.evaluation_functions;

import gamerules.*;
import players.Player;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

public class PaperEval implements EvaluationFunction {

    private final float weightA, weightB, weightC;
//    private final int[][] boardMatrix = {
//            // Layers with an even amount of nodes are not centered:
//            // - layers  2,  4,  6,  8: offset of 1 to the left
//            // - layers 10, 12, 14, 16: offset of 1 to the right
//            {-1 , -1 , -1 , -1 , -1 , -1 , 0  , -1 , -1 , -1 , -1 , -1 , -1},  // Layer 1
//            {-1 , -1 , -1 , -1 , -1 , 1  , 2  , -1 , -1 , -1 , -1 , -1 , -1},  // Layer 2
//            {-1 , -1 , -1 , -1 , -1 , 3  , 4  , 5  , -1 , -1 , -1 , -1 , -1},  // Layer 3
//            {-1 , -1 , -1 , -1 , 6  , 7  , 8  , 9  , -1 , -1 , -1 , -1 , -1},  // Layer 4
//            {10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22},  // Layer 5
//            {23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , -1},  // Layer 6
//            {-1 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , -1},  // Layer 7
//            {-1 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , -1 , -1},  // Layer 8
//            {-1 , -1 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , -1 , -1},  // Layer 9 (horizontal middle)
//            {-1 , -1 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , -1},  // Layer 10
//            {-1 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , -1},  // Layer 11
//            {-1 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97},  // Layer 12
//            {98 , 99 , 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110}, // Layer 13
//            {-1 , -1 , -1 , -1 , -1 , 111, 112, 113, 114, -1 , -1 , -1 , -1},  // Layer 14
//            {-1 , -1 , -1 , -1 , -1 , 115, 116, 117, -1 , -1 , -1 , -1 , -1},  // Layer 15
//            {-1 , -1 , -1 , -1 , -1 , -1 , 118, 119, -1 , -1 , -1 , -1 , -1},  // Layer 16
//            {-1 , -1 , -1 , -1 , -1 , -1 , 120, -1 , -1 , -1 , -1 , -1 , -1},  // Layer 17
//    };
    private final float[][] nodeCoordinates = {
            // nodeCoordinates[i][j]:   - i: number of the node [0, 120]
            //                          - j:    * j = 0: x-coordinate
            //                                  * j = 1: y-coordinate
            {6, 0}   ,                                                                                                                                      // Layer 1
            {5.5f, 1} , {6.5f, 1} ,                                                                                                                           // Layer 2
            {5, 2}   , {6, 2}   , {7, 2}   ,                                                                                                                // Layer 3
            {4.5f, 3} , {5.5f, 3} , {6.5f, 3} , {7.5f, 3} ,                                                                                                     // Layer 4
            {0, 4}   , {1, 4}   , {2, 4}   , {3, 4}   , {4, 4}   , {5, 4}   , {6, 4}   , {7, 4}   , {8, 4}   , {9, 4}   , {10, 4}  , {11, 4}    , {12, 4},  // Layer 5
            {0.5f, 5} , {1.5f, 5} , {2.5f, 5} , {3.5f, 5} , {4.5f, 5} , {5.5f, 5} , {6.5f, 5} , {7.5f, 5} , {8.5f, 5} , {9.5f, 5} , {10.5f, 5}, {11.5f, 5}  ,           // Layer 6
            {1, 6}   , {2, 6}   , {3, 6}   , {4, 6}   , {5, 6}   , {6, 6}   , {7, 6}   , {8, 6}   , {9, 6}   , {10, 6}  , {11, 6}  ,                        // Layer 7
            {1.5f, 7} , {2.5f, 7} , {3.5f, 7} , {4.5f, 7} , {5.5f, 7} , {6.5f, 7} , {7.5f, 7} , {8.5f, 7} , {9.5f, 7} , {10.5f, 7},                                   // Layer 8
            {2, 8}   , {3, 8}   , {4, 8}   , {5, 8}   , {6, 8}   , {7, 8}   , {8, 8}   , {9, 8}   , {10, 8}  ,                                              // Layer 9
            {1.5f, 9} , {2.5f, 9} , {3.5f, 9} , {4.5f, 9} , {5.5f, 9} , {6.5f, 9} , {7.5f, 9} , {8.5f, 9} , {9.5f, 9} , {10.5f, 9},                                   // Layer 10
            {1, 10}  , {2, 10}  , {3, 10}  , {4, 10}  , {5, 10}  , {6, 10}  , {7, 10}  , {8, 10}  , {9, 10}  , {10, 10} , {11, 10}  ,                       // Layer 11
            {0.5f, 11}, {1.5f, 11}, {2.5f, 11}, {3.5f, 11}, {4.5f, 11}, {5.5f, 11}, {6.5f, 11}, {7.5f, 11}, {8.5f, 11}, {9.5f, 11}, {10.5f, 11}, {11.5f, 11},           // Layer 12
            {0, 12}  , {1, 12}  , {2, 12}  , {3, 12}  , {4, 12}  , {5, 12}  , {6, 12}  , {7, 12}  , {8, 12}  , {9, 12}  , {10, 12}  , {11, 12}  , {12, 12}, // Layer 13
            {4.5f, 13}, {5.5f, 13}, {6.5f, 13}, {7.5f, 13},                                                                                                     // Layer 14
            {5, 14}  , {6, 14}  , {7, 14}  ,                                                                                                                // Layer 15
            {5.5f, 15}, {6.5f, 15},                                                                                                                           // Layer 16
            {6, 16}                                                                                                                                         // Layer 17
    };
    private final float[][] lookupTable;
    private final HashMap<BigInteger, Double> scores = new HashMap<>();
    private final int[] furthestGoalNodesPerPlayer = {120, 0, 98, 22, 10, 110};
    private final int[][] verticalLineNodesPerPlayer = {
            {0, 4, 16, 40, 60, 80, 104, 116, 120}, // Player 1 and 2
            {98, 87, 77, 68, 60, 52, 43, 33, 22},  // Player 3 and 4
            {10, 24, 37, 49, 60, 71, 83, 96, 110}  // Player 5 and 6
    };

    public PaperEval() {
//        this(0.911f, 0.140f, 0.388f); // Optimized values for depth=2 from paper = (0.911, 0.140, 0.388)
        this(0.902f, 0.004f, 0.431f); // Optimized values for depth=4 from paper = (0.902, 0.004, 0.431)
    }

    public PaperEval(float weightA, float weightB, float weightC) {
        this.weightA = weightA;
        this.weightB = weightB;
        this.weightC = weightC;
        this.lookupTable = createLookupTable();
    }

    private float[][] createLookupTable() {
        float[][] table = new float[121][121];
        for (int node1 = 0; node1 < table.length; node1++) {
            for (int node2 = 0; node2 < table[0].length; node2++) {
                table[node1][node2] = calculateDistance(node1, node2);
            }
        }
        return table;
    }

    private float calculateDistance(int node1, int node2) {
        // Method: Pythagoras
        float differenceInX = Math.abs(nodeCoordinates[node2][0] - nodeCoordinates[node1][0]);
        float differenceInY = Math.abs(nodeCoordinates[node2][1] - nodeCoordinates[node1][1]);
        return (float) Math.sqrt(Math.pow(differenceInX, 2) + Math.pow(differenceInY, 2));
    }

    private float calculateA(int pawnPosition, int playerID) {
        float distance = lookupTable[pawnPosition][furthestGoalNodesPerPlayer[playerID]];
        return (distance * distance);
    }

    private float calculateB(int pawnPosition, int playerID) {
        float distance = Float.POSITIVE_INFINITY;
        int[] verticalLineNodes;

        if (playerID == 0 | playerID == 1) {
            verticalLineNodes = verticalLineNodesPerPlayer[0];
        } else if (playerID == 2 | playerID == 3) {
            verticalLineNodes = verticalLineNodesPerPlayer[1];
        } else {
            verticalLineNodes = verticalLineNodesPerPlayer[2];
        }

        for (int verticalLineNode : verticalLineNodes) {
            distance = Math.min(distance, lookupTable[pawnPosition][verticalLineNode]);
        }

        return (distance * distance);
    }

    private float calculateC(int pawnPosition, int playerID, List<Move> moves, Board board) {
        Move bestMove = null;
        float bestDistance = Float.POSITIVE_INFINITY;
        int furthestGoalNodeID = furthestGoalNodesPerPlayer[playerID];
        for (Move move : moves) {
            int targetNode = move.getTarget(board).getID();
            float distance = lookupTable[targetNode][furthestGoalNodeID];
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMove = move;
            }
        }

        int targetNode = bestMove.getTarget(board).getID();
        if (lookupTable[pawnPosition][furthestGoalNodeID] < lookupTable[targetNode][furthestGoalNodeID]) {
            return lookupTable[pawnPosition][targetNode] * -1;
        }
        else {
            return lookupTable[pawnPosition][targetNode];
        }
    }

    @Override
    public Double apply(GameState state, Player player) {
        Double V = scores.get(state.gameStateID());

        if (V == null) {
            long startTime = System.nanoTime();

            Board originalBoard = state.getOriginalBoard();
            List<Pawn> playerPawns = state.getAllPawnsOf(player);
            List<Pawn> enemyPlayerPawns = state.getAllPawnsOf(state.getEnemy(player));
            int playerID = originalBoard.getPlayerIndex(playerPawns.get(0).getOwner());
            int enemyPlayerID = originalBoard.getPlayerIndex(enemyPlayerPawns.get(0).getOwner());

            float[] A = new float[2];
            float[] B = new float[2];
            float[] C = new float[2];

            float originalC0 = 0;
            float originalC1 = 0;

            // Player 1
            for (Pawn pawn : playerPawns) {
                int pawnPosition = pawn.getPosition().getID();

                A[0] += calculateA(pawnPosition, playerID);
                B[0] += calculateB(pawnPosition, playerID);

                List<Move> moves = state.getAllPossibleMoves(pawn);
                if (!moves.isEmpty()) {
                    C[0] += calculateC(pawnPosition, playerID, moves, originalBoard);

                    float maxVerticalAdvance = Float.NEGATIVE_INFINITY;
                    for (Move move : moves) {
                        int target = move.getTarget(originalBoard).getID();
                        maxVerticalAdvance = Math.max(maxVerticalAdvance, nodeCoordinates[target][1] - nodeCoordinates[pawnPosition][1]);
                    }
                    originalC0 += maxVerticalAdvance;
                }
            }

            // Player 2
            for (Pawn pawn : enemyPlayerPawns) {
                int pawnPosition = pawn.getPosition().getID();

                A[1] += calculateA(pawnPosition, enemyPlayerID);
                B[1] += calculateB(pawnPosition, enemyPlayerID);

                List<Move> moves = state.getAllPossibleMoves(pawn);
                if (!moves.isEmpty()) {
                    C[1] += calculateC(pawnPosition, enemyPlayerID, moves, originalBoard);

                    float maxVerticalAdvance = Float.POSITIVE_INFINITY;
                    for (Move move : moves) {
                        int target = move.getTarget(originalBoard).getID();
                        maxVerticalAdvance = Math.min(maxVerticalAdvance, nodeCoordinates[target][1] - nodeCoordinates[pawnPosition][1]);
                    }
                    originalC1 -= maxVerticalAdvance;
                }
            }

            // Compute V
            V = (double) weightA * (A[1] - A[0]) + weightB * (B[1] - B[0]) + weightC * (C[0] - C[1]);

            long endTime = System.nanoTime();
//            System.out.println("A[0]: " + A[0] + ", A[1]: " + A[1]);
//            System.out.println("B[0]: " + B[0] + ", B[1]: " + B[1]);
//            System.out.println("C[0]: " + C[0] + ", C[1]: " + C[1]);
//            System.out.println("C0: " + originalC0 + ", C1: " + originalC1);
//            System.out.println(endTime - startTime);
            scores.put(state.gameStateID(), V);
        }

        return V;
    }
    
    public String toString() {
    	return "PaperEval";
    }

}