package com.queens.entities;

import com.queens.Main;
import com.queens.OpenCV;
import com.queens.communications.JsonSerializer;
import com.queens.communications.Server;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MyMouseListener implements MouseListener {
    private static final String locationEndpoint = "moveToTarget";
    private static final String robot = "testbot-one";
    Server server;

    public MyMouseListener(Server server) {
        this.server = server;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        OpenCV openCV = Main.getOpenCV();
        int x = e.getX() - 6;
        int y = e.getY() - 27;
        openCV.points.add(new Point(x, y));
        Mat mat = new Mat();
        Imgproc.cvtColor(openCV.getLastImage(), mat, Imgproc.COLOR_BGR2HSV);

        double array[] = mat.get(y, x);
        if (array == null) return;

        for (double number : array) {
            System.out.printf("%f, ", number);
        }
        Coordinates coords = new Coordinates(x, y);
        JsonSerializer serializer = new JsonSerializer();
        serializer.addSection("coordinates", coords);
        serializer.addSingleData("deviceName", robot);
        server.sendPost(serializer.finish(), locationEndpoint);
        System.out.println();
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
