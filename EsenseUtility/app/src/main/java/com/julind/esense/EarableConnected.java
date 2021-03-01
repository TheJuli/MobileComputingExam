package com.julind.esense;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.julind.esenseUtils.EventListener;
import com.julind.esenseUtils.OnMainUiThreadTimer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.esense.esenselib.ESenseManager;

public class EarableConnected extends Fragment {

    private MainActivity mainActivity;
    private ESenseManager manager;
    private TextView earableNamePlaceholder;
    private ImageView upImage;
    private ImageView downImage;
    private ImageView leftImage;
    private ImageView rightImage;
    private ImageView centerImage;
    private ImageView pulsator;
    private Timer pulsatingTimer;
    private ExtendedFloatingActionButton disconnectFab;

    public EarableConnected() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View connectedView = inflater.inflate(R.layout.fragment_earable_connected, container, false);
        this.earableNamePlaceholder = connectedView.findViewById(R.id.earableNamePlaceholder);

        this.pulsator = connectedView.findViewById(R.id.pulsator);

        this.upImage = connectedView.findViewById(R.id.upImage);
        this.downImage = connectedView.findViewById(R.id.downImage);
        this.leftImage = connectedView.findViewById(R.id.leftImage);
        this.rightImage = connectedView.findViewById(R.id.rightImage);
        this.centerImage = connectedView.findViewById(R.id.centerImage);

        this.disconnectFab = connectedView.findViewById(R.id.disconnect);


        SwitchCompat eventListenerToggle = connectedView.findViewById(R.id.eventListenerAttached);

        eventListenerToggle.setChecked(true);

        this.pulsatingTimer = new Timer();

        eventListenerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startEventListenerRegistering();
            } else {
                stopEventListenerRegistering();
            }
        });

        return connectedView;
    }

    private void pulsate() {
        final ArrayList<Double> visibilities = new ArrayList<>();

        for(double i = 0; i < Math.PI; i+= (Math.PI / 100)) {
            visibilities.add(i);
        }

        visibilities.add(Math.PI);

        final int[] visibilitiesSizeCounter = {0};

        Runnable runnable = () -> {
            this.pulsator.setAlpha((float) Math.sin(visibilities.get(visibilitiesSizeCounter[0])));

            if (visibilitiesSizeCounter[0] == visibilities.size() - 1) {
                visibilitiesSizeCounter[0] = 0;
            } else {
                visibilitiesSizeCounter[0]++;
            }
        };

        this.pulsatingTimer = new Timer();
        pulsatingTimer.scheduleAtFixedRate(new OnMainUiThreadTimer(this.mainActivity, runnable), 0, 25);
    }

    private void stopPulsate() {
        this.pulsatingTimer.cancel();
        this.pulsatingTimer.purge();
        this.pulsatingTimer = new Timer();
        pulsatingTimer.schedule(new OnMainUiThreadTimer(this.mainActivity, () -> pulsator.setAlpha(0f)), 0 );
    }

    public void registerMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private void startNotification() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("0", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = this.mainActivity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent appIntent = new Intent(getContext(), MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.mainActivity.getApplicationContext(), "0")
                .setSmallIcon(R.drawable.ic_baseline_hearing_24_flipped)
                .setContentTitle("eSense Media Controller is Running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(getContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManagerCompat.from(this.mainActivity.getApplicationContext()).notify(0, notificationBuilder.build());
    }

    public void registerManagerAndSetName(ESenseManager manager, String name) {
        this.manager = manager;
        this.earableNamePlaceholder.setText(name);

        this.disconnectFab.setOnClickListener((view) -> {
            this.mainActivity.disconnect();
        });
    }

    public void startEventListenerRegistering() {
        this.startNotification();
        this.pulsate();

        this.manager.registerEventListener(new EventListener(manager, this, 50));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                manager.getSensorConfig();
            }
        }, 3000);
    }

    public void stopEventListenerRegistering() {
        this.stopNotification();
        this.stopPulsate();
        try {
            this.manager.unregisterSensorListener();
            this.manager.unregisterEventListener();
        } catch (Exception e) {
            Log.i("EarableConnected", "stopEventListenerRegistering: " + e.toString());
        }
    }

    private void stopNotification() {
        NotificationManagerCompat.from(this.mainActivity.getApplicationContext()).cancel(0);
    }

    private void activateImageView(ImageView image) {
        ColorFilter filter = image.getColorFilter();

        this.mainActivity.runOnUiThread(() -> {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    image.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    image.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    break;
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    image.setColorFilter(filter);
                }
            }, 1000);
        });
    }

    public void activateDirection(Direction direction) {
        switch (direction) {
            case UP:
                this.activateImageView(this.upImage);
                break;
            case DOWN:
                this.activateImageView(this.downImage);
                break;
            case LEFT:
                this.activateImageView(this.leftImage);
                break;
            case RIGHT:
                this.activateImageView(this.rightImage);
                break;
            case CENTER:
                this.activateImageView(this.centerImage);
                break;
        }
    }
}