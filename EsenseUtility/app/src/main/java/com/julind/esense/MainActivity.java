package com.julind.esense;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.julind.esenseUtils.BroadcastRecieverWithFragment;
import com.julind.esenseUtils.ConnectionManager;
import com.julind.esenseUtils.mediaControls.MediaControlService;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.esense.esenselib.ESenseManager;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_GRANTED = PackageManager.PERMISSION_GRANTED;

    private final String[] permissions = new String[]{BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};

    private ESenseManager manager;
    private FragmentManager fragmentManager;
    private NoEarableConnected noEarableFragment;
    private String earableName;
    private EarableConnected connectedEarableFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.fragmentManager = getSupportFragmentManager();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        MediaControlService.setAudioManager(audioManager);

        if (ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), BLUETOOTH) == PERMISSIONS_GRANTED &&
            ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), BLUETOOTH_ADMIN) == PERMISSIONS_GRANTED &&
            ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), ACCESS_COARSE_LOCATION) == PERMISSIONS_GRANTED &&
            ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(), ACCESS_FINE_LOCATION) == PERMISSIONS_GRANTED) {
            this.acitvateConnectionState();
        } else {
            ActivityResultLauncher<String[]> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                        boolean val = true;
                        if (isGranted.entrySet().isEmpty()) {
                            val = false;
                        } else {
                            for (Map.Entry<String, Boolean> e: isGranted.entrySet()) {
                                val = val && e.getValue();
                            }
                        }
                        if (val) {
                            this.acitvateConnectionState();
                        } else {
                            this.fragmentManager.beginTransaction().replace(R.id.stateFrame, MissingReq.class, null).commitNow();
                        }
                    });
            requestPermissionLauncher.launch(this.permissions);
        }
    }

    private void acitvateConnectionState() {
        this.fragmentManager.beginTransaction().replace(R.id.stateFrame, NoEarableConnected.class, null).commitNow();

        this.noEarableFragment = (NoEarableConnected) fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
        this.noEarableFragment.registerMainClass(this);

        BroadcastRecieverWithFragment<NoEarableConnected> noEarableConnectedBroadcastReciever
                = new BroadcastRecieverWithFragment<>(this.noEarableFragment);

        getApplicationContext().registerReceiver(noEarableConnectedBroadcastReciever, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        getApplicationContext().registerReceiver(noEarableConnectedBroadcastReciever, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));

    }

    public void runConnection() {
        TextView earableNameTextView = this.noEarableFragment.getView().findViewById(R.id.earableName);
        this.earableName = earableNameTextView.getText().toString();

        this.manager =
                new ESenseManager(earableName,
                        MainActivity.this.getApplicationContext(),
                        new ConnectionManager(this.noEarableFragment, this));
        this.manager.connect(10000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.manager.disconnect();
        NotificationManagerCompat.from(getApplicationContext()).cancel(0);
    }

    public void onConnectionSuccess() {
        runOnUiThread(() -> {
            this.fragmentManager.beginTransaction().replace(R.id.stateFrame, EarableConnected.class, null).commitNow();

            this.connectedEarableFragment = (EarableConnected) this.fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);

            this.connectedEarableFragment.registerMainActivity(this);
            this.connectedEarableFragment.registerManagerAndSetName(this.manager, this.earableName);

            connectedEarableFragment.startEventListenerRegistering();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    manager.getSensorConfig();
                }
            }, 3000);
        });
    }

    public void disconnect() {
        this.runOnUiThread(() -> {
            this.manager.disconnect();
            this.connectedEarableFragment.stopEventListenerRegistering();
            this.fragmentManager.beginTransaction().remove(this.connectedEarableFragment);
            this.acitvateConnectionState();
        });
    }
}