package ui;

import ImageProcessing.ImageProcessing;
import ImageProcessing.ImageProcessingResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;

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

    @FXML
    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture();
        frame = new Mat();
        cameraActive = false;
        startCamera();
    }

    private void startCamera() {
        if (!cameraActive) {
            capture.open(1);
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
                                Point center = res.getCenter();
                                double radius = res.getRadius();
                                // Imgproc.cvtColor(processedFrame, processedFrame, Imgproc.COLOR_BGR2RGB);
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

    @FXML
    public void onRestartButtonClick(ActionEvent event) {
        // Placeholder for game restart logic
        System.out.println("Game restarted!");
        resetGameUI();
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