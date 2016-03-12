package com.queens.testing;

import com.queens.JsonSerializer;
import com.queens.Server;
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
                continue;
            }

            pairing.update(window.getRotation(), window.getXLocation(), window.getYLocation());

            serializer.start();
            serializer.addSection(pairing);
            try {
                server.putOnQueue(serializer.finish());
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        } while (window.isVisible());
    }
}
