package ru.zipta.authtest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.zipta.authtest.restful.ApiService;
import ru.zipta.authtest.restful.ServiceGenerator;


public class NetService extends Service {

    public static final String TAG = NetService.class.getSimpleName();
    public static final int VACUUM_THRESHOLD = 10000; //10k rows
    public static final int PUB_TIMEOUT = 30000; //half min
    public static final int VACUUM_TIMEOUT = 600000; //10 min

    private EventBus bus = EventBus.getDefault();
    private ApiService api = null;
    private Timer timer;
    private DBHelper dbHelper;

    public NetService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus.register(this);
        dbHelper = DBHelper.getInstance(this);
        timer = new Timer();
        timer.schedule(new PubTask(), PUB_TIMEOUT, PUB_TIMEOUT);
        timer.schedule(new VacuumTask(), VACUUM_TIMEOUT, VACUUM_TIMEOUT);
        Log.d(TAG, "created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroyed");
        timer.cancel();
        bus.unregister(this);
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    public void onEvent(UpdateConnect e) {
        Log.d(TAG, "update connect");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = prefs.getString("token", null);
        if (token != null) {
            api = ServiceGenerator.createService(ApiService.class, ApiService.BASE_URL, token);
        } else {
            api = null;
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(StatusQuery sq) {
        if (api == null) return;
        Log.d(TAG, "message query");
        try {
            ApiService.Status status = api.test();
            bus.post(new StatusReply(status.status));
        } catch (RetrofitError e) {
            Response r = e.getResponse();
            if (r != null) {
                int s = e.getResponse().getStatus();
                Log.e(TAG, "PubTask error status " + s);
                bus.post(new ErrorEvent(s, "" + e));
            } else {
                Log.e(TAG, "PubTask unknown error " + e);
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public class PubTask extends TimerTask {

        @Override
        public void run() {
            if (api == null) return;
            if (!isOnline())
                return;
            Log.d(TAG, "publish to site");
            Cursor c = dbHelper.getUnpublished();
            try {
                while (c.moveToNext()) {
                    long _id = c.getLong(c.getColumnIndex("_id"));
                    Log.d(TAG, "post locations " + _id);
                    ApiService.Location l = new ApiService.Location(
                            c.getDouble(c.getColumnIndex("lat")),
                            c.getDouble(c.getColumnIndex("lng")),
                            c.getDouble(c.getColumnIndex("alt")),
                            new Date(c.getLong(c.getColumnIndex("time")))
                    );
                    api.postLocation(l);
                    dbHelper.setPublished(_id);
                }
            } catch (RetrofitError e) {
                Response r = e.getResponse();
                if (r != null) {
                    int s = e.getResponse().getStatus();
                    Log.e(TAG, "PubTask error status " + s);
                    bus.post(new ErrorEvent(s, "" + e));
                } else {
                    Log.e(TAG, "PubTask unknown error " + e);
                }
            }
            c.close();
        }
    }

    public class VacuumTask extends TimerTask {

        @Override
        public void run() {
            Log.d(TAG, "vacuum base");
            if (dbHelper.getRowsCount() > VACUUM_THRESHOLD)
                dbHelper.vacuum();
        }
    }

}

