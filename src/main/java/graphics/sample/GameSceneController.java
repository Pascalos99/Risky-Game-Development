package graphics.sample;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import players.HumanPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import game.GamePanel;

public class GameSceneController implements Initializable{

    @FXML
    public SwingNode swingNode;
    
    @FXML
    private Pane pane;
    
    private GamePanel gamePanel;

    @FXML
    private void Exit(ActionEvent event) throws IOException {
        System.exit(0);
    }
    @FXML
    private void PlayAgain(ActionEvent event) throws IOException {
        Controller.gameStage.close();
        Main.mainStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    	gamePanel = Controller.gamePanel;
        gamePanel.setSize(400,400);
        JFXPanel panel = new JFXPanel();
        panel.setSize(400,400);
        panel.add(gamePanel);
        swingNode.setContent(panel);
        pane.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
        	HumanPlayer.GLOBAL_INPUT.setSelectedNode(null);
        	gamePanel.repaint();
        });
    }
}
