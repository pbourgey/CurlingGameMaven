package ImageProcessing;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


public class VideoCaptureApp {

    private JFrame frame;
    private JLabel imageLabel;
    private VideoCapture camera;
    private boolean isRecording;

    public VideoCaptureApp() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        
        camera = new VideoCapture(1);
        if (!camera.isOpened()) {
            System.out.println("Error: Could not open camera");
            return;
        }
        
        frame = new JFrame("Live Stream");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 720);
        frame.setLayout(new BorderLayout());
        
       
        imageLabel = new JLabel();
        frame.add(imageLabel, BorderLayout.CENTER);
        
       
        JButton stopButton = new JButton("Stop Recording");
        frame.add(stopButton, BorderLayout.SOUTH);
        
        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRecording = false;
            }
        });

   
        frame.setVisible(true);
        
     
        isRecording = true;
        startCamera();
    }
    
    private void startCamera() {
        new Thread(() -> {
            Mat frameMat = new Mat();
            int refreshRateMs = 1000;
            int numPic=0;
            while (isRecording) {
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

                if (camera.read(frameMat)) {

    	           
                    Imgproc.resize(frameMat, frameMat, new Size(960, 720));
                    
                    Imgcodecs.imwrite("captured\\captured".concat(String.valueOf(numPic)).concat(".jpg"), frameMat);
                    numPic++;
                    ImageIcon image = new ImageIcon(matToBufferedImage(frameMat));
                    imageLabel.setIcon(image);
                    imageLabel.repaint();
                }
            }
            camera.release();
            frame.dispose(); 
        }).start();
    }
    
    private BufferedImage matToBufferedImage(Mat mat) {
    	Mat matRGB = new Mat();
        Imgproc.cvtColor(mat, matRGB, Imgproc.COLOR_BGR2RGB);
        int width = matRGB.width();
        int height = matRGB.height();
        int channels = matRGB.channels();
        
        byte[] sourcePixels = new byte[width * height * channels];
        matRGB.get(0, 0, sourcePixels);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, sourcePixels);
        
        return image;
    }

    public static void main(String[] args) {
    	
    	   
    	System.setProperty("java.library.path", "opencv\\build\\java\\x64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Lancer l'application avec l'interface Swing
        SwingUtilities.invokeLater(VideoCaptureApp::new);
    }


}
