package com.example.facedetector.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.interfaces.MsgListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

public class MsgSender{

    private static MsgSender msgSender;

    private String host;
    private int port;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private Thread connectionThread;

    private ArrayList<MsgListener> msgListeners;

    private long lastConnectionCheckTime;
    private final long CHECK_CONNECTION_INTERVAL = 5000;
    private boolean isConnected = false;

    public static MsgSender getIntent() {
        if (msgSender == null) {
            msgSender = new MsgSender();
        }
        return msgSender;
    }

    private MsgSender() {}

    public void setListener(MsgListener listener) {
        if (msgListeners == null) {
            msgListeners = new ArrayList<>();
        }
        msgListeners.add(listener);
    }

    public void deleteListener(MsgListener listener) {
        if (msgListeners == null) {
            return;
        }
        msgListeners.remove(listener);
    }

    public void disconnect() {
        interruptConnectionThread();
        if (socket == null) {
            return;
        }
        try {
            Log.e("PassSystem", "beforeSocket");
            socket.close();
            inputStream.close();
            outputStream.close();
            isConnected = false;
            Log.e("PassSystem", "afterSocket");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        return socket.isConnected();
    }

    private void interruptConnectionThread() {
        try {
            if (connectionThread == null) {
                return;
            }
            connectionThread.interrupt();
            Log.e("PassSystem", "before");
            connectionThread.join();
            Log.e("PassSystem", "after");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connect(String host, int port) {
        disconnect();
        Log.e("PassSystem", "*");
        connectionThread = new Thread(() -> {
            try {
                Log.i("PassSystem", "Connecting to " + host + ":" + port);
                isConnected = createSocket(host, port);
                if (!isConnected) {
                    sendMsgToListeners("Failed to connect " + host + ":" + port);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int attempts = 1;
            while (!Thread.interrupted()) {
                try {
                    if (!isConnected && !Thread.interrupted()) {
                        Log.i("PassSystem", "Reconnect to " + host + ":" + port + " Attempt:" + attempts);
                        isConnected = createSocket(host, port);
                        lastConnectionCheckTime = System.currentTimeMillis();
                    } else {
                        checkConnectionUntilDisconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    break;
                }
                attempts++;
            }
        });
        connectionThread.start();
    }

    private void checkConnectionUntilDisconnect() throws InterruptedException {
        while (!Thread.interrupted()) {
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
                Log.i("PassSystem", "(checking) isConnected:");
            }
            Log.i("PassSystem", "Checking:sleeping");
            Thread.sleep(CHECK_CONNECTION_INTERVAL);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void exchangeMessages(TreeMap<MsgType, byte[]> myMsg) {
        new Thread(() -> {
            if (!isConnected) {
                sendMsgToListeners("Couldn't send msg. There's no connection");
                return;
            }
            Log.i("PassSystem", "Sending msg " + myMsg);
            sendMsgByParts(myMsg);
            Log.i("PassSystem", "Waiting server's answer ");
            byte[] receivedBytes = receiveMsg();
            String receivedString = receivedBytes == null ? "Failed to receive msg" : new String(receivedBytes);
            sendMsgToListeners(receivedString);
        }).start();
    }

    private synchronized void sendMsg(byte[] msg, MsgType type, String additionalInf) throws IOException {
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

    private synchronized boolean sendMsgByParts(TreeMap<MsgType, byte[]> msgParts) {
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

    private synchronized byte[] receiveMsg() {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendMsgToListeners(String msg) {
        msgListeners.forEach(l -> l.receiveMsg(msg));
    }

    public boolean isConnected() {
        return isConnected;
    }
}
