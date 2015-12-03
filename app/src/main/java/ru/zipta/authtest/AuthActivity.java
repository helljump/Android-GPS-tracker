package ru.zipta.authtest;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AuthActivity extends AppCompatActivity {

    public static final String TAG = AuthActivity.class.getSimpleName();
    public static final String AUTH_URL = "https://auth-jump.rhcloud.com/login/google-oauth2/";
    public static final String SUCCESS_URL = "https://auth-jump.rhcloud.com/login-success/";
    public static final String ERROR_URL = "https://auth-jump.rhcloud.com/login-error/";
    public static final int RESULT_ERROR = 1000;


    @Override
    @SuppressWarnings("setJavascriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        final WebView web = (WebView) findViewById(R.id.webView);
        web.getSettings().setJavaScriptEnabled(true);

        web.setWebViewClient(new WebViewClient() {

                                 @Override
                                 public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                     Toast.makeText(getApplicationContext(), "connect error: " + errorCode, Toast.LENGTH_LONG).show();
                                     Log.e(TAG, "error code:" + errorCode);
                                     super.onReceivedError(view, errorCode, description, failingUrl);
                                     web.clearCache(true);
                                     setResult(RESULT_ERROR);
                                     finish();
                                 }

                                 @Override
                                 public void onPageFinished(WebView view, String url) {
                                     super.onPageFinished(view, url);
                                     if (url.contains(SUCCESS_URL)) {
                                         Log.d(TAG, "auth success");
                                         CookieManager cookieManager = CookieManager.getInstance();
                                         String cookies = cookieManager.getCookie(url);
                                         String[] temp = cookies.split(";");
                                         for (String row : temp) {
                                             String[] egg = row.split("=");
                                             if (egg[0].trim().equals("token")) {
                                                 Log.d(TAG, "k:" + egg[0] + " v:" + egg[1]);
                                                 String token = egg[1];
                                                 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                 SharedPreferences.Editor editor = prefs.edit();
                                                 editor.putString("token", token);
                                                 editor.apply();
                                                 web.clearCache(true);
                                                 setResult(RESULT_OK);
                                                 finish();
                                             }
                                         }
                                     } else if (url.contains(ERROR_URL)) {
                                         Log.e(TAG, "auth error");
                                         Toast.makeText(getApplicationContext(), "Auth error", Toast.LENGTH_LONG).show();
                                         web.clearCache(true);
                                         setResult(RESULT_ERROR);
                                         finish();
                                     }
                                 }
                             }

        );


        web.loadUrl(AUTH_URL);

    }

}
