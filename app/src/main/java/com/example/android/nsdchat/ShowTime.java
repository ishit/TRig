package com.example.android.nsdchat;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class ShowTime extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        SntpClient ntpClient = new SntpClient();
        if (ntpClient.requestTime("pool.ntp.org", 1000)) {
            long now = ntpClient.getNtpTime() + SystemClock.elapsedRealtime() - ntpClient.getNtpTimeReference();
            Log.d("Time", "Now: " + now);
        }
        return null;
    }
}
