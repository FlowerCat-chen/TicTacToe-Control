package com.flowercat.chessControl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.io.Serializable;

public class BluetoothService implements Serializable{
    // 常量
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int STATE_NONE = 0;       // 未连接
    public static final int STATE_CONNECTING = 1; // 正在连接
    public static final int STATE_CONNECTED = 2;  // 已连接
	public static final int STATE_DISCONNECTED = 3;  // 断开连接
	
	
	
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // 成员变量
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothServiceThread bluetoothServiceThread;
    private int state;
    // 构造函数
    public BluetoothService(Handler handler) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.state = STATE_NONE;
        this.handler = handler;
    }
    // 设置当前状态
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;
        handler.obtainMessage(BluetoothService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
		// 如果当前状态是断开连接，则关闭连接线程和传输线程
        if (state == STATE_DISCONNECTED) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
            if (connectedThread != null) {
                connectedThread.cancel();
                connectedThread = null;
            }
        }
    }
    // 获取当前状态
    public synchronized int getState() {
        return state;
    }
    // 开始连接
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);
        // 如果正在连接，先关闭连接线程
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }
        // 如果已经连接，先关闭连接线程
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        // 如果蓝牙服务线程不为空，关闭它
        if (bluetoothServiceThread != null) {
            bluetoothServiceThread.cancel();
            bluetoothServiceThread = null;
        }
        // 开始连接线程
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }
    // 开始连接
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");
        // 如果正在连接，先关闭连接线程
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        // 如果已经连接，先关闭连接线程
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        // 如果蓝牙服务线程不为空，关闭它
        if (bluetoothServiceThread != null) {
            bluetoothServiceThread.cancel();
            bluetoothServiceThread = null;
        }
        // 开始蓝牙服务线程
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        // 向UI发送连接成功的消息
        Message msg = handler.obtainMessage(BluetoothService.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }
    // 断开连接
    public synchronized void disconnect() {
        if (D) Log.d(TAG, "disconnect");
        // 关闭连接线程
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        // 关闭传输线程
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        // 关闭蓝牙服务线程
        if (bluetoothServiceThread != null) {
            bluetoothServiceThread.cancel();
            bluetoothServiceThread = null;
        }
        setState(STATE_NONE);
    }
    // 发送数据
    public synchronized void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        r.write(out);
    }
    // 发送Toast消息
    private void showToast(String message) {
        Message msg = handler.obtainMessage(BluetoothService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.TOAST, message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
    // 连接线程
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            socket = tmp;
        }
        public void run() {
            Log.i(TAG, "BEGIN connectThread");
            setName("ConnectThread");
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                // 连接失败关闭socket
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // 发送Toast消息
                showToast("Unable to connect device");
                setState(STATE_NONE);
                return;
            }
            // 重置连接线程
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
            // 连接成功，开始蓝牙服务线程
            connected(socket, device);
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    // 传输线程
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }
        public void run() {
            Log.i(TAG, "BEGIN connectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // 连接断开，发送Toast消息
                    showToast("Device connection was lost");
                    setState(STATE_NONE);
                    break;
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                handler.obtainMessage(BluetoothService.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    // 蓝牙服务线程
    private class BluetoothServiceThread extends Thread {
        public void run() {
            Log.i(TAG, "BEGIN bluetoothServiceThread");
            setName("BluetoothServiceThread");
            while (state == STATE_CONNECTED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "bluetoothServiceThread interrupted", e);
                }
            }
        }
        public void cancel() {
            if (D) Log.d(TAG, "bluetoothServiceThread cancel");
            interrupt();
        }
    }
}
