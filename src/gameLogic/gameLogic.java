package gameLogic;

import org.opencv.core.Point;

public class gameLogic {

    private int scorePlayer1;
    private int scorePlayer2;
    private double player1distance;
    private double player2distance;

    private int currentTurn;
    private int currentPlayer;
    private int nbTurns;
    private Point target;

    private boolean isGameOver;

    private double distance(Point center, Point target) {
        return Math.sqrt(Math.pow(center.x - target.x, 2) + Math.pow(center.y - target.y, 2));
    }

    public gameLogic(Point initialTarget) {
        this.target = initialTarget;
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        nbTurns = 0;
        currentTurn = 0;
        currentPlayer = 0;
        isGameOver = false;
    }

    public void throwToken(Point center, Integer radius) {

        // Calcul de la distance du tir par rapport à la cible
        if (currentPlayer == 0) {
            player1distance = distance(center, target);
        } else {
            player2distance = distance(center, target);
        }

        // Passage au joueur suivant
        if (currentPlayer == 0) {
            currentPlayer = 1;
        } else {
            currentPlayer = 0;
        }

        if (currentPlayer == 1) {
            if (player1distance < player2distance) {
                scorePlayer1++;
            } else {
                scorePlayer2++;
            }
            player1distance = 0;
            player2distance = 0;

            currentTurn++;
        }
        if (currentTurn == nbTurns+1) {
            isGameOver = true;
        }
    }

    public int getScorePlayer1() {
        return scorePlayer1;
    }
    public int getScorePlayer2() {
        return scorePlayer2;
    }
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    public boolean isGameOver() {
        return isGameOver;
    }
    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setNbTurns(int nbTurns) {
        this.nbTurns = nbTurns;
    }

    public void setTarget(Point target) {
        this.target = target;
    }

}
