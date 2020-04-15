package com.example.facedetector.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.employee.Employee;
import com.example.facedetector.model.employee.IndexedEmployee;
import com.example.facedetector.model.employee.NotIndexedEmployee;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.JSONManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkService {

    private static NetworkService msgSender;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private Thread connectionThread;

    private long lastConnectionCheckTime;
    private boolean isConnected = false;

    public static NetworkService getIntent() {
        if (msgSender == null) {
            msgSender = new NetworkService();
        }
        return msgSender;
    }

    private NetworkService() {}

    public void disconnect() {
        interruptConnectionThread();
        if (socket == null) {
            return;
        }
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            isConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(10000);
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
            connectionThread.join();
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
                    Log.e("PassSystem","Failed to connect " + host + ":" + port);
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
        AtomicBoolean receivedMsg = new AtomicBoolean(false);
        while (!Thread.interrupted()) {
            if (!isConnected) {
                break;
            }
            long CHECK_CONNECTION_INTERVAL = 15000;
            if (lastConnectionCheckTime + CHECK_CONNECTION_INTERVAL < System.currentTimeMillis()) {
                Log.i("PassSystem", "Check connection status");

                byte[] body = Consts.MSG_TYPE_CHECK.getBytes();
                String jsonHeader = JSONManager.dump(new HashMap<String, String>(){{
                    put(Consts.MSG_TYPE_CHECK, String.valueOf(body.length));
                }});

                //receivedMsg.set(false);
//                Thread thread = exchange(jsonHeader.getBytes(), body, (response)-> {
//                    if (response != null && response.containsKey(Consts.MSG_TYPE_CHECK))
//                        receivedMsg.set(true);
//                });
//                thread.join();
//                isConnected = receivedMsg.get();
            }
            Thread.sleep(CHECK_CONNECTION_INTERVAL);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    private byte[] mergeArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private byte[] receiveBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        if (inputStream.read(bytes) == -1) {
            isConnected = false;
            throw new IOException("Failed receiving msg from server");
        }
        return bytes;
    }

    private byte[] receiveTotalBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        inputStream.readFully(bytes);
        return bytes;
    }

    private Map<String, byte[]> receiveMsg() throws IOException {
        String rawHeader = new String(receiveBytes(64));
        String jsonHeader = rawHeader.substring(0, rawHeader.indexOf('}') + 1);
        Map<String, String> header = JSONManager.parse(jsonHeader);
        Log.e("PassSystem", "Header: " + header.toString());
        int totalSize = header.values()
                .stream()
                .mapToInt(Integer::parseInt)
                .sum();
        Log.e("PassSystem", "Total size: " + totalSize);
        byte[] body = receiveTotalBytes(totalSize);

        Map<String, byte[]> result = new LinkedHashMap<>();
        int iterator = 0;
        for (String key: header.keySet()) {
            int nextPart = Integer.parseInt(header.get(key));
            result.put(key, Arrays.copyOfRange(body, iterator, iterator + nextPart));
            iterator += nextPart;
        }
        Log.e("PassSystem", "RECEIVED:----------------------");
        result.forEach((key, value) -> Log.e("passSystem", "Key " + key + " " + new String(value)));
        return result;
    }

    private void sendMsg(byte[] jsonHeader, byte[] body) throws IOException {
        Log.e("PassSystem", "Sending header" +
                new String(jsonHeader) + " \nbody " + new String(body));

        outputStream.write(jsonHeader);
        outputStream.flush();
        outputStream.write(body);
        outputStream.flush();
    }

    private void handleAndSend(Employee employee, String msgType, MsgListener listener) {
        if (employee == null) {
            throw new NullPointerException("Failed sending msg: employee object is null");
        }
        byte[] employeeData = JSONManager.dump(employee.toHashMapExcludingPhoto()).getBytes();
        byte[] employeePhoto = employee.getPhoto();

        String jsonHeader = JSONManager.dump(new HashMap<String, String>(){{
            put(msgType, String.valueOf(employeeData.length));
            put(Consts.DATA_TYPE_PHOTO, String.valueOf(employeePhoto.length));
        }});

        exchange(jsonHeader.getBytes(), mergeArrays(employeeData, employeePhoto), listener);
    }

    public void addEmployee(NotIndexedEmployee notIndexedEmployee, MsgListener listener) {
        handleAndSend(notIndexedEmployee, Consts.MSG_TYPE_ADD, listener);
    }

    public void editEmployee(IndexedEmployee indexedEmployee, MsgListener listener) {
        handleAndSend(indexedEmployee, Consts.MSG_TYPE_EDIT, listener);
    }

    public void recognizeEmployee(byte[] photo, MsgListener listener) {
        if (photo == null || photo.length == 0) {
            throw new NullPointerException("Failed recognize employee: photo is missing");
        }
        String jsonHeader = JSONManager.dump(new HashMap<String, String>(){{
            put(Consts.MSG_TYPE_RECOGNIZE, String.valueOf(photo.length));
        }});

        exchange(jsonHeader.getBytes(), photo, listener);
    }

    public void deleteEmployee(String id, MsgListener listener) {
        byte[] body = id.getBytes();
        String jsonHeader = JSONManager.dump(new HashMap<String, String>(){{
            put(Consts.MSG_TYPE_DELETE, String.valueOf(body.length));
        }});

        exchange(jsonHeader.getBytes(), body, listener);
    }

    private Thread exchange(byte[] jsonHeader, byte[] body, MsgListener listener) {
        Thread thread = new Thread(()->{
            try {
                lastConnectionCheckTime = System.currentTimeMillis();
                if (!isConnected) {
                    throw new IOException("There's no connection");
                }
                sendMsg(jsonHeader, body);
                listener.callback(receiveMsg());
            } catch (IOException e) {
                listener.callback(buildErrorMsg(e.getMessage()));
            }
        });
        thread.start();
        return thread;
    }

    private Map<String, byte[]> buildErrorMsg(String msg) {
        return new LinkedHashMap<String, byte[]>(){{
            put("ERROR", msg != null?
                    msg.getBytes(): "Unknown error".getBytes());
            put(Consts.DATA_TYPE_CODE, Consts.CODE_ERROR.getBytes());
        }};
    }
}
