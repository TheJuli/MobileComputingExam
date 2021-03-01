package com.julind.esenseUtils;

import android.util.Log;

import com.julind.esense.Direction;
import com.julind.esense.EarableConnected;
import com.julind.esenseUtils.mediaControls.MediaControlService;

import io.esense.esenselib.ESenseConfig;
import io.esense.esenselib.ESenseEventListener;
import io.esense.esenselib.ESenseManager;

public class EventListener implements ESenseEventListener {

    private static final String TAG = "ESenseEventListener";
    private final ESenseManager manager;
    private final EarableConnected earableConnectedFragment;
    private final int sampleRate;

    public EventListener(ESenseManager manager, EarableConnected earableConnectedFragment, int sampleRate) {
        this.manager = manager;
        this.earableConnectedFragment = earableConnectedFragment;
        this.sampleRate = sampleRate;
    }

    @Override
    public void onBatteryRead(double voltage) {
        Log.i(TAG, "onBatteryRead: " + voltage + "V");
    }

    @Override
    public void onButtonEventChanged(boolean pressed) {
        if (pressed) {
            MediaControlService.playPause();
            this.earableConnectedFragment.activateDirection(Direction.CENTER);
        }
    }

    @Override
    public void onAdvertisementAndConnectionIntervalRead(int minAdvertisementInterval, int maxAdvertisementInterval, int minConnectionInterval, int maxConnectionInterval) {

    }

    @Override
    public void onDeviceNameRead(String deviceName) {

    }

    @Override
    public void onSensorConfigRead(ESenseConfig config) {
        Log.i(TAG, "onSensorConfigRead: Config Read!");
        this.manager.registerSensorListener(new SensorListener(config, this.earableConnectedFragment, sampleRate), sampleRate);
    }

    @Override
    public void onAccelerometerOffsetRead(int offsetX, int offsetY, int offsetZ) {
    }
}
