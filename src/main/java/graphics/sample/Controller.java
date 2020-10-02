package graphics.sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import game.GamePanel;
import game.GameSetup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import java.io.IOException;


public class Controller implements Initializable {

	static class PlayerSelect {
		public String name = null;
		public String type = null;
		public Color color = null;
		public boolean isComplete() {
			return name!= null && name != "" && type != null && color != null; }
	}
	
	private PlayerSelect[] players = {new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect(), new PlayerSelect()};
	
    public static Stage newStage = new Stage();
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
    
    private List<ColorPicker> colorpickers;
    
    @FXML
    private Canvas boardPreview;
    private GraphicsContext bp_graphics;

    private void setListener(JFXTextField field, int id) {
    	field.textProperty().addListener((observable, oldValue, newValue) -> {
    		players[id].name = field.getText();
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
    
    @FXML
    private void Play() throws IOException{
    	updatePlayers();
    	GameSetup gs = new GameSetup();
    	for (PlayerSelect ps : players)
    		if (ps.isComplete()) gs.addPlayer(ps.type, ps.name,
    				new java.awt.Color((int)(255 * ps.color.getRed()), (int)(255 * ps.color.getGreen()), (int)(255 * ps.color.getBlue())));
    	if (!gs.hasValidPlayerCount()) { System.err.println("Must have at least 2 players and an even amount of players to play"); return; }
    	
		GamePanel result = gs.build();
    	
    	JFrame frame = new JFrame("Risky Checkers v0.005");
    	frame.setSize(500, 500);
    	frame.add(result);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);
    	
    	System.out.println("Created Game");
    	// @Mohammad, I don't know what to do from here
    	
    }

    @FXML
    private void Rules(ActionEvent event) throws IOException{
        StageChanger();
//        Main.mainStage.hide();
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
//                Main.mainStage.show();
            }
        });
    }
    @FXML
    public void updatePlayers() {
    	for (int i=0; i < players.length; i++) {
    		players[i].name = namefields.get(i).getText();
    		players[i].type = comboboxes.get(i).getValue();
    		players[i].color = colorpickers.get(i).getValue();
    	}
    	javafx.scene.effect.ColorAdjust e = new javafx.scene.effect.ColorAdjust();
    	e.setHue(new java.util.Random().nextDouble());
    	bp_graphics.applyEffect(e);
    }

}
