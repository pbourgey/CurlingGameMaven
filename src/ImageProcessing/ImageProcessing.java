package ImageProcessing;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessing {
    static {
        // Load OpenCV library
        System.setProperty("java.library.path", "C:\\opencv\\opencv\\build\\java\\x64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Charger la bibliothèque native OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Charger l'image dans un objet Mat (remplacez par votre chemin d'image)
        Mat image = Imgcodecs.imread("C:\\Users\\Pierre\\eclipse-workspace\\CurlingGameMaven\\nice_capture\\captured8.jpg", Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            System.out.println("Erreur : Impossible de charger l'image.");
            return;
        }

        // Convertir en niveaux de gris
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou pour réduire le bruit
        Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);

        // Détecter les cercles avec HoughCircles
        Mat circles = new Mat();
        Imgproc.HoughCircles(
            gray,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.0,
            (double)gray.rows() / 8, // Distance minimale entre les centres de cercles
            100.0, // Seuil supérieur pour le détecteur de bords Canny
            30.0,  // Seuil d'accumulateur pour les centres de cercles
            10,    // Rayon minimum
            100    // Rayon maximum
        );

        // Trouver le cercle le plus "circulaire"
        double maxRoundness = 0;
        Point bestCenter = null;
        double bestRadius = 0;

        for (int i = 0; i < circles.cols(); i++) {
            double[] data = circles.get(0, i);
            if (data == null) continue;

            double centerX = data[0];
            double centerY = data[1];
            double radius = data[2];

            // Vérifier la "circularité" : ici, nous utilisons le rayon comme approximation
            double roundness = Math.PI * radius * radius;
            if (roundness > maxRoundness) {
                maxRoundness = roundness;
                bestCenter = new Point(centerX, centerY);
                bestRadius = radius;
            }
        }

        // Dessiner le meilleur cercle trouvé sur l'image d'origine
        if (bestCenter != null) {
            Imgproc.circle(image, bestCenter, (int)bestRadius, new Scalar(0, 255, 0), 3);
            Imgproc.rectangle(
                image,
                new Point(bestCenter.x - bestRadius, bestCenter.y - bestRadius),
                new Point(bestCenter.x + bestRadius, bestCenter.y + bestRadius),
                new Scalar(255, 0, 0),
                2
            );
            System.out.println("Cercle le plus circulaire trouvé : Centre = " + bestCenter + ", Rayon = " + bestRadius);
        } else {
            System.out.println("Aucun cercle détecté.");
        }

        // Enregistrer l'image résultante (avec le cercle dessiné)
        Imgcodecs.imwrite("output_image.jpg", image);
    }
}