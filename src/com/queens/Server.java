package com.queens;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {
    private LinkedBlockingQueue<String> toSend = new LinkedBlockingQueue<String>();
    private static final String url = "http://localhost:3000/devices/locationData";
    private static final String userAgent = "Mozilla/5.0";
    private static final String urlParameters = "";
    private static final int sendSpeedMs = 0;
    private static final int retryConnectionMs = 1000;
    private boolean noConnect = false;

    public Server() {}

    private void sendData(String data, String urlParameters) throws InterruptedException {
        // Send post request
        HttpURLConnection connection = null;
        URL obj = null;
        DataOutputStream wr;

        System.out.println("SENDING DATA: " + data);

        try {
            obj = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert obj != null;
            connection = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert connection != null;
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try {
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + Integer.toString(responseCode));
            noConnect = false;
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                noConnect = true;
            } else {
                e.printStackTrace();
            }
        }
    }

    public void putOnQueue(String data) {
        try {
            toSend.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (Main.systemActive()) {
            String data = toSend.poll();
            try {
                if (noConnect) {
                    Thread.sleep(retryConnectionMs);
                }
                if (sendSpeedMs != 0) {
                    Thread.sleep(sendSpeedMs);
                }
                if (data != null) {
                    sendData(data, urlParameters);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
