package com.queens;

import com.queens.colours.ColourNames;
import com.queens.communications.JsonSerializer;
import com.queens.communications.Server;
import com.queens.entities.BorderedArea;
import com.queens.entities.ColouredArea;
import com.queens.entities.ObjectPairing;
import com.queens.utilities.MatOperations;
import com.queens.utilities.OutputFrame;
import com.queens.utilities.Utilities;
import org.opencv.core.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {
    static private boolean outputEnabled = false;
    static private boolean disableServer = false;

    private static Server server = new Server();
    private static JsonSerializer serializer = new JsonSerializer();
    private static ArrayList<ObjectPairing> robots = new ArrayList<ObjectPairing>();
    private static ArrayList<BorderedArea> hazards = new ArrayList<BorderedArea>();
    private static OpenCV openCV;
    private static OutputFrame frame;

    public static void main(String[] args) {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        openCV = new OpenCV();
        frame = new OutputFrame(server);

        robots.add(new ObjectPairing("testbot-one", ColourNames.Green, ColourNames.OrangeAndRed));
        robots.add(new ObjectPairing("testbot-two", ColourNames.OrangeAndRed, ColourNames.Blue));
        robots.add(new ObjectPairing("testbot-three", ColourNames.Blue, ColourNames.Yellow));
        robots.add(new ObjectPairing("testbot-four", ColourNames.Yellow, ColourNames.Green));

        loop();
    }

    public static void shutdown() {
        openCV.shutdown();
        server.shutdown();
    }

    public static OpenCV getOpenCV() {
        return openCV;
    }

    public static boolean currentlyRunning() {
        return openCV.systemActive();
    }

    private static void loop() {
        do {
            for (ColouredArea area : openCV.getAreas()) {
                area.resetUpdated();
            }

            openCV.process();

            ArrayList<ColouredArea> toRemove = new ArrayList<ColouredArea>();
            for (int i = 0; i < openCV.getAreas().size(); i++) {
                if (!openCV.getAreas().get(i).hasBeenUpdated()) {
                    toRemove.add(openCV.getAreas().get(i));
                }
            }
            openCV.getAreas().removeAll(toRemove);

            for (ObjectPairing robot : robots) {
                robot.checkForPairing(openCV.getAreas());
            }

            handleHazards();

            sendData();

            openCV.addColouredAreaOutputs();
            BufferedImage displayImage = frame.toBufferedImage(openCV.getDisplayImage());
            displayImage = drawImage(displayImage);

            if (displayImage != null) {
                frame.show(displayImage);
            }

        } while (currentlyRunning());
    }

    private static BufferedImage drawImage(BufferedImage toUpdate) {
        int hazardCount = 1;

        for (BorderedArea hazard : hazards) {
            if (hazard.isActive()) {
                String hazardName = Utilities.generateArrayElemName("hazard", hazardCount++);
                toUpdate = frame.addLabel(toUpdate, hazardName, hazard.getX(), hazard.getY());
            }
        }
        for (ObjectPairing robot : robots) {
            if (robot.isActive()) {
                toUpdate = frame.addLabel(toUpdate, robot.getPairingName(), robot.getX(), robot.getY());
            }
        }
        return toUpdate;
    }

    private static void handleHazards() {
        /* update old hazards, remove those which are no longer valid */
        Iterator<BorderedArea> hazardIterator = hazards.iterator();

        while (hazardIterator.hasNext()) {
            BorderedArea hazard = hazardIterator.next();
            hazard.update(openCV.getAreas());
            if (!hazard.isActive()) {
                hazardIterator.remove();
            }
        }

        /* generate new hazards if any are found */
        BorderedArea hazard;
        do {
            hazard = new BorderedArea(ColourNames.Yellow, ColourNames.OrangeAndRed);
            hazard.update(openCV.getAreas());
            if (hazard.isActive()) {
                hazards.add(hazard);
            }
        } while (hazard.isActive());
    }

    private static void sendData() {
        if (!server.ready()) {
            return;
        }

        if (disableServer && outputEnabled) {
            for (ObjectPairing robot : robots) {
                if (!robot.isActive()) continue;
                float rotation = robot.getRotation();
                int x = robot.getX();
                int y = robot.getY();
                System.out.printf("%s Rotation: %f X: %d Y: %d\n", robot.getPairingName(), rotation, x, y);
            }

        } else if (!disableServer) {
            serializer.start();
            for (ObjectPairing robot : robots) {
                if (!robot.isActive()) continue;
                serializer.addSection(robot.getPairingName(), robot);
            }

            if (hazards.size() > 0) {
                serializer.addArray("hazards", "hazard", hazards);
            }
            String toSend = serializer.finish();
            if (toSend != null) {
                server.putOnQueue(toSend);
            }
        }
    }

    public static void lightingChanged() {
        MatOperations.alphaFactor = MatOperations.defaultFactor;
        MatOperations.betaFactor = MatOperations.defaultFactor;
        frame.resetBrightnessLabels();
    }
}
