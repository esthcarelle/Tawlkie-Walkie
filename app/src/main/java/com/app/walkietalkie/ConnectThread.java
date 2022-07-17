package com.app.walkietalkie;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {
    private BluetoothDevice bDevice;
    private BluetoothSocket bSocket;
    private BluetoothSocket fallbackSocket;

    // Establish connection
    public boolean connect(BluetoothDevice device, UUID UUID, Activity activity) {

        // Get the MAC address
        bDevice = device;

        try {
            // Create a RFCOMM socket with the UUID
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

            bSocket = createBluetoothSocket(bDevice,UUID,activity);
            System.out.println(bSocket);
        } catch (IOException e) {
            Log.d("CONNECT", "Failed at create RFCOMM");
            return false;
        }

        if (bSocket == null) {
            return false;
        }

        try {
            // Try to connect
            bSocket.connect();
        } catch (IOException e) {
            Log.d("CONNECT", "Failed at socket connect");
            Log.e("", e.getMessage());
            try {
                Log.e("", "trying fallback...");

                Class<?> clazz = bSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};

                fallbackSocket = (BluetoothSocket) m.invoke(bSocket.getRemoteDevice(), params);
                fallbackSocket.connect();

                Log.e("E", "Connected");
            } catch (Exception e2) {
                Log.e("Exception 2", "Couldn't establish Bluetooth connection!");
                return false;
            }
            // Moved return false out from inner catch, making it return false when connect is unsuccessful.
            // Return value used to determine if intent switch to next screen.

        }
        return true;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID UUID,Activity activity)
            throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation

        }
        return device.createRfcommSocketToServiceRecord(UUID);
    }
    // Close connection
    public boolean closeConnect() {
        try {
            bSocket.close();
        } catch(IOException e) {
            Log.d("CONNECT", "Failed at socket close");
            return false;
        }
        return true;
    }

    // Returns the bluetooth socket object
    public BluetoothSocket getSocket() {
        return bSocket;
    }
}
