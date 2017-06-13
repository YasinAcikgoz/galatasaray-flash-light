package com.yacikgoz.galatasarayflashlight;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.yacikgoz.galatasarayflashlight.light.BuildConfig;
import com.yacikgoz.galatasarayflashlight.light.R;

import java.io.IOException;
import java.util.List;

public class LightService extends Service {

    public static final String FLASH_ON = BuildConfig.APPLICATION_ID + ".FLASH_ON";
    public static final String FLASH_OFF = BuildConfig.APPLICATION_ID + ".FLASH_OFF";
    private Camera mCamera;

    private PowerManager.WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FLASH_ON.equals(action)) {
                try {
                    startCamera();
                    updateWidgets(this, true);
                    return START_STICKY;
                } catch (Exception ex) {
                    Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();
                    if (BuildConfig.DEBUG) {
                        Log.v("LightService", "exception " + ex.getMessage());
                    }
                }
            }
        }
        updateWidgets(this, false);
        stopSelf();
        return START_NOT_STICKY;
    }
    static RemoteViews getRemoteViews(String packageName, boolean flashState, PendingIntent pendingIntent) {
        final RemoteViews remoteViews = new RemoteViews(packageName, R.layout.widget_layout);
        int imageResource;

        if(flashState)
            imageResource = R.drawable.on;
        else
            imageResource = R.drawable.off;

        remoteViews.setImageViewResource(R.id.update, imageResource);
        remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);

        return remoteViews;
    }

    @Override
    public void onDestroy() {
        stopCamera();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld())
                mWakeLock.release();
            mWakeLock = null;
        }
        super.onDestroy();
    }
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
        stopSelf();
    }

    public static void updateWidgets(Context context, boolean flashOn) {
        final Intent intent = new Intent(context, LightService.class);
        intent.setAction(flashOn ? FLASH_OFF : FLASH_ON);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        final RemoteViews views = getRemoteViews(context.getPackageName(), flashOn, pendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, Light.class), views);
    }


    private void startCamera() throws IOException {
        mCamera = Camera.open();
        final Parameters parameters = mCamera.getParameters();

        configFlashParameters(parameters);
        mCamera.setParameters(parameters);

        mCamera.setPreviewTexture(new SurfaceTexture(0));

        mCamera.startPreview();
        if (mWakeLock == null)
            mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID);

        if (!mWakeLock.isHeld())
            mWakeLock.acquire();

    }


    private void configFlashParameters(Parameters p) {

        final List<String> flashes = p.getSupportedFlashModes();
        if (flashes == null) {
            throw new IllegalStateException("No flash available");
        }
        if (flashes.contains(Parameters.FLASH_MODE_TORCH)) {
            p.setFlashMode(Parameters.FLASH_MODE_TORCH);
        } else if (flashes.contains(Parameters.FLASH_MODE_ON)) {
            p.setFlashMode(Parameters.FLASH_MODE_ON);
        } else {
            throw new IllegalStateException("No useable flash mode");
        }
    }

}
