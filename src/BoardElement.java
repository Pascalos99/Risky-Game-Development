import javax.swing.*;
import java.awt.*;

public class BoardElement extends JComponent {
    public int index;
    public BoardElement(){
        super();
    }
    public BoardElement(int index){
        this();
        this.index = index;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(151, 93, 26));
        Graphics2D g2 = (Graphics2D) g;
        g2.fillOval(0,0,40,40);
    }
}