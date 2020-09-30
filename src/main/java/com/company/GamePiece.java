package com.company;

import javax.swing.*;
import java.awt.*;

@Deprecated
public class GamePiece extends JComponent {
    private Color pieceColor;
    private BoardElement location;
    public GamePiece(int pieceColor, BoardElement location){
        this.pieceColor = getPieceColor(pieceColor);
        this.location = location;
        this.setBounds(this.location.getX()+5,this.location.getY()+5,30,30);
    }

    Color getPieceColor(int colorID){
        return switch (colorID) {
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.YELLOW;
            case 4 -> Color.MAGENTA;
            case 5 -> Color.ORANGE;
            case 6 -> Color.RED;
            default -> Color.BLACK;
        };

    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.pieceColor);
        g.fillOval(0,0,30,30);
    }
}
