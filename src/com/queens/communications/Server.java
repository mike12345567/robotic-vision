package com.queens.communications;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {
    private LinkedBlockingQueue<String> toSend = new LinkedBlockingQueue<String>();
    private String latest = "";
    private static final String url = "http://localhost:3000/devices/locationData";
    private static final String wsUrl = "ws://localhost:4201";
    private static final String userAgent = "Mozilla/5.0";
    private static final String urlParameters = "";
    private static final int enqueueDelayMs = 0;
    private static final int sendSpeedMs = 50;
    private static final int retryConnectionMs = 1000;
    private static final int queueEmptySize = 1000;

    private boolean noConnect = false;
    private boolean webSocketConnected = false;
    private Thread serverThread;
    private boolean isRunning = false;
    private long lastEnqueue = 0;
    private long lastSend = 0;

    private WebSocketClient client;

    public Server() {
        serverThread = new Thread(this);
        serverThread.start();
        isRunning = true;
        connectWebsocket();

    }

    private void connectWebsocket() {
        try {
            if (client != null && !webSocketConnected) {
                client.close();
                client = null;
            }
            client = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    webSocketConnected = true;
                }

                public void onMessage(String s) {
                }

                public void onClose(int i, String s, boolean b) {
                    webSocketConnected = false;
                }

                public void onError(Exception e) {
                    webSocketConnected = false;
                    client.close();
                }
            };
            client.connect();
        } catch (URISyntaxException e) {
            client.close();
        }
    }


    private void sendData(String data, String urlParameters) throws InterruptedException {
        // if the web socket isn't connected then fallback to AJAX
        if (webSocketConnected) {
            sendDataWebSocket(data);
            return;
        }
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
            connection.disconnect();
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                noConnect = true;
                System.out.println("FAILED CANNOT CONNECT TO API");
            } else {
                e.printStackTrace();
            }
        }
        if (!webSocketConnected && !noConnect) {
            connectWebsocket();
        }
    }

    private void sendDataWebSocket(String data) {
        if (data != null) {
            System.out.println("Websocket sent: " + data);
            client.send(data);
        }
    }

    public void putOnQueue(String data) {
        try {
            if (toSend.size() > queueEmptySize) {
                toSend.clear();
            }
            toSend.put(data);
            latest = data;
            lastEnqueue = System.currentTimeMillis();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean ready() {
        int currentDelay = (noConnect ? retryConnectionMs : sendSpeedMs) + enqueueDelayMs;
        return (lastEnqueue + currentDelay < System.currentTimeMillis());
    }

    public void shutdown() {
        toSend.clear();
        isRunning = false;
    }

    public void start() {
        isRunning = true;
        if (!serverThread.isAlive()) {
            serverThread = new Thread(this);
            serverThread.start();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void run() {
        while (isRunning) {
            int currentDelay;
            if (webSocketConnected) {
                currentDelay = 0;
            } else {
                currentDelay = noConnect ? retryConnectionMs : sendSpeedMs;
            }

            if (lastSend + currentDelay >= System.currentTimeMillis()) {
                continue;
            }

            try {
                String data = latest;

                if (data != "") {
                    sendData(data, urlParameters);
                }
                lastSend = System.currentTimeMillis();
                latest = "";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
