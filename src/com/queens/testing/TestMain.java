package com.queens.testing;

import com.queens.communications.JsonSerializer;
import com.queens.communications.Server;
import com.sun.media.sound.InvalidDataException;
import org.opencv.core.Rect;

import java.util.ArrayList;

public class TestMain {
    private static Server server = new Server();
    private static JsonSerializer serializer = new JsonSerializer();
    private static VisionTesting window;
    private static TestPairing pairing = new TestPairing();
    private static ArrayList<TestHazard> hazards = new ArrayList<TestHazard>();

    public static void main(String[] args) {
        window = new VisionTesting();
        hazards.add(new TestHazard());
        hazards.add(new TestHazard());
        loop();
    }

    private static void loop() {
        do {
            if (!window.isRunning()) {
                if (server.isRunning()) {
                    server.shutdown();
                }
                continue;
            } else if (!server.isRunning()) {
                server.start();
            }

            pairing.update(window.getRotation(), window.getXLocation(), window.getYLocation());
            for (int i = 0; i < 2; i++) {
                Rect hazard1 = window.getHazardRect(i);
                hazards.get(i).update(hazard1.x, hazard1.y, hazard1.width, hazard1.height);
            }

            if (!server.ready()) {
                continue;
            }

            serializer.start();
            serializer.addSection("testbot-one", pairing);
            serializer.addArray("hazards", "hazard", hazards);
            String toSend = serializer.finish();
            if (toSend != null) {
                server.putOnQueue(toSend);
            }
        } while (window.isVisible());
    }
}
