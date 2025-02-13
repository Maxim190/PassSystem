package com.example.facedetector.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.facedetector.model.employee.Employee;
import com.example.facedetector.model.employee.IndexedEmployee;
import com.example.facedetector.model.employee.NotIndexedEmployee;
import com.example.facedetector.ui.authorization.AuthorizationHandler;
import com.example.facedetector.ui.authorization.AuthorizationPresenter;
import com.example.facedetector.utils.Consts;
import com.example.facedetector.utils.Cryptography;
import com.example.facedetector.utils.JSONManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkService {

    public static final String DEFAULT_HOST_IP = "192.168.0.103";
    private static String BUFFER_HOST_IP = "";
    private static final int DEFAULT_PORT = 8000;

    private static NetworkService intent;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private Cryptography coder;
    private List<ConnectionStatusListener> statusListeners;
    private List<ConnectionLogListener> logListeners;
    private Thread connectionThread;
    private long lastConnectionCheckTime;
    private boolean isConnected = false;

    public static NetworkService getIntent() {
        if (intent == null) {
            intent = new NetworkService();
        }
        return intent;
    }

    private NetworkService() {
        coder = new Cryptography();
    }

    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        if (statusListeners == null) {
            statusListeners = new ArrayList<>();
        }
        statusListeners.add(listener);
    }

    public void deleteConnectionStatusListener(ConnectionStatusListener listener) {
        if (logListeners != null) {
            logListeners.remove(listener);
        }
    }

    public void setLogListener(ConnectionLogListener listener) {
        if (logListeners == null) {
            logListeners = new ArrayList<>();
        }
        logListeners.add(listener);
    }

    public void disconnect() {
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
        if (socket != null) {
            disconnect();
        }
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        return socket.isConnected();
    }

    private void interruptConnectionThread() {
        if (connectionThread == null) {
            return;
        }
        connectionThread.interrupt();
    }

    public void connect() {
        if (!BUFFER_HOST_IP.isEmpty()) {
            connect(BUFFER_HOST_IP);
        }
        else {
            connect(DEFAULT_HOST_IP);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void connect(String host) {
        interruptConnectionThread();
        connectionThread = new Thread(() -> {
            try {
                log( "Connecting to " + host + ":" + DEFAULT_PORT);
                setConnectionStatus(createSocket(host, DEFAULT_PORT));
            } catch (IOException e) {
                setConnectionStatus(false);
                log("Failed to connect " + host + ":" + DEFAULT_PORT);
                return;
            }

            int attempts = 1;
            while (!Thread.interrupted()) {
                try {
                    if (!isConnected && !Thread.interrupted()) {
                        log("Reconnect to " + host + ":" + DEFAULT_PORT + " Attempt:" + attempts);
                        setConnectionStatus(createSocket(host, DEFAULT_PORT));
                        AuthorizationHandler.setRightMode("");
                    } else {
                        BUFFER_HOST_IP = host;
                        exchangeDHParams();
                        checkConnectionUntilDisconnect();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    log(e.getMessage());
                    e.printStackTrace();
                }
                attempts++;
            }
        });
        connectionThread.start();
    }

    private void exchangeDHParams() {
        try {
            while (true) {
                log("Changing cryptography params with server...");

                byte[] body = "get_params".getBytes();
                String jsonHeader = JSONManager.dump(new HashMap<String, String>() {{
                    put("dh_params", String.valueOf(body.length));
                }});
                sendMsg(jsonHeader.getBytes(), body);

                Map<String, byte[]> msg = receiveMsg(false);
                if (Consts.CODE_ERROR.equals(msg.get(Consts.DATA_TYPE_CODE))) {
                    log(new String(msg.values().iterator().next()));
                    continue;
                }

                byte[] publicKey = coder.calcPublicKeyFromServerParams(msg);
                jsonHeader = JSONManager.dump(new HashMap<String, String>() {{
                    put("public_key", String.valueOf(publicKey.length));
                }});
                sendMsg(jsonHeader.getBytes(), publicKey);
                Log.i("PassSystem", socket.toString());
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionUntilDisconnect() throws InterruptedException {
        AtomicBoolean receivedMsg = new AtomicBoolean(false);
        while (!Thread.interrupted()) {
            if (!isConnected) {
                break;
            }
            long CHECK_CONNECTION_INTERVAL = 10000;
            if (lastConnectionCheckTime + CHECK_CONNECTION_INTERVAL < System.currentTimeMillis()) {
                log( "Check connection status");

                byte[] body = Consts.MSG_TYPE_CHECK.getBytes();
                Map<String, String> jsonHeader = new HashMap<String, String>(){{
                    put(Consts.MSG_TYPE_CHECK, String.valueOf(body.length));
                }};
                receivedMsg.set(false);
                Thread thread = exchange(jsonHeader, body, (response)-> {
                    if (!response.containsKey("ERROR"))
                        receivedMsg.set(true);
                });
                thread.join();
                setConnectionStatus(receivedMsg.get());
            }
            Thread.sleep(CHECK_CONNECTION_INTERVAL);
        }
    }

    private void log(String msg) {
        Log.i("PassSystem", msg);
        if (logListeners != null) {
            logListeners.forEach(i->{
                if (i != null) {
                    i.logCallback(msg);
                }
            });
        }
    }

    private void setConnectionStatus(boolean value) {
        if (isConnected != value && statusListeners != null) {
            statusListeners.forEach(i-> {
                if (i != null) {
                    i.connectionStatusChanged(value);
                }
            });
        }
        isConnected = value;
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

    private synchronized byte[] receiveBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        if (inputStream.read(bytes) == -1) {
            isConnected = false;
            throw new IOException("Failed receiving msg from server: no byte is available");
        }
        return bytes;
    }

    private synchronized byte[] receiveTotalBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        inputStream.readFully(bytes);
        return bytes;
    }

    private Map<String, byte[]> receiveMsg(boolean decrypt) throws IOException {
        Log.i("PassSystem", "Receiving....");
        String rawHeader = new String(
                decrypt? coder.decrypt(receiveBytes(120)): receiveBytes(120));
        String jsonHeader = rawHeader.substring(0, rawHeader.indexOf('}') + 1);
        Map<String, String> header = JSONManager.parse(jsonHeader);
        if (header == null) {
            throw new IOException("Failed receiving data from server");
        }
        int totalSize;
        if (decrypt) {
            totalSize = Integer.parseInt(header.get(Consts.DATA_TYPE_ENC));
            header.remove(Consts.DATA_TYPE_ENC);
        }
        else {
            totalSize = header.values()
                .stream()
                .mapToInt(Integer::parseInt)
                .sum();
        }

        byte[] body = decrypt? coder.decrypt(receiveTotalBytes(totalSize)) : receiveTotalBytes(totalSize);

        Map<String, byte[]> result = new LinkedHashMap<>();
        int iterator = 0;
        for (String key: header.keySet()) {
            int nextPart = Integer.parseInt(header.get(key));
            result.put(key, Arrays.copyOfRange(body, iterator, iterator + nextPart));
            iterator += nextPart;
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void sendMsg(byte[] jsonHeader, byte[] body) throws Exception {
        lastConnectionCheckTime = System.currentTimeMillis();
        if (jsonHeader.length < 120) {
            jsonHeader = mergeArrays(jsonHeader, String.join("",
                            Collections.nCopies((120 - jsonHeader.length), "_")).getBytes());
        }
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

        Map<String, String> jsonHeader = new HashMap<String, String>(){{
            put(msgType, String.valueOf(employeeData.length));
            put(Consts.DATA_TYPE_PHOTO, String.valueOf(employeePhoto.length));
        }};

        exchange(jsonHeader, mergeArrays(employeeData, employeePhoto), listener);
    }

    public void authorize(String login, String password, MsgListener listener) {
        if (login == null || password == null) {
            throw new NullPointerException("Authorisation Error: login or password is null");
        }

        byte[] body = JSONManager.dump(new HashMap<String, String>() {{
            put(Consts.DATA_TYPE_LOGIN, login);
            put(Consts.DATA_TYPE_PASSWORD, password);
        }}).getBytes();

        Map<String, String> jsonHeader = new HashMap<String, String>(){{
            put(Consts.MSG_TYPE_AUTHORIZE, String.valueOf(body.length));
        }};

        exchange(jsonHeader, body, listener);
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
        handleAndSend(Consts.MSG_TYPE_RECOGNIZE, photo, listener);
    }

    public void deleteEmployee(String id, MsgListener listener) {
        byte[] body = id.getBytes();
        handleAndSend(Consts.MSG_TYPE_DELETE, body, listener);
    }

    public void getAllDepartments(MsgListener listener) {
        byte[] body = Consts.DATA_TYPE_DEPARTMENT.getBytes();
        handleAndSend(Consts.MSG_TYPE_GET, body, listener);
    }

    public void getDepartmentPositions(String department, MsgListener listener) {
        byte[] body = department.getBytes();
        handleAndSend(Consts.MSG_TYPE_GET, body, listener);
    }

    private void handleAndSend(String msgType, byte[] body, MsgListener listener) {
        Map<String, String> jsonHeader = new HashMap<String, String>(){{
            put(msgType, String.valueOf(body.length));
        }};

        exchange(jsonHeader, body, listener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Thread exchange(Map<String, String> jsonHeader, byte[] body, MsgListener listener) {
        Thread thread = new Thread(()-> {
            try {
                lastConnectionCheckTime = System.currentTimeMillis();
                if (!isConnected) {
                    throw new IOException("There's no connection");
                }
                byte[] encBody = coder.encrypt(body);
                jsonHeader.put(Consts.DATA_TYPE_ENC, String.valueOf(encBody.length));

                sendMsg(coder.encrypt(JSONManager.dump(jsonHeader).getBytes()), encBody);
                listener.callback(receiveMsg(true));
            } catch (Exception e) {
                setConnectionStatus(false);
                e.printStackTrace();
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
