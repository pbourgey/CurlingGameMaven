package ui;

import java.lang.Math;
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
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Point;
import javafx.geometry.Pos;

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
    private StackPane stackPane;

    private VideoCapture capture;
    int refreshRateMs = 100; // Ms entre 2 images camera
    int refreshProcessing = 4; // Process toutes les X images
    int nbImagesWoRefresh = 0;
    private Mat frame;
    private boolean cameraActive;
    private gameLogic game;
    private Point detectedCenter;
    private double detectedRadius;
    private boolean isLaunch;
    private boolean tempState;
    private boolean isSavedToken = true;
    Point p_1;
    Point temp;
    Point p;

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
        game = new gameLogic(new Point(570, 237)); // Initialize gameLogic with a target point
        startCamera();

        // Set the initial state of the turn indicator
        updateTurnIndicator();

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
	                                	if(!isSavedToken&&isLaunch&&(java.lang.Math.sqrt((p.x - p_1.x)*(p.x - p_1.x)+(p.y - p_1.y)*(p.y - p_1.y))<5)) {
	                                		System.out.println("jeton enregistré");
	                                		isSavedToken = true;
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
                                    Imgproc.line(processedFrame, new Point(target.x - 10, target.y), new Point(target.x + 10, target.y), new Scalar(0, 0, 255), 2);
                                    Imgproc.line(processedFrame, new Point(target.x, target.y - 10), new Point(target.x, target.y + 10), new Scalar(0, 0, 255), 2);

                                    // Draw circles for player 1 throws
                                    for (Point p : game.getPlayer1Positions()) {
                                        Imgproc.circle(processedFrame, p, 25, new Scalar(74, 144, 226, 128), -1); // Blue with transparency
                                    }

                                    // Draw circles for player 2 throws
                                    for (Point p : game.getPlayer2Positions()) {
                                        Imgproc.circle(processedFrame, p, 25, new Scalar(226, 74, 74, 128), -1); // Red with transparency
                                    }
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

    private void handleThrow(){
        game.throwToken(detectedCenter, (int) detectedRadius);
        Platform.runLater(() -> {
            player1Score.setText(String.valueOf(game.getScorePlayer1()));
            player2Score.setText(String.valueOf(game.getScorePlayer2()));
            updateTurnIndicator();
            updateCurrentRoundScore();
            updateBackgroundColor();
        });
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
            turnIndicator.setStyle("-fx-text-fill: #4a90e2;"); // Match background color
        } else {
            turnIndicator.setStyle("-fx-text-fill: #e24a4a;"); // Match background color
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
    			System.out.println("jeton lancé !");
                handleThrow();
    		}
    		return true;
    	}else {
    		if(bool) {
    			System.out.println("jeton pret !");
    		}
    		isSavedToken=false;
    	}
    	return false;
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