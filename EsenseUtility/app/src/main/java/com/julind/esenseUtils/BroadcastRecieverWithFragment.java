package com.julind.esenseUtils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.location.LocationManagerCompat;

public class BroadcastRecieverWithFragment<T extends NotifiableFragment> extends BroadcastReceiver {
    private final T fragment;

    public BroadcastRecieverWithFragment(T fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            this.fragment.broadcastedBluetootState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
            return;
        }
        if (LocationManager.MODE_CHANGED_ACTION.equals(action)) {
            this.fragment.broadcastedGPSState(
                    LocationManagerCompat.isLocationEnabled(
                            (LocationManager) context.getSystemService(Context.LOCATION_SERVICE)
                    )
            );
        }
    }
}
