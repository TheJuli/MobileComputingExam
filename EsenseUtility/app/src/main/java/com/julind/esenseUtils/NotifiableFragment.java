package com.julind.esenseUtils;

import androidx.fragment.app.Fragment;

public abstract class NotifiableFragment extends Fragment {

    public abstract void broadcastedBluetootState(int broadcastedBluetootState);
    public abstract void broadcastedGPSState(boolean broadcastedGPSState);
}
