package ui;

import java.lang.Math;
import ImageProcessing.ImageProcessing;
import ImageProcessing.ImageProcessingResult;
import gameLogic.gameLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;

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

    @FXML
    private Rectangle rectangleLaunch;

    @FXML
    private Button pauseButton;

    @FXML
    private StackPane stackPane;

    @FXML
    private Text statusText;

    private VideoCapture capture;
    int refreshRateMs = 0; // Ms entre 2 images camera
    int refreshProcessing = 0; // Process toutes les X images
    int nbImagesWoRefresh = 0;
    private Mat frame;
    private boolean cameraActive;
    private gameLogic game;
    private Point detectedCenter;
    private int initialRadius;
    private double detectedRadius;
    private boolean isLaunch;
    private boolean tempState;
    private boolean isSavedToken = true;
    private long lastMovementTime = 0;
    Point p_1;
    Point temp;
    Point p;
    private boolean isPaused = false;

    @FXML
    private Label currentRoundScore;

    Mat processedFrame;

    @FXML
    public void initialize() {
        StackPane.setAlignment(rectangleLaunch, Pos.CENTER_LEFT);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture();
        frame = new Mat();
        cameraActive = false;
        game = new gameLogic(new Point(1100, 500)); // 570, 237 (target pour la camera du projet)
                                                         // 1300, 387 (target pour la camera de mon tel)
        startCamera();

        // Set the initial state of the turn indicator
        updateTurnIndicator();
        updateBackgroundColor();

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
            capture.open(1);
            if (capture.isOpened()) {
                cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                    	if(cameraActive) {
                            List<Integer> initialRadii = new ArrayList<>();
                            while (initialRadii.size() < 20) {
                                capture.read(frame);
                                ImageProcessingResult res = ImageProcessing.processImage(frame);
                                processedFrame = ImageProcessingResult.getImage();
                                int radius = (int) res.getRadius();
                                if (radius > 0) {
                                    initialRadii.add(radius);
                                    updateStatusText("Calibration...");
                                } else {
                                    updateStatusText("Put the token in the launch area");
                                }
                                Image imageToShow = mat2Image(frame);
                                Platform.runLater(() -> videoView.setImage(imageToShow));
                                System.out.println("Radius measured: " + radius);

                            }
                            updateStatusText("Ready to play!");
                            initialRadius = (int) initialRadii.stream().mapToInt(Integer::intValue).average().orElse(0);
                            System.out.println("initialRadius : " + initialRadius);
                    	}
                        while (cameraActive) {
                        	long startTime = System.currentTimeMillis();

            	            long elapsedTime = System.currentTimeMillis() - startTime;
            	            long sleepTime = refreshRateMs - elapsedTime;

            	            if (sleepTime > 0) {
            	                try {
            	                    Thread.sleep(sleepTime);
            	                } catch (InterruptedException e) {
            	                    e.printStackTrace();
            	                }
            	            }
                            capture.read(frame);
                            if (!frame.empty()) {
                                // Process the frame using ImageProcessing
                            	if(nbImagesWoRefresh>=refreshProcessing) {
                            		ImageProcessingResult res = ImageProcessing.processImage(frame);
                            		processedFrame = ImageProcessingResult.getImage();
                                    detectedCenter = res.getCenter();
                                    detectedRadius = res.getRadius();
                            		if((temp = ImageProcessingResult.getCenter())!=null) {
	                                	p_1=p;
	                                	p=temp;
	                                	tempState=isLaunch;
	                                	isLaunch=isLaunch(isLaunch, p.x);
	                                	if (!isSavedToken && isLaunch && (Math.sqrt((p.x - p_1.x) * (p.x - p_1.x) + (p.y - p_1.y) * (p.y - p_1.y)) < 40)) {
	                                        long currentTime = System.currentTimeMillis();
	                                        if (currentTime - lastMovementTime >= 1000) {
	                                            updateStatusText("Thrown registered!\n Put the token in the launch area");
	                                            handleThrow();
	                                            isSavedToken = true;
	                                        }
	                                    } else {
	                                        lastMovementTime = System.currentTimeMillis();
	                                    }
                                	}
                                	// Image imageToShow = mat2Image(processedFrame);
                            		nbImagesWoRefresh=0;
                            	}else {
                            		nbImagesWoRefresh++;
                            	}
                                // Draw a cross at the target position
                                Point target = game.getTarget();
                                if (processedFrame != null) {


                                    // Draw circles for player 1 throws
                                    for (Point p : game.getPlayer1Positions()) {
                                        Imgproc.circle(processedFrame, p, initialRadius, new Scalar(250, 56, 56, 128), -1);
                                    }

                                    // Draw circles for player 2 throws
                                    for (Point p : game.getPlayer2Positions()) {
                                        Imgproc.circle(processedFrame, p, initialRadius, new Scalar(56, 56, 250, 128), -1);
                                    }

                                    // Finally draw the cross over the points
                                    Imgproc.line(processedFrame, new Point(target.x - 15, target.y), new Point(target.x + 15, target.y), new Scalar(0, 165, 255), 5);
                                    Imgproc.line(processedFrame, new Point(target.x, target.y - 15), new Point(target.x, target.y + 15), new Scalar(0, 165, 255), 5);
                                    Imgproc.line(processedFrame, new Point(target.x - 17, target.y), new Point(target.x + 17, target.y), new Scalar(0, 0, 0), 2);
                                    Imgproc.line(processedFrame, new Point(target.x, target.y - 17), new Point(target.x, target.y + 17), new Scalar(0, 0, 0), 2);
                                }

                                Point center = ImageProcessingResult.getCenter();
                                // double radius = ImageProcessingResult.getRadius();
                                // Imgproc.cvtColor(processedFrame, processedFrame, Imgproc.COLOR_BGR2RGB);
                            	Image imageToShow = mat2Image(frame);
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
                    handleThrow();
                }
                event.consume();
                break;
            default:
                break;
        }
    }

    public void updateStatusText(String text) {
        Platform.runLater(() -> {
            statusText.setText(text);
            statusText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-alignment: center;");

            // Determine the fill color based on the current player
            javafx.scene.paint.Color fillColor = game.getCurrentPlayer() == 0 ? javafx.scene.paint.Color.web("#187bef") : javafx.scene.paint.Color.web("#e30d0d");
            statusText.setFill(fillColor);

            // Calculate a darker shade for the stroke color
            javafx.scene.paint.Color strokeColor = fillColor.darker();
            statusText.setStroke(strokeColor);
            statusText.setStrokeWidth(1);

            // Add shadow effect for better contrast
            statusText.setEffect(new javafx.scene.effect.DropShadow(5, javafx.scene.paint.Color.BLACK));
        });
    }

    private void handleThrow() {
        game.throwToken(detectedCenter, (int) detectedRadius);
        Platform.runLater(() -> {
            player1Score.setText(String.valueOf(game.getScorePlayer1()));
            player2Score.setText(String.valueOf(game.getScorePlayer2()));
            updateTurnIndicator();
            updateCurrentRoundScore();
            updateBackgroundColor();
            if (game.isGameOver()) {
                stopCamera();
                displayFinalScore();
                updateStatusText("Game Over");
            }
        });
    }

    private void stopCamera() {
        cameraActive = false;
        capture.release();
    }

    private void displayFinalScore() {
        gameArea.getChildren().clear();

        // Determine the winner and set the background color
        String winner = game.getScorePlayer1() >= game.getWinScore() ? "Player 1" : "Player 2";
        String backgroundColor = winner.equals("Player 1") ? "linear-gradient(to bottom, #4a90e2, #357ab7, #2a5c8a)" : "linear-gradient(to bottom, #e24a4a, #b73535, #8a2a2a)";
        String textColor = winner.equals("Player 1") ? "#187bef" : "#e30d0d";
        gameArea.setStyle("-fx-background-color: " + backgroundColor + ";");

        // Create and style the final score label
        Label finalScoreLabel = new Label(game.getFinalScore());
        finalScoreLabel.setStyle("-fx-font-size: 72px; -fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

        // Create a StackPane to center the label
        StackPane stackPane = new StackPane(finalScoreLabel);
        stackPane.setAlignment(Pos.CENTER);
        stackPane.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Add the StackPane to the game area
        gameArea.getChildren().add(stackPane);
    }

    private void updateCurrentRoundScore() {
        int roundScore = game.getCurrentRoundScore();
        currentRoundScore.setText(String.valueOf(Math.abs(roundScore)));
        if (roundScore > 0) {
            currentRoundScore.setStyle("-fx-text-fill: blue;");
        } else if (roundScore < 0) {
            currentRoundScore.setStyle("-fx-text-fill: red;");
        } else {
            currentRoundScore.setStyle("-fx-text-fill: white;");
        }
    }

    private void updateTurnIndicator() {
        int currentPlayer = game.getCurrentPlayer();
        turnIndicator.setText("Player " + (currentPlayer + 1));
        if (currentPlayer == 0) {
            turnIndicator.setStyle("-fx-text-fill: #187bef;"); // Match background color
        } else {
            turnIndicator.setStyle("-fx-text-fill: #e30d0d;"); // Match background color
        }
    }

    private void updateBackgroundColor() {
        if (game.getCurrentPlayer() == 0) {
            gameArea.setStyle("-fx-background-color: linear-gradient(to bottom, #4a90e2, #357ab7, #2a5c8a);");
        } else {
            gameArea.setStyle("-fx-background-color: linear-gradient(to bottom, #e24a4a, #b73535, #8a2a2a);");
        }
    }


    public boolean isLaunch(boolean bool, double x) {

    	if(x>188) {
    		if(!bool) {
                updateStatusText("Token thrown !!\n Wait for the token to land");
    		}
    		return true;
    	}else {
    		if(bool) {
                updateStatusText("Token ready !\n Throw the token");
    		}
    		isSavedToken=false;
    	}
    	return false;
    }

    @FXML
    public void onPauseButtonClick(ActionEvent event) {
        if (isPaused) {
            // Unpause the game
            startCamera();
            pauseButton.setText("Pause");
            isPaused = false;
        } else {
            // Pause the game
            stopCamera();
            pauseButton.setText("Unpause");
            isPaused = true;
        }
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