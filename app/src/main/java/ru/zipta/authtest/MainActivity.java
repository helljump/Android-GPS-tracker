package ru.zipta.authtest;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int DRAWER_VACUUM = 0;

    private EventBus bus = EventBus.getDefault();
    private SwitchCompat logSwitch;
    private SwitchCompat wakelockSwitch;
    private RecyclerView rvLocations;
    private LocationListCursorAdapter mAdapter;
    private DBHelper dbHelper;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView drawer_menu = (ListView) findViewById(R.id.list_menu);
        drawer_menu.setOnItemClickListener(this);

        android.support.v7.app.ActionBar toolbar = getSupportActionBar();
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setHomeButtonEnabled(true);
        toolbar.setDisplayShowTitleEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        rvLocations = (RecyclerView) findViewById(R.id.rvLocations);
        StaggeredGridLayoutManager locationLayourManager = new StaggeredGridLayoutManager(getColumnsCount(), StaggeredGridLayoutManager.VERTICAL);
        rvLocations.setLayoutManager(locationLayourManager);

        dbHelper = DBHelper.getInstance(this);
        mAdapter = new LocationListCursorAdapter(this, dbHelper.getAllLocations());
        rvLocations.setAdapter(mAdapter);

        findViewById(R.id.testButton).setOnClickListener(this);

        logSwitch = (SwitchCompat) findViewById(R.id.logSw);
        logSwitch.setOnCheckedChangeListener(this);

        wakelockSwitch = (SwitchCompat) findViewById(R.id.wakelockSw);
        wakelockSwitch.setChecked(prefs.getBoolean("wakelock", false));
        wakelockSwitch.setOnCheckedChangeListener(this);

        bus.post(new UpdateConnect());
    }

    public int getColumnsCount() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 2;
        else
            return 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
        bus.post(new GPSLoggerCommand(GPSLoggerCommand.STATUS));
    }

    @Override
    protected void onStop() {
        bus.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        } else if (id == R.id.action_signout) {
            signOut();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    private void signOut() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("token");
        editor.apply();
        bus.post(new UpdateConnect());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.testButton) {
            bus.post(new StatusQuery());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(GPSLoggerStatus e) {
        if (e.active)
            logSwitch.setChecked(true);
        if (e.event == GPSLoggerStatus.NEW_POSITION) {
            mAdapter.changeCursor(dbHelper.getAllLocations());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(StatusReply e) {
        //Snackbar.make(rvLocations, "Status: " + e.status, Snackbar.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "Status: " + e.status, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ErrorEvent e) {
        if (e.status == 403) {
            Toast.makeText(getApplicationContext(), "Sign in, please", Toast.LENGTH_LONG).show();
            bus.post(new UpdateConnect());
            signOut();
            finish();
        } else if (e.status == 404 || e.status == 500) {
            Toast.makeText(getApplicationContext(), "Site is down", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.logSw) {
            GPSLoggerCommand c;
            if (isChecked)
                c = new GPSLoggerCommand(GPSLoggerCommand.START);
            else
                c = new GPSLoggerCommand(GPSLoggerCommand.STOP);
            bus.post(c);
        } else if (buttonView.getId() == R.id.wakelockSw) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("wakelock", isChecked);
            editor.apply();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick " + id);
        if(id == DRAWER_VACUUM){
            dbHelper.vacuum();
            Snackbar.make(rvLocations, "Vacuumized", Snackbar.LENGTH_SHORT).show();
        }
        if (drawerLayout != null)
            drawerLayout.closeDrawers();
    }
}