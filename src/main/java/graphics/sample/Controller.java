package graphics.sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import game.GamePanel;
import game.GameSetup;
import game.events.GameEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import java.io.IOException;


public class Controller implements Initializable {

	static class PlayerSelect {
		public String name = null;
		public String type = null;
		public Color color = null;
		public JPanel p;
        public boolean isComplete() {
			return name!= null && !name.matches("[\\s\\v\\h ]*") && type != null && color != null; }
	}
	
	private PlayerSelect[] players = {new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect()};
	
    public static Stage newStage = new Stage();
    public static Stage gameStage = new Stage();
    public JFXButton PlayBtn;
    public JFXButton GameRulesBtn;

    @FXML
    private JFXTextField name1;
    @FXML
    private JFXTextField name2;
    @FXML
    private JFXTextField name3;
    @FXML
    private JFXTextField name4;
    @FXML
    private JFXTextField name5;
    @FXML
    private JFXTextField name6;
    
    private List<JFXTextField> namefields;
    
    @FXML
    public JFXComboBox<String> combo1;
    @FXML
    public JFXComboBox<String> combo2;
    @FXML
    public JFXComboBox<String> combo3;
    @FXML
    public JFXComboBox<String> combo4;
    @FXML
    public JFXComboBox<String> combo5;
    @FXML
    public JFXComboBox<String> combo6;
    
    private List<JFXComboBox<String>> comboboxes;
    
    @FXML
    private ColorPicker color1;
    @FXML
    private ColorPicker color2;
    @FXML
    private ColorPicker color3;
    @FXML
    private ColorPicker color4;
    @FXML
    private ColorPicker color5;
    @FXML
    private ColorPicker color6;
    
    public int selected_graphics = GameSetup.REALISTIC_BOARD_GRAPHICS;

    private List<ColorPicker> colorpickers;
    
    @FXML
    private Canvas boardPreview;
    private GraphicsContext bp_graphics;

    private void setListener(JFXTextField field, int id) {
    	field.textProperty().addListener((observable, oldValue, newValue) -> {
    		updatePlayers();
    	});
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    	namefields = List.of(name1, name2, name3, name4, name5, name6);
    	comboboxes = List.of(combo1, combo2, combo3, combo4, combo5, combo6);
    	colorpickers = List.of(color1, color2, color3, color4, color5, color6);
    	
        ObservableList<String> list = FXCollections.observableArrayList(GameSetup.player_type_names);
        for (JFXComboBox<String> b : comboboxes) b.setItems(list);
        for (ColorPicker p : colorpickers) p.setBackground(Background.EMPTY);
        for (int i=0; i < namefields.size(); i++) setListener(namefields.get(i), i);
        
        color1.setValue(Color.RED);
        color2.setValue(Color.GREEN);
        color3.setValue(Color.BLACK);
        color4.setValue(Color.WHITE);
        color5.setValue(Color.BLUE);
        color6.setValue(Color.YELLOW);
        
        Image image = new Image(AssetFinder.getResource("circle-cropped-2.png").toString());
        bp_graphics = boardPreview.getGraphicsContext2D();
        bp_graphics.drawImage(image, 0, 0, boardPreview.getWidth(), boardPreview.getHeight());
        
        updatePlayers();
    }
    
    private GameSetup getGameSetup() {
    	GameSetup gs = new GameSetup();
    	gs.setBoardGraphics(selected_graphics);
    	for (PlayerSelect ps : players)
    		if (ps.isComplete()) gs.addPlayer(ps.type, ps.name,
    				new java.awt.Color((int)(255 * ps.color.getRed()), (int)(255 * ps.color.getGreen()), (int)(255 * ps.color.getBlue())));
    	return gs;
    }

    @FXML
    private void Play() throws IOException{
    	updatePlayers();
    	GameSetup gs = getGameSetup();
    	if (gs.playerCount() == 0) { System.err.println("Starting graphical debug mode"); }
    	else if (!gs.hasValidPlayerCount()) { System.err.println("Must have at least 2 players and an even amount of players to play"); return; }

    	GameEvent.clearAll();
		GamePanel result = gs.build();

		///*		<-- (un)comment this line to toggle code:
    	JFrame frame = new JFrame("Risky Checkers v0.005");
    	frame.setSize(800, 800);
    	JPanel mainPanel = new JPanel();
    	mainPanel.setPreferredSize(new Dimension(500,500));
    	mainPanel.setBounds(100,100,500,500);
    	mainPanel.add(result);
    	frame.add(mainPanel);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);
    	//*/

//        Parent root = FXMLLoader.load(AssetFinder.getResource("GameScene.fxml"));
//        Scene s = new Scene(root);
//
//        JFrame frame = new JFrame("Swing and JavaFX");
//        final JFXPanel fxPanel = new JFXPanel();
//        frame.add(fxPanel);
//        frame.setSize(1000, 1000);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                initFX(fxPanel,s);
//            }
//        });


    	//			<-- (un)comment this line to toggle code:
//        JFXPanel panel = new JFXPanel();
//        panel.setSize(500,500);
//        panel.add(result);
//        createAndSetSwingContent(GameSceneController.swingNode, panel);
//        GameSceneController.GamePane.getChildren().add(GameSceneController.swingNode);

//        Parent root = FXMLLoader.load(AssetFinder.getResource("GameScene.fxml"));
//        gameStage.setScene(new Scene(root, 1000, 1000));
//        gameStage.setScene(new Scene(GameSceneCon));
//        Main.mainStage.hide();
//        gameStage.showAndWait();

    	System.out.println("Created Game");
    }

    private void initFX(JFXPanel fxPanel,Scene scene) {
        fxPanel.setScene(scene);
    }

    @FXML
    private void Rules(ActionEvent event) throws IOException{
        StageChanger();
        Main.mainStage.hide();
        newStage.showAndWait();
    }

    public void StageChanger() throws IOException {
        Parent type2view = FXMLLoader.load(AssetFinder.getResource("GameRule.fxml"));
        Scene type2ViewScene = new Scene(type2view);
        newStage.setScene(type2ViewScene);
        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                newStage.close();
                Main.mainStage.show();
            }
        });
    }
    @FXML
    public void updatePlayers() {
    	boolean preview_needs_update = false;
    	for (int i=0; i < players.length; i++) {
    		Color old_color = players[i].color;
    		boolean was_valid = players[i].isComplete();

    		players[i].name = namefields.get(i).getText();
    		players[i].type = comboboxes.get(i).getValue();
    		players[i].color = colorpickers.get(i).getValue();

    		if (!players[i].color.equals(old_color)) preview_needs_update = true;
    		if (was_valid != players[i].isComplete()) preview_needs_update = true;
    	}
    	if (preview_needs_update) updatePreview();
    }

    public void updatePreview() {
    	if (bp_graphics == null) return;
    	GraphicsContext g = bp_graphics;
    	g.clearRect(0, 0, boardPreview.getWidth(), boardPreview.getHeight());
    	GameSetup trial = getGameSetup();
    	Image img = SwingFXUtils.toFXImage(trial.getGraphics(trial.getBoard()).getImage(), null);
    	g.drawImage(img, 0, 0, boardPreview.getWidth(), boardPreview.getHeight());
    }

//    private void createAndSetSwingContent(SwingNode swingNode, JFXPanel panel) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                swingNode.setContent(panel);
//            }
//        });
//    }

}
