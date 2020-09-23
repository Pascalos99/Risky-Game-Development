package com.company;

import javax.swing.*;
import java.util.HashMap;

public class Board extends JFrame {
    public Board(){
        this.setSize(1280,720);
        this.setBounds(0,0,1280,720);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.instantiateBoard();
        this.setVisible(true);
    }
    private int playersCount = 6;
    private int setsUsed = 1;
    private BoardElement[] boardElement;
    private GamePiece[][] players;
    private HashMap<Integer, int[]> neighbours;
    private void instantiateBoard(){
        JLayeredPane pane = new JLayeredPane();
        pane.setSize(1280, 720);
        // Generating starting playing sets
        if(playersCount == 2){
            if(setsUsed==1) {
                players = new GamePiece[2][15];
            }
            if(setsUsed==2) {
                players = new GamePiece[4][10];
            }
            if(setsUsed==3){
                players = new GamePiece[6][10];
            }
        }
        else if(playersCount == 3 && setsUsed == 2){
            players = new GamePiece[6][10];
        }
        else{
            players = new GamePiece[playersCount][10];
        }

        // Placing board elements (squares?)
        boardElement = new BoardElement[121];
        for(int i=0;i<boardElement.length;i++){
            boardElement[i] = new BoardElement(i);
        }
        for(int i=10;i<=22;i++){
            boardElement[i].setBounds((i-5)*50,150,40,40);
        }

        for(int i=23;i<=34;i++){
            boardElement[i].setBounds((i-18)*50+25,185,40,40);
        }

        for(int i=35; i<=45; i++){
            boardElement[i].setBounds((i-29)*50,220,40,40);
        }

        for(int i=46; i<=55; i++){
            boardElement[i].setBounds((i-40)*50+25,255,40,40);
        }

        for(int i=56; i<=64; i++){
            boardElement[i].setBounds((i-49)*50, 290, 40, 40);
        }

        for(int i=65; i<=74; i++){
            boardElement[i].setBounds((i-59)*50+25, 325, 40, 40);
        }

        for(int i=75; i<=85; i++){
            boardElement[i].setBounds((i-69)*50, 360, 40, 40);
        }

        for(int i=86; i<=97; i++){
            boardElement[i].setBounds((i-81)*50+25, 395, 40, 40);
        }

        for(int i=98; i<=110; i++){
            boardElement[i].setBounds((i-93)*50, 430, 40, 40);
        }

        for(int i=111; i<=114; i++){
            boardElement[i].setBounds((i-111)*50 + boardElement[102].getX() + 25, 465, 40, 40);
        }

        for(int i=115; i<=117; i++){
            boardElement[i].setBounds((i-115)*50 + boardElement[111].getX() + 25, 500, 40, 40);
        }

        boardElement[118].setBounds(boardElement[115].getX()+25, 535, 40, 40);
        boardElement[119].setBounds(boardElement[118].getX()+50, 535, 40, 40);
        boardElement[120].setBounds(boardElement[118].getX()+25, 570, 40, 40);

        for(int i=6; i<=9; i++){
            boardElement[i].setBounds(boardElement[14].getX()+25+(i-6)*50, 115, 40, 40);
        }

        for(int i=3; i<=5; i++){
            boardElement[i].setBounds(boardElement[6].getX()+25+(i-3)*50, 80, 40, 40);
        }
        boardElement[1].setBounds(boardElement[3].getX()+25, 45, 40, 40);
        boardElement[2].setBounds(boardElement[1].getX()+50, 45, 40, 40);
        boardElement[0].setBounds(boardElement[1].getX()+25, 10, 40, 40);

        for(int i=0;i<=9;i++){
            players[0][i] = new GamePiece(1, boardElement[i]);
        }

        for(int i=10, j=19, k=98, l=107, m=111; i<=13; i++, j++, k++, l++, m++){
            players[1][i-10] = new GamePiece(2, boardElement[i]);
            if(players.length>2)
                players[2][j-19] = new GamePiece(3, boardElement[j]);
            if(players.length>3)
                players[3][k-98] = new GamePiece(4, boardElement[k]);
            if(players.length>4)
                players[4][l-107] = new GamePiece(5, boardElement[l]);
            if(players.length==6)
                players[5][m-111] = new GamePiece(6, boardElement[m]);
        }

        for(int i=23, j=32, k=86, l=95, m=115; i<=25; i++, j++, k++, l++, m++){
            players[1][i-19] = new GamePiece(2, boardElement[i]);
            if(players.length>2)
                players[2][j-28] = new GamePiece(3, boardElement[j]);
            if(players.length>3)
                players[3][k-82] = new GamePiece(4, boardElement[k]);
            if(players.length>4)
                players[4][l-91] = new GamePiece(5, boardElement[l]);
            if(players.length==6)
                players[5][m-111] = new GamePiece(6, boardElement[m]);
        }

        for(int i=35, j=44, k=75, l=84, m=118; i<=36; i++, j++, k++, l++, m++){
            players[1][i-28] = new GamePiece(2, boardElement[i]);
            if(players.length>2)
                players[2][j-37] = new GamePiece(3, boardElement[j]);
            if(players.length>3)
                players[3][k-68] = new GamePiece(4, boardElement[k]);
            if(players.length>4)
                players[4][l-77] = new GamePiece(5, boardElement[l]);
            if(players.length==6)
                players[5][m-111] = new GamePiece(6, boardElement[m]);
        }

        players[1][players[1].length-1] = new GamePiece(2, boardElement[46]);
        if(players.length>2)
            players[2][9] = new GamePiece(3, boardElement[55]);
        if(players.length>3)
            players[3][9] = new GamePiece(4, boardElement[65]);
        if(players.length>4)
            players[4][9] = new GamePiece(5, boardElement[74]);
        if(players.length==6)
            players[5][9] = new GamePiece(6, boardElement[120]);

        for (BoardElement element : boardElement)
            pane.add(element, Integer.valueOf(1));

        for (GamePiece[] player : players)
            for (GamePiece piece : player)
                pane.add(piece, Integer.valueOf(2));
        this.add(pane);
        // Making an adjacency map for the game field
        neighbours = new HashMap<>();
        neighbours.put(0, new int[]{1,2});
        neighbours.put(1, new int[]{0,2,3,4});
        neighbours.put(2, new int[]{0,1,4,5});
        neighbours.put(3, new int[]{1,4,6,7});
        neighbours.put(4, new int[]{1,2,3,5,7,8});
        neighbours.put(5, new int[]{1,4,6,7});
        neighbours.put(6, new int[]{3,7,14,15});
        neighbours.put(7, new int[]{3,4,6,8,15,16});
        neighbours.put(8, new int[]{4,5,7,9,16,17});
        neighbours.put(9, new int[]{5,8,17,18});
        neighbours.put(10, new int[]{11,23});
        neighbours.put(11, new int[]{10,12,23,24});
        neighbours.put(12, new int[]{11,13,24,25});
        neighbours.put(13, new int[]{12,14,25,26});
        neighbours.put(14, new int[]{6,13,15,26,27});
        neighbours.put(15, new int[]{6,7,14,16,27,28});
        neighbours.put(16, new int[]{7,8,15,17,28,29});
        neighbours.put(17, new int[]{8,9,16,18,29,30});
        neighbours.put(18, new int[]{9, 17, 19, 30, 31});
        neighbours.put(19, new int[]{18, 20, 31, 32});
        neighbours.put(20, new int[]{19,21,32,33});
        neighbours.put(21, new int[]{20,22,33,34});
        neighbours.put(22, new int[]{21,34});
        neighbours.put(23, new int[]{10,11,24,35});

        for(int i=24;i<=33;i++){
            neighbours.put(i, new int[]{i-13,i-12,i-1,i+1,i+11,i+12});
        }

        neighbours.put(34, new int[]{21,22,33,45});
        neighbours.put(35, new int[]{23,24,36,46});

        for(int i=36;i<=44;i++){
            neighbours.put(i, new int[]{i-12,i-11,i-1,i+1,i+10,i+11});
        }

        neighbours.put(45, new int[]{33,34,44,55});
        neighbours.put(46, new int[]{35,36,47,56});

        for(int i=47;i<=54;i++){
            neighbours.put(i, new int[]{i-11,i-10,i-1,i+1,i+10,i+11});
        }

        neighbours.put(55, new int[]{44,45,54,64});
        neighbours.put(56, new int[]{46,47,57,65,66});

        for(int i=57;i<=63;i++){
            neighbours.put(i, new int[]{i-10,i-9,i-1,i+1,i+9,i+10});
        }

        neighbours.put(64, new int[]{54,55,63,73,74});
        neighbours.put(65, new int[]{56,66,75,76});

        for(int i=66;i<=73;i++){
            neighbours.put(i, new int[]{i,i-10,i-9,i-1,i+1,i+10,i+11});
        }

        neighbours.put(74, new int[]{64,73,84,85});
        neighbours.put(75, new int[]{65,76,86,87});

        for(int i=76;i<=84;i++){
            neighbours.put(i, new int[]{i-11,i-10,i-1,i+1,i+11,i+12});
        }

        neighbours.put(85, new int[]{74,84,96,97});
        neighbours.put(86, new int[]{75,87,98,99});

        for(int i=87;i<=96;i++){
            neighbours.put(i, new int[]{i-12,i-11,i-1,i+1,i+12,i+13});
        }

        neighbours.put(97, new int[]{85, 96, 109, 110});
        neighbours.put(98, new int[]{86,99});
        neighbours.put(99, new int[]{86,87,98,100});
        neighbours.put(100, new int[]{87,88,99,101});
        neighbours.put(101, new int[]{88,89,100,102});
        neighbours.put(102, new int[]{89,90,101,103,111});
        neighbours.put(103, new int[]{90,91,102,104,111,112});
        neighbours.put(104, new int[]{91,92,103,105,112,113});
        neighbours.put(105, new int[]{92,93,104,106,113,114});
        neighbours.put(106, new int[]{93,94,105,107,114});
        neighbours.put(107, new int[]{94,95,106,108});
        neighbours.put(108, new int[]{95,96,107,109});
        neighbours.put(109, new int[]{96,97,108,110});
        neighbours.put(110, new int[]{96,109});
        neighbours.put(111, new int[]{102,103,112,115});
        neighbours.put(112, new int[]{103,104,111,113,115,116});
        neighbours.put(113, new int[]{104,105,112,114,116,117});
        neighbours.put(114, new int[]{105,106,113,117});
        neighbours.put(115, new int[]{111,112,116,118});
        neighbours.put(116, new int[]{112,113,115,117,118,119});
        neighbours.put(117, new int[]{113,114,116,119});
        neighbours.put(118, new int[]{115,116,119,120});
        neighbours.put(119, new int[]{116,117,118,120});
        neighbours.put(120, new int[]{118,119});
    }
}
