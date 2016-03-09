package com.queens;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {
    private LinkedBlockingQueue<String> toSend = new LinkedBlockingQueue<String>();
    private static final String url = "http://localhost:3000/devices/locationData";
    private static final String userAgent = "Mozilla/5.0";
    private static final String urlParameters = "";
    private static final int sendSpeedMs = 1000;

    public Server() {}

    private void sendData(String data, String urlParameters) {
        // Send post request
        HttpURLConnection connection = null;
        URL obj = null;
        DataOutputStream wr;
        String inputLine;
        BufferedReader in;
        StringBuilder response;

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
        connection.setDoOutput(true);

        try {
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("\nPost parameters : " + urlParameters);
            System.out.println("\nResponse Code : " + responseCode);

            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            response = new StringBuilder(data);

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            //print result
            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putOnQueue(String data) throws InterruptedException {
        toSend.put(data);
    }

    public void run() {
        while (Main.systemActive()) {
            String data = toSend.poll();
            try {
                Thread.sleep(sendSpeedMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (data != null) {
                sendData(data, urlParameters);
            }
        }
    }
}
