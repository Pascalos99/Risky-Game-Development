package graphics.sample;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameSceneController implements Initializable{

    @FXML
    public SwingNode swingNode;

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
        Controller.gamePanel.setSize(300,300);
        JFXPanel panel = new JFXPanel();
        panel.setSize(300,300);
        panel.add(Controller.gamePanel);
        swingNode.setContent(panel);
    }
}
