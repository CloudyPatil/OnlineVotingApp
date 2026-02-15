package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class AirplaneModeReceiver extends BroadcastReceiver {

    // Interface to notify Activity about airplane mode changes
    public interface AirplaneModeListener {
        void onAirplaneModeChanged(boolean isEnabled);
    }

    private AirplaneModeListener listener;

    // Default constructor (required for manifest registration)
    public AirplaneModeReceiver() {
    }

    // Constructor with listener (for dynamic registration from Activity)
    public AirplaneModeReceiver(AirplaneModeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method runs automatically when airplane mode changes

        if (intent != null && Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {

            // Check if airplane mode is currently ON or OFF
            boolean isAirplaneModeOn = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            // Show a Toast message to user
            if (isAirplaneModeOn) {
                Toast.makeText(context,
                        context.getString(R.string.airplane_mode_on_toast),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context,
                        context.getString(R.string.airplane_mode_off_toast),
                        Toast.LENGTH_LONG).show();
            }

            // Notify the Activity if listener is set
            if (listener != null) {
                listener.onAirplaneModeChanged(isAirplaneModeOn);
            }
        }
    }
}