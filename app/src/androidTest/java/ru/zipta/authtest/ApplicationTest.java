package ru.zipta.authtest;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.zipta.authtest.restful.ApiService;
import ru.zipta.authtest.restful.ServiceGenerator;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public static final String TAG = ApplicationTest.class.getSimpleName();

    public ApplicationTest() {
        super(Application.class);
    }


    public void testLocation() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = prefs.getString("token", null);
        ApiService api = ServiceGenerator.createService(ApiService.class, ApiService.BASE_URL, token);

        ApiService.Location l = new ApiService.Location(
                12,
                34,
                56,
                new Date()
        );

        try {
            Response r = api.postLocation(l);
        }catch (RetrofitError e){
            Log.d(TAG, "testLocation " + e.getResponse().getStatus());
            throw e;
        }
    }

}