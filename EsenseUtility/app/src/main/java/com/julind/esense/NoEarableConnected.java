package com.julind.esense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.julind.esenseUtils.NotifiableFragment;


public class NoEarableConnected extends NotifiableFragment {
    private MainActivity mainActivity;

    private boolean gpsStatus = false;
    private boolean btStatus = false;
    private FloatingActionButton connectFab;
    private ImageView gpsIcon;
    private ImageView btIcon;
    private TextView connectionStatusText;
    private TextView deviceNotFoundText;
    private View view;

    public NoEarableConnected() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_no_earable_connected, container, false);

        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        this.connectFab = this.view.findViewById(R.id.connectFab);
        this.gpsIcon = this.view.findViewById(R.id.gpsIcon);
        this.btIcon = this.view.findViewById(R.id.btIcon);

        this.connectionStatusText = this.view.findViewById(R.id.connectionStatusText);
        this.deviceNotFoundText = this.view.findViewById(R.id.failedText);

        this.connectFab.setOnClickListener((val) -> {
            this.deviceNotFoundText.setText("");
            this.connectionStatusText.setText("Connecting ...");
            this.mainActivity.runConnection();
            this.connectFab.hide();
        });

        this.btStatus = BluetoothAdapter.STATE_ON == bluetoothManager.getAdapter().getState();
        this.gpsStatus = locationManager.isLocationEnabled();

        this.checkToEnableStuff();
        return view;
    }

    private void checkToEnableStuff() {
        if (gpsStatus) {
            this.gpsIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_gps_fixed_24, getActivity().getTheme()));
        } else {
            this.gpsIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_gps_off_24, getActivity().getTheme()));
        }

        if (btStatus)  {
            this.btIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_bluetooth_24, getActivity().getTheme()));
        } else {
            this.btIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24, getActivity().getTheme()));
        }

        if (this.btStatus && this.gpsStatus) {
            this.connectFab.show();
            this.connectionStatusText.setText("Ready to Connect");
        } else {
            this.connectFab.hide();
            this.connectionStatusText.setText("Please enable Bluetooth and GPS");

        }
    }

    public void registerMainClass(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void broadcastedBluetootState(int bluetoothState) {
        this.btStatus = bluetoothState == BluetoothAdapter.STATE_ON;
        this.checkToEnableStuff();
    }

    @Override
    public void broadcastedGPSState(boolean locationEnabled) {
        this.gpsStatus = locationEnabled;
        this.checkToEnableStuff();
    }

    public void notFound() {
        this.mainActivity.runOnUiThread(() -> {
            this.connectFab.show();
            this.checkToEnableStuff();
            this.deviceNotFoundText.setText("Earable not found!");
            this.deviceNotFoundText.setTextColor(getResources().getColor(R.color.design_default_color_error, mainActivity.getTheme()));
        });
    }
}