package com.example.c.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
    }
}
