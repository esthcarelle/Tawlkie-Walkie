package com.app.walkietalkie;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ListenThread {

    private static final String TAG =  "ListenThread";
    private BluetoothSocket listenSocket;
    private byte[] buffer;

    // Accept connection and create socket object
    public boolean acceptConnect(BluetoothAdapter adapter, UUID mUUID, Activity activity) {
        BluetoothServerSocket temp = null;
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return false;
                }

            }

            temp = adapter.listenUsingRfcommWithServiceRecord("BTService", mUUID);
            System.out.println(temp+" bt service");
        } catch(IOException e) {
            Log.e("LISTEN", "Error at listen using RFCOMM");
        }

        try {
            listenSocket = temp.accept();
            System.out.println(listenSocket +" listen socket");
        } catch (IOException e) {
            Log.e("LISTEN", "Error at accept connection");
        }
        if (listenSocket != null) {
            try {
                temp.close();
            } catch (IOException e) {
                Log.d("LISTEN", "Error at socket close");
            }
            return true;
        }
        return false;
    }

    // Close connection
    public boolean closeConnect() {
        try {
            listenSocket.close();
        } catch(IOException e) {
            Log.d("LISTEN", "Failed at socket close");
            return false;
        }
        return true;
    }

    // Return socket object
    public BluetoothSocket getSocket() {
        return listenSocket;
    }

}
