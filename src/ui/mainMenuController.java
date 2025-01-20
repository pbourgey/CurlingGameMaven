package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class mainMenuController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button playButton;

    @FXML
    private VBox settingsBox;

    private boolean isFirstPlayClick = true;

    @FXML
    private Label pointsToWinLabel;

    @FXML
    private Label tokensPerRoundLabel;

    private int pointsToWin = 13;
    private int tokensPerRound = 3;

    @FXML
    public void onPlayButtonClick(ActionEvent event) throws IOException {
        if (isFirstPlayClick) {
            settingsBox.setVisible(true);
            settingsBox.setManaged(true);
            isFirstPlayClick = false;
        } else {
            // Load the game interface (second window)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("inGame-view.fxml"));
            Scene gameScene = new Scene(fxmlLoader.load());

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            inGameController controller = fxmlLoader.getController();
            controller.setGameSettings(pointsToWin, tokensPerRound);
            stage.setScene(gameScene);
            stage.setTitle("Game Interface");
            stage.show();
        }
    }

    @FXML
    public void onDecreasePointsClick(ActionEvent event) {
        if (pointsToWin > 1) {
            pointsToWin--;
            pointsToWinLabel.setText(String.valueOf(pointsToWin));
        }
    }

    @FXML
    public void onIncreasePointsClick(ActionEvent event) {
        pointsToWin++;
        pointsToWinLabel.setText(String.valueOf(pointsToWin));
    }

    @FXML
    public void onDecreaseTokensClick(ActionEvent event) {
        if (tokensPerRound > 1) {
            tokensPerRound--;
            tokensPerRoundLabel.setText(String.valueOf(tokensPerRound));
        }
    }

    @FXML
    public void onIncreaseTokensClick(ActionEvent event) {
        tokensPerRound++;
        tokensPerRoundLabel.setText(String.valueOf(tokensPerRound));
    }
}