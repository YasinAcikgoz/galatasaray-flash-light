package com.yacikgoz.galatasarayflashlight;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yacikgoz.galatasarayflashlight.light.BuildConfig;

public class Light extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.v("Light", "Action: " + intent.getAction());
        }
        final String action = intent.getAction();

        switch (action) {
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                context.startService(new Intent(context, LightService.class));
                break;
            case AppWidgetManager.ACTION_APPWIDGET_DISABLED:
                onDisabled(context);
                break;
        }
    }
    public static void onDisabled(Context context) {
        if (BuildConfig.DEBUG) {
            Log.d("Light", "onDisabled");
        }
        context.stopService(new Intent(context, LightService.class));
    }
}
