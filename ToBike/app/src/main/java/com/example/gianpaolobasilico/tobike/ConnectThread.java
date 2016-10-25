package com.example.gianpaolobasilico.tobike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread implements Runnable{

    private final BluetoothSocket bluetoothSocket;
    private final BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isConnected=false;

    public ConnectThread(BluetoothDevice bd, UUID uuid, BluetoothAdapter bluetoothAdapter){
        BluetoothSocket tmp=null;
        this.bluetoothDevice=bd;
        this.bluetoothAdapter=bluetoothAdapter;
        try {
            tmp=bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothSocket=tmp;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        bluetoothAdapter.cancelDiscovery();
        try {
            bluetoothSocket.connect();
            Log.i("Connessione attempt ","connected");

        } catch (IOException e) {
            e.printStackTrace();
            try {
                bluetoothSocket.close();
                isConnected=false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        isConnected=true;
    }

    public  void  cancel(){
        try {
            bluetoothSocket.close();
            isConnected=false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String msg){
        byte[] msgBuffer=msg.getBytes();
        ConnectedThread connectedThread=new ConnectedThread(bluetoothSocket);
        connectedThread.write(msgBuffer);
        Log.i("senddata",msg);

    }

    public boolean BtConnected(){
        return isConnected;
    }


}
