package com.julind.esenseUtils;

import com.julind.esense.MainActivity;

import java.util.TimerTask;

public class OnMainUiThreadTimer extends TimerTask {

    private final MainActivity mainActivity;
    private final Runnable runnable;

    public OnMainUiThreadTimer(MainActivity mainActivity, Runnable runnable) {
        this.mainActivity = mainActivity;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        this.mainActivity.runOnUiThread(this.runnable);
    }
}
