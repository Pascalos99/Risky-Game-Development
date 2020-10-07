package graphics.sample;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameSceneController implements Initializable{

    @FXML
    public Pane GamePane;

    public static Pane GloabalGamePane;

    public static SwingNode swingswing;

    @FXML
    public SwingNode swingNode;

    @FXML
    private void Exit(ActionEvent event) throws IOException {
        Controller.newStage.close();
        Main.mainStage.show();
    }
    @FXML
    private void PlayAgain(ActionEvent event) throws IOException {
        Controller.newStage.close();
        Main.mainStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GloabalGamePane = GamePane;
        swingswing = swingNode;
//        JFXPanel panel = new JFXPanel();
//        panel.setSize(500,500);
//        panel.add(Controller.gamePanel);
        Controller.gamePanel.setSize(300,300);
        swingNode.setContent(Controller.gamePanel);
    }
}

