package com.julind.esenseUtils;

import android.util.Log;

import com.julind.esense.Direction;
import com.julind.esense.EarableConnected;
import com.julind.esenseUtils.mediaControls.MediaControlService;
import com.julind.esenseUtils.rotationDetector.Movement;
import com.julind.esenseUtils.rotationDetector.RotationDetectorService;

import io.esense.esenselib.ESenseConfig;
import io.esense.esenselib.ESenseEvent;
import io.esense.esenselib.ESenseSensorListener;


public class SensorListener implements ESenseSensorListener {
    private static final String TAG = "SensorListener";
    private boolean firstNone = false;
    private boolean firstTraining = false;

    private final EarableConnected earableConnectedFragment;
    private final RotationDetectorService rotationDetectorService;
    private final ESenseConfig config;

    public SensorListener(ESenseConfig config, EarableConnected earableConnectedFragment, int sampleRate) {
        this.config = config;
        this.earableConnectedFragment = earableConnectedFragment;
        this.rotationDetectorService =
                new RotationDetectorService(10, (int) Math.floor(sampleRate * 0.2));
    }

    @Override
    public void onSensorChanged(ESenseEvent event) {
        Movement movement =
                this.rotationDetectorService.calculateMovement(event.convertGyroToDegPerSecond(this.config), event.getTimestamp());

        switch (movement) {
            case TRAINING:
                if (!this.firstTraining) {
                    Log.i(TAG, "onSensorChanged: Gathering Baseline Recording ... ");
                    this.firstTraining = true;
                }
                break;
            case NONE:
                if (!this.firstNone) {
                    Log.i(TAG, "onSensorChanged: Baseline Recorded");
                    this.firstNone = true;
                }
                break;
            case ROTATION_LEFT:
                MediaControlService.prevMedia();
                this.earableConnectedFragment.activateDirection(Direction.LEFT);
                break;
            case ROTATON_RIGHT:
                MediaControlService.nextMedia();
                this.earableConnectedFragment.activateDirection(Direction.RIGHT);
                break;
            case ROTATION_UP:
                MediaControlService.volumeUp();
                this.earableConnectedFragment.activateDirection(Direction.UP);
                break;
            case ROTATION_DOWN:
                MediaControlService.volumeDown();
                this.earableConnectedFragment.activateDirection(Direction.DOWN);
                break;
            default:
                Log.i(TAG, "onSensorChanged: WTF?");
        }
    }
}
