package pl.revanmj.facebookwebview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FB_URL = "https://www.facebook.com/";
    private static final String FB_SHARE_URL = "https://www.facebook.com/sharer.php?u=";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private AdvancedWebView mWebView;

    private static final List<String> PERMITTED_HOSTNAMES = new ArrayList<String>() {{
        add("www.facebook.com");
        add("touch.facebook.com");
        add("m.facebook.com");
        add("h.facebook.com");
        add("facebook.com");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.createInstance(getApplicationContext());
        CookieManager.getInstance().setAcceptCookie(true);

        mWebView = findViewById(R.id.webView);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.addPermittedHostnames(PERMITTED_HOSTNAMES);
        mWebView.setListener(this, new AdvancedWebView.Listener() {
            @Override
            public void onPageFinished(String url) {
                if (Build.VERSION.SDK_INT > 20)
                    CookieManager.getInstance().flush();
                else if (Build.VERSION.SDK_INT < 21)
                    CookieSyncManager.getInstance().sync();
            }

            @Override
            public void onPageStarted(String url, Bitmap favicon) {
                // a new page started loading
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
                Log.e(LOG_TAG, "onPageError: " + errorCode + " - " + description);
            }

            @Override
            public void onDownloadRequested(String url, String suggestedFilename, String mimeType,
                                            long contentLength, String contentDisposition, String userAgent) {
                if (AdvancedWebView.handleDownload(MainActivity.this, url, suggestedFilename)) {
                    Log.d(LOG_TAG, getString(R.string.download_finished));
                }
                else {
                    Log.e(LOG_TAG, "File download failed.");
                    Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onExternalPageRequest(String url) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();

            checkForPermission();

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                String shareText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (Patterns.WEB_URL.matcher(shareText).matches()) {
                    try {
                        mWebView.loadUrl(FB_SHARE_URL + URLEncoder.encode(shareText, "UTF-8"));
                        super.onNewIntent(intent);
                        return;
                    } catch (UnsupportedEncodingException e) {}
                } else {
                    Toast.makeText(this, R.string.error_only_url, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        mWebView.loadUrl(FB_URL);
    }

    private void checkForPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            mWebView.setGeolocationEnabled(false);
            return;
        } else if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }
        mWebView.setGeolocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mWebView.setGeolocationEnabled(true);
                } else {
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                    mWebView.setGeolocationEnabled(false);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.getInstance().stopSync();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Making back key going to the previous site
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
