package gameLogic;

import org.opencv.core.Point;
import java.util.*;

public class gameLogic {
    private int scorePlayer1;
    private int scorePlayer2;

    // List of distances of tokens for each player
    private List<Double> player1Throws;
    private List<Double> player2Throws;

    // List of positions of tokens for each player
    private List<Point> player1Positions;
    private List<Point> player2Positions;

    private int currentTurn;
    private int currentPlayer;
    private Point target;
    private int throwsInTurn;
    private boolean isGameOver;
    private Random random;

    // Counters for the number of tokens each player has thrown
    private int player1TokenCount;
    private int player2TokenCount;

    // Win score variable
    private int winScore;

    private double distance(Point center, Point target) {
        return Math.sqrt(Math.pow(center.x - target.x, 2) + Math.pow(center.y - target.y, 2));
    }

    public gameLogic(Point initialTarget) {
        this.target = initialTarget;
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.player1Throws = new ArrayList<>();
        this.player2Throws = new ArrayList<>();
        this.player1Positions = new ArrayList<>();
        this.player2Positions = new ArrayList<>();
        this.currentTurn = 0;
        this.throwsInTurn = 0;
        this.random = new Random();
        this.currentPlayer = 0;
        this.isGameOver = false;
        this.player1TokenCount = 0;
        this.player2TokenCount = 0;
        this.winScore = 5; // Default win score
    }

    private int determineNextPlayer() {
        if (throwsInTurn == 0) return 0;
        if (throwsInTurn >= 6) return -1;

        double bestPlayer1Distance = player1Throws.isEmpty() ? Double.MAX_VALUE : Collections.min(player1Throws);
        double bestPlayer2Distance = player2Throws.isEmpty() ? Double.MAX_VALUE : Collections.min(player2Throws);

        if (player1TokenCount >= 3) return 1;
        if (player2TokenCount >= 3) return 0;

        return bestPlayer1Distance < bestPlayer2Distance ? 1 : 0;
    }

    // Returns the current round score
    public int getCurrentRoundScore() {
        if (player1Throws.isEmpty() || player2Throws.isEmpty()) return 0;

        double bestPlayer1 = Collections.min(player1Throws);
        double bestPlayer2 = Collections.min(player2Throws);

        int points = 0;
        if (bestPlayer1 < bestPlayer2) {
            // Positive score for player 1
            for (Double throw1 : player1Throws) {
                if (throw1 < bestPlayer2) points++;
            }
            return points;  // returns 1, 2, or 3 for player 1
        } else {
            // Negative score for player 2
            for (Double throw2 : player2Throws) {
                if (throw2 < bestPlayer1) points++;
            }
            return -points;  // returns -1, -2, or -3 for player 2
        }
    }

    private void calculateScore() {
        int roundScore = getCurrentRoundScore();
        if (roundScore > 0) {
            scorePlayer1 += roundScore;
        } else {
            scorePlayer2 += -roundScore;
        }

        // Check if any player has reached the win score
        if (scorePlayer1 >= winScore) {
            isGameOver = true;
            System.out.println("Player 1 wins!");
        } else if (scorePlayer2 >= winScore) {
            isGameOver = true;
            System.out.println("Player 2 wins!");
        }
    }

    private void removeOverlappingTokens(Point newToken, double initialRadius) {
        if (currentPlayer == 0) {
            for (int i = 0; i < player2Positions.size(); i++) {
                if (distance(newToken, player2Positions.get(i)) < initialRadius) {
                    player2Positions.remove(i);
                    player2Throws.remove(i);
                    break;
                }
            }
        } else {
            for (int i = 0; i < player1Positions.size(); i++) {
                if (distance(newToken, player1Positions.get(i)) < initialRadius) {
                    player1Positions.remove(i);
                    player1Throws.remove(i);
                    break;
                }
            }
        }
    }

    public void throwToken(Point center, Integer radius) {
        if (isGameOver) return;

        double throwDistance = distance(center, target);

        removeOverlappingTokens(center, radius);
        if (currentPlayer == 0) {
            player1Throws.add(throwDistance);
            player1Positions.add(center);
            player1TokenCount++;
        } else {
            player2Throws.add(throwDistance);
            player2Positions.add(center);
            player2TokenCount++;
        }

        throwsInTurn++;

        if (throwsInTurn == 6) {
            calculateScore();
            player1Throws.clear();
            player2Throws.clear();
            player1Positions.clear();
            player2Positions.clear();
            throwsInTurn = 0;
            currentTurn++;
            player1TokenCount = 0;
            player2TokenCount = 0;
            if (!isGameOver) {
                currentPlayer = scorePlayer1 > scorePlayer2 ? 1 : 0;
            }
        } else {
            currentPlayer = determineNextPlayer();
        }

        if (isGameOver) {
            System.out.println("Game Over");
        }
    }

    public String getFinalScore() {
        return "Player " + (scorePlayer1 >= winScore ? "1" : "2") + " wins! " + scorePlayer1 + "-" + scorePlayer2;
    }

    // Getters for token positions
    public List<Point> getPlayer1Positions() {
        return new ArrayList<>(player1Positions);
    }

    public List<Point> getPlayer2Positions() {
        return new ArrayList<>(player2Positions);
    }

    // Existing getters
    public int getWinScore() {
        return winScore;
    }
    public int getScorePlayer1() { return scorePlayer1; }
    public int getScorePlayer2() { return scorePlayer2; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return isGameOver; }
    public int getCurrentTurn() { return currentTurn; }
    public Point getTarget() { return target; }
    public int getThrowsInTurn() { return throwsInTurn; }
    public List<Double> getPlayer1Throws() { return new ArrayList<>(player1Throws); }
    public List<Double> getPlayer2Throws() { return new ArrayList<>(player2Throws); }

    // Setters
    public void setWinScore(int winScore) { this.winScore = winScore; }
    public void setTarget(Point target) { this.target = target; }
}