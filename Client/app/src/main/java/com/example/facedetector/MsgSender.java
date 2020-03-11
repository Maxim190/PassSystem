package com.example.facedetector;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

enum MsgType {
    RECOGNIZE,
    EDIT,
    ADD,
    CHECK,
    ADDITIONAL_PHOTO
}
enum DataType {
    PHOTO()
}

public class MsgSender{

    private String host;
    private int port;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private Thread connectionThread;

    private MsgListener msgListener;

    private long lastConnectionCheckTime;
    private final long CHECK_CONNECTION_INTERVAL = 5000;
    private boolean isConnected = false;

    MsgSender(MsgListener msgListener) {
        this.msgListener = msgListener;
        //connect();

    }

    public void disconnect() {
        if (socket == null) {
            return;
        }
        try {
            connectionThread.interrupt();
            socket.close();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createSocket(String host, int port) throws IOException{
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        return socket.isConnected();
    }

    public void connect(String host, int port) {
        disconnect();
        connectionThread = new Thread(() -> {
            int attempts = 1;
            while (!Thread.interrupted()) {
                Log.e("PassSystem", "Interrupted1: " + Thread.interrupted());
                Log.i("PassSystem", "Connect to " + host + ":" + port + " Attempt:" + attempts);
                try {
                    isConnected = createSocket(host, port);
                    lastConnectionCheckTime = System.currentTimeMillis();
                    if (isConnected) {
                        checkConnectionStatus();
                    }
                } catch (NoRouteToHostException e) {
                    msgListener.receiveMsg("Server " + host + " unreachable");
                } catch (IOException e) {
                    msgListener.receiveMsg("failed to connect to "  + host);
                }

                try {
                    Thread.sleep(CHECK_CONNECTION_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    break;
                }
                attempts++;
            }
            Log.e("PassSystem", "Interrupted " + host);
        });
        connectionThread.start();
    }

    private void checkConnectionStatus() {
        while (!Thread.interrupted()) {
            Log.e("PassSystem", "Interrupted2: " + Thread.interrupted());
            if (!isConnected) {
                break;
            }
            if (lastConnectionCheckTime + CHECK_CONNECTION_INTERVAL < System.currentTimeMillis()) {
                Log.i("PassSystem", "Checking:SendMsg");
                if(sendMsgByParts(new TreeMap<>(Collections.singletonMap( MsgType.CHECK, "{CHECK=0}".getBytes())))) {
                    Log.i("PassSystem", "Checking:ReceiveMsg");
                    byte[] bytes = receiveMsg();
                    isConnected = bytes != null;
                } else {
                    isConnected = false;
                }
                Log.i("PassSystem", "(checking) isConnected:" + isConnected);
            }
            try {
                Log.i("PassSystem", "Checking:sleeping");
                Thread.sleep(CHECK_CONNECTION_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                isConnected = false;
                break;
            }
        }
    }

    public void exchangeMessages(TreeMap<MsgType, byte[]> myMsg) {
        new Thread(() -> {
            if (!isConnected) {
                msgListener.receiveMsg("Couldn't send msg. There's no connection");
                return;
            }
            Log.i("PassSystem", "Sending msg " + myMsg);
            msgListener.receiveMsg("Sending msg to server...");
            sendMsgByParts(myMsg);
            msgListener.receiveMsg("Waiting server's answer");
            byte[] receivedBytes = receiveMsg();
            String receivedString = receivedBytes == null ? "Failed to receive msg" : new String(receivedBytes);
            msgListener.receiveMsg(receivedString);
        }).start();
    }

    private void sendMsg(byte[] msg, MsgType type, String additionalInf) throws IOException {
        outputStream.write((additionalInf + type.toString() + ":" + msg.length).getBytes());
        outputStream.flush();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outputStream.write(msg);
        outputStream.flush();
    }

    private boolean sendMsgByParts(TreeMap<MsgType, byte[]> msgParts) {
        String lastPartFlag = "FINAL";
        for(MsgType key : msgParts.keySet()) {
            try {
                Log.i("PassSystem", new String(msgParts.get(key)));
                sendMsg(msgParts.get(key), key, msgParts.lastKey().equals(key)? lastPartFlag: "");
                Thread.sleep(100);
            } catch (IOException e) {
                Log.e("PassSystem", "Couldn't send msg");
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private byte[] receiveMsg() {
        byte[] msg;
        try {
            lastConnectionCheckTime = System.currentTimeMillis();
            int available = 0;
            while (available == 0) {
                available = inputStream.available();
                if (lastConnectionCheckTime - System.currentTimeMillis() > CHECK_CONNECTION_INTERVAL) {
                    throw new TimeoutException();
                }
            }
            msg = new byte[available];
            inputStream.read(msg);
            return msg;
        } catch (IOException e) {
            msg = "Receive msg: failed".getBytes();
            Log.e("PassSystem", new String(msg));
            e.printStackTrace();
        } catch (TimeoutException e) {
            msg = "Receive msg :timed out".getBytes();
            Log.e("PassSystem", new String(msg));
            e.printStackTrace();
        }
        return msg;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
