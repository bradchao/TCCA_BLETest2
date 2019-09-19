package com.example.mybletest2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private MyLeScanCallback myLeScanCallback;

    private BluetoothLeScanner bluetoothLeScanner;
    private MyScanCallback myScanCallback;

    private BluetoothDevice bluetoothDevice;
    private boolean isScan;
    private MyGattCallback myGattCallback;

    private BluetoothGatt bluetoothGatt;

    private UUID uuidNotify = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    9487);
        }else{
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9487){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // OK
                init();
            }else{
                finish();
            }
        }
    }


    private void init(){
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void enableBle(View view) {
        if (!bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 123);
        }
    }

    public void disableBle(View view) {
        if (bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK){

        }
    }

    public void scanDevices(View view) {
        if (bluetoothAdapter.isEnabled()){
            //myLeScanCallback = new MyLeScanCallback();
            //bluetoothAdapter.startLeScan(myLeScanCallback);
            Log.v("brad", "scanDevices");
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            myScanCallback = new MyScanCallback();
            bluetoothLeScanner.startScan(myScanCallback);
            isScan = true;

        }
    }

    public void stopScanDevices(View view) {
        if (bluetoothLeScanner != null){
            bluetoothLeScanner.stopScan(myScanCallback);
        }
    }

    private class MyScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            //Log.v("brad", device.getAddress());

//            if (isScan && device.getName() != null && device.getName().contains("Brad")){
//                isScan = false;
//                bluetoothDevice = device;
//                stopScanDevices(null);
//                connectDevice();
//            }

            if (isScan && device.getType() == BluetoothDevice.DEVICE_TYPE_LE &&
                device.getName() != null
                    && device.getName().contains("Brad")) {
                Log.v("brad", device.getName() + "=> " + device.getAddress());

                isScan = false;
                bluetoothDevice = device;
                //stopScanDevices(null);
                connectDevice();
            }

        }
    }

    private void connectDevice(){
        Log.v("brad", "start connect...");

        myGattCallback = new MyGattCallback();
        bluetoothGatt = bluetoothDevice.connectGatt(
                this, false, myGattCallback);


    }

    private class MyGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("brad", "newState = " +newState);
            if (newState == BluetoothProfile.STATE_CONNECTING){
                Log.v("brad", "ing...");
            }else if (newState == BluetoothProfile.STATE_CONNECTED){
                Log.v("brad", "ed");

                gatt.discoverServices();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.v("brad", status + ":" + BluetoothGatt.GATT_SUCCESS);
            if (status == BluetoothGatt.GATT_SUCCESS){
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services){
                    if (service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY){
                        Log.v("brad", service.getUuid().toString());
                    }

                    List<BluetoothGattCharacteristic> charts =  service.getCharacteristics();
                    for (BluetoothGattCharacteristic chart : charts){
                        if (chart.getProperties() == BluetoothGattCharacteristic.PROPERTY_READ){
                            Log.v("brad", "read:" + chart.getUuid().toString());
                        }else if (chart.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE){
                            Log.v("brad", "write:" + chart.getUuid().toString());
                        }else if (chart.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                            Log.v("brad", "notify:" + chart.getUuid().toString());
                        }else if (chart.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY + BluetoothGattCharacteristic.PROPERTY_READ){
                            Log.v("brad", "R+N:" + chart.getUuid().toString());
                            gatt.setCharacteristicNotification(chart,true);
                        }else {
                            Log.v("brad", "other:" + chart.getProperties());
                        }
                    }

                }
            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            for (byte v : value){
                Log.v("brad", "value => " + v);
            }

        }
    }






    private class MyLeScanCallback
            implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            String mac = bluetoothDevice.getAddress();
            String name = bluetoothDevice.getName();
            Log.v("brad", mac +"; " +name);
        }
    }

}
