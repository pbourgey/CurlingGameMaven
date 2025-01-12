package gameLogic;

import org.opencv.core.Point;
import java.util.*;

public class gameLogic {
    private int scorePlayer1;
    private int scorePlayer2;

    // Liste des distances des boules pour chaque joueur
    private List<Double> player1Throws;
    private List<Double> player2Throws;

    // Liste des positions des boules pour chaque joueur
    private List<Point> player1Positions;
    private List<Point> player2Positions;

    private int currentTurn;
    private int currentPlayer;
    private int nbTurns;
    private Point target;
    private int throwsInTurn;
    private boolean isGameOver;
    private Random random;

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
        this.nbTurns = 0;
        this.currentTurn = 0;
        this.throwsInTurn = 0;
        this.random = new Random();
        this.currentPlayer = random.nextInt(2);
        this.isGameOver = false;
    }

    private int determineNextPlayer() {
        if (throwsInTurn >= 6) return -1;

        double bestPlayer1Distance = player1Throws.isEmpty() ? Double.MAX_VALUE :
                Collections.min(player1Throws);
        double bestPlayer2Distance = player2Throws.isEmpty() ? Double.MAX_VALUE :
                Collections.min(player2Throws);

        if (player1Throws.size() == 3) return 1;
        if (player2Throws.size() == 3) return 0;

        return bestPlayer1Distance <= bestPlayer2Distance ? 1 : 0;
    }

    // Retourne le score actuel de la manche
    public int getCurrentRoundScore() {
        if (player1Throws.isEmpty() || player2Throws.isEmpty()) return 0;

        double bestPlayer1 = Collections.min(player1Throws);
        double bestPlayer2 = Collections.min(player2Throws);

        int points = 0;
        if (bestPlayer1 < bestPlayer2) {
            // Score positif pour joueur 1
            for (Double throw1 : player1Throws) {
                if (throw1 < bestPlayer2) points++;
            }
            return points;  // retourne 1, 2 ou 3 pour joueur 1
        } else {
            // Score négatif pour joueur 2
            for (Double throw2 : player2Throws) {
                if (throw2 < bestPlayer1) points++;
            }
            return -points;  // retourne -1, -2 ou -3 pour joueur 2
        }
    }

    private void calculateScore() {
        int roundScore = getCurrentRoundScore();
        if (roundScore > 0) {
            scorePlayer1 += roundScore;
        } else {
            scorePlayer2 += -roundScore;
        }
    }

    public void throwToken(Point center, Integer radius) {
        double throwDistance = distance(center, target);

        if (currentPlayer == 0) {
            player1Throws.add(throwDistance);
            player1Positions.add(center);
        } else {
            player2Throws.add(throwDistance);
            player2Positions.add(center);
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
            currentPlayer = scorePlayer1 > scorePlayer2 ? 1 : 0;
        } else {
            currentPlayer = determineNextPlayer();
        }

        if (currentTurn == nbTurns) {
            isGameOver = true;
        }
    }

    // Getters pour les positions des boules
    public List<Point> getPlayer1Positions() {
        return new ArrayList<>(player1Positions);
    }

    public List<Point> getPlayer2Positions() {
        return new ArrayList<>(player2Positions);
    }

    // Getters existants
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
    public void setNbTurns(int nbTurns) { this.nbTurns = nbTurns; }
    public void setTarget(Point target) { this.target = target; }
}