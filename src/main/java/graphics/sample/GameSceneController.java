package graphics.sample;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameSceneController implements Initializable{

    @FXML
    public Pane GamePane;

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
        Controller.gamePanel.setSize(500,500);
        JFXPanel panel = new JFXPanel();
        panel.setSize(500,500);
        panel.add(Controller.gamePanel);
        swingNode.setContent(panel);
    }
}

