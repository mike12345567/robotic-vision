package com.queens.testing;

import com.queens.communications.JsonSerializer;
import com.queens.communications.Server;
import com.sun.media.sound.InvalidDataException;

public class TestMain {
    private static Server server = new Server();
    private static JsonSerializer serializer = new JsonSerializer();
    private static VisionTesting window;
    private static TestPairing pairing = new TestPairing();

    public static void main(String[] args) {
        window = new VisionTesting();
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

            if (!server.ready()) {
                continue;
            }

            serializer.start();
            serializer.addSection("testbot-one", pairing);
            try {
                server.putOnQueue(serializer.finish());
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        } while (window.isVisible());
    }
}
