package com.julind.esenseUtils;

import android.util.Log;

import com.julind.esense.MainActivity;
import com.julind.esense.NoEarableConnected;

import io.esense.esenselib.ESenseConnectionListener;
import io.esense.esenselib.ESenseManager;

import static android.content.ContentValues.TAG;

public class ConnectionManager implements ESenseConnectionListener {

    private final NoEarableConnected noEarableFragment;
    private final MainActivity mainActivity;

    public ConnectionManager(NoEarableConnected noEarableFragment, MainActivity mainActivity) {
        this.noEarableFragment = noEarableFragment;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onDeviceFound(ESenseManager manager) {
        Log.i(TAG, "Device Found!");
    }

    @Override
    public void onDeviceNotFound(ESenseManager manager) {
        this.noEarableFragment.notFound();
    }

    @Override
    public void onConnected(ESenseManager manager) {
        mainActivity.onConnectionSuccess();
    }

    @Override
    public void onDisconnected(ESenseManager manager) {
        this.mainActivity.disconnect();
    }
}