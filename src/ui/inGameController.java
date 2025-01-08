package ui;

import ImageProcessing.ImageProcessing;
import ImageProcessing.ImageProcessingResult;
import gameLogic.gameLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;

public class inGameController {
    @FXML
    private Label player1Score;

    @FXML
    private Label player2Score;

    @FXML
    private Label turnIndicator;

    @FXML
    private AnchorPane gameArea;

    @FXML
    private ImageView videoView;

    private VideoCapture capture;
    private Mat frame;
    private boolean cameraActive;
    private gameLogic game;
    private Point detectedCenter;
    private double detectedRadius;

    @FXML
    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture();
        frame = new Mat();
        cameraActive = false;
        game = new gameLogic(new Point(1200, 360)); // Initialize gameLogic with a target point
        startCamera();

        // Add a listener to set up the key event handler once the scene is available
        gameArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
                gameArea.requestFocus(); // Request focus for gameArea
            }
        });
    }

    private void startCamera() {
        if (!cameraActive) {
            capture.open(0);
            if (capture.isOpened()) {
                cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        while (cameraActive) {
                            capture.read(frame);
                            if (!frame.empty()) {
                                // Process the frame using ImageProcessing
                                ImageProcessingResult res = ImageProcessing.processImage(frame);
                                Mat processedFrame = res.getImage();

                                // Draw a cross at the target position
                                Point target = game.getTarget();
                                Imgproc.line(processedFrame, new Point(target.x - 10, target.y), new Point(target.x + 10, target.y), new Scalar(0, 0, 255), 2);
                                Imgproc.line(processedFrame, new Point(target.x, target.y - 10), new Point(target.x, target.y + 10), new Scalar(0, 0, 255), 2);

                                detectedCenter = res.getCenter();
                                detectedRadius = res.getRadius();
                                Image imageToShow = mat2Image(processedFrame);
                                Platform.runLater(() -> videoView.setImage(imageToShow));
                            }
                        }
                    }
                };
                Thread thread = new Thread(frameGrabber);
                thread.setDaemon(true);
                thread.start();
            } else {
                System.err.println("Impossible d'ouvrir la caméra.");
            }
        } else {
            cameraActive = false;
            capture.release();
        }
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE:
                if (detectedCenter != null && detectedRadius > 0) {
                    // Call throwToken function with the detected center and radius
                    game.throwToken(detectedCenter, (int) detectedRadius);
                    System.out.println("Space pressed");
                    // Update the display with the score, current turn, and whose turn it is
                    Platform.runLater(() -> {
                        player1Score.setText("Player 1: " + game.getScorePlayer1());
                        player2Score.setText("Player 2: " + game.getScorePlayer2());
                        turnIndicator.setText("Turn: Player " + (game.getCurrentPlayer() + 1));
                    });
                }
                event.consume();
                break;
            default:
                break;
        }
    }

    @FXML
    public void onRestartButtonClick(ActionEvent event) {
        // Placeholder for game restart logic
        System.out.println("Game restarted!");
        // resetGameUI();
    }

    @FXML
    public void onLeaveButtonClick(ActionEvent event) {
        // Placeholder for leaving the game
        System.out.println("Exiting the game...");
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close(); // Close the game window
    }

    private void resetGameUI() {
        // Reset the UI elements to their default state
        player1Score.setText("Player 1: 0");
        player2Score.setText("Player 2: 0");
        turnIndicator.setText("Turn: Player 1");
    }
}