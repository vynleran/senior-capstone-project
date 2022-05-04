package com.example.seizuredetectionapp.profile.blecallbacks;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class BleAccXDataCallback implements ProfileDataCallback, DataSentCallback, BleAccXCallback {

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        parse(device, data);
    }

    @Override
    public void onDataSent(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        parse(device, data);
    }

    public void parse(@NonNull final BluetoothDevice device, @NonNull final Data data) {
//        if (data.size() != 1) {
//            onInvalidDataReceived(device, data);
//            return;
//        }

        final byte[] bytes = data.getValue();
        Log.d("value", String.valueOf(bytes));
        String newString = new String(bytes, StandardCharsets.UTF_8);
        onAccXValChanged(device, newString);
    }
//    public String getData() {
//        parse(@NonNull final BluetoothDevice device, @NonNull final Data data)
//        return ;
//    }
}
