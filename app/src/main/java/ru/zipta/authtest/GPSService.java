package ru.zipta.authtest;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public class GPSService extends Service {

    public static final String TAG = GPSService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = 1000;
    public static final String GPS_WAKE_LOCK = "GPSWakeLock";
    public static final int GPS_TIME_THRESHOLD = 10000; // 10 sec
    public static final int GPS_DISTANCE_THRESHOLD = 10; // 10 meters

    private EventBus bus = EventBus.getDefault();
    private LocationManager lm;
    private LocationListener locationListener;
    private Location location = null;
    private Timer timer;
    private DumpTask dumpTask = null;
    private DBHelper dbHelper = null;
    private boolean active = false;
    private PowerManager.WakeLock wakeLock = null;


    @Override
    public void onCreate() {
        super.onCreate();
        bus.register(this);
        timer = new Timer();
        dbHelper = DBHelper.getInstance(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        Log.d(TAG, "onCreate ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroyed");
        bus.unregister(this);
        timer.cancel();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        lm.removeUpdates(locationListener);
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    public void onEvent(GPSLoggerCommand e) {
        if (e.command == GPSLoggerCommand.START && !active) {
            Log.d(TAG, "start gps logger");
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_THRESHOLD, GPS_DISTANCE_THRESHOLD, locationListener);
            }catch (SecurityException ex){
                Log.e(TAG, "onEvent " + ex.toString());
            }
            dumpTask = new DumpTask();
            timer.schedule(dumpTask, GPS_TIME_THRESHOLD, GPS_TIME_THRESHOLD);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.gps_logger))
                    .setContentText(getString(R.string.loggingnotify_text));
            Notification notification = builder.build();

            startForeground(ONGOING_NOTIFICATION_ID, notification);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (prefs.getBoolean("wakelock", false)) {
                PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, GPS_WAKE_LOCK);
                wakeLock.acquire();
            }

            active = true;
        } else if (e.command == GPSLoggerCommand.STOP && active) {
            Log.d(TAG, "stop gps logger");

            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }

            dumpTask.cancel();
            try {
                lm.removeUpdates(locationListener);
            }catch(SecurityException ex){
                Log.e(TAG, "onEvent " + ex);
            }
            bus.post(new StatusReply("total rows " + dbHelper.getRowsCount()));
            stopForeground(true);
            active = false;
        } else if (e.command == GPSLoggerCommand.STATUS) {
            Log.d(TAG, "onEvent send message " + active);
            bus.post(new GPSLoggerStatus(active));
        }
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {
            if (loc != null) {
                Log.d(TAG, "onLocationChanged " + loc.getLatitude() + ":" + loc.getLongitude());
                location = loc;
            }
        }

        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            String showStatus = null;
            if (status == LocationProvider.AVAILABLE)
                showStatus = "Available";
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                showStatus = "Temporarily Unavailable";
            if (status == LocationProvider.OUT_OF_SERVICE)
                showStatus = "Out of Service";
            Log.d(TAG, "onStatusChanged " + showStatus);
        }

    }

    public class DumpTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "dump to base");
            if (location != null) {
                dbHelper.insertLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), new Date());
                bus.post(new GPSLoggerStatus(active, GPSLoggerStatus.NEW_POSITION));
            }
        }
    }

}
