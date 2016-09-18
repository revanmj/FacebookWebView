package pl.revanmj.facebookwebview;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends ActionBarActivity {

    AdvancedWebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.createInstance(getApplicationContext());
        CookieManager.getInstance().setAcceptCookie(true);

        webview = (AdvancedWebView) findViewById(R.id.webView);
        webview.getSettings().setAppCacheEnabled(true);
        webview.addPermittedHostname("www.facebook.com");
        webview.addPermittedHostname("touch.facebook.com");
        webview.addPermittedHostname("m.facebook.com");
        webview.addPermittedHostname("h.facebook.com");
        webview.addPermittedHostname("facebook.com");
        webview.setListener(this, new AdvancedWebView.Listener() {

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
                // the new page failed to load
            }

            @Override
            public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
                if (AdvancedWebView.handleDownload(MainActivity.this, url, suggestedFilename)) {
                    Log.d(MainActivity.class.getSimpleName(), "File download finished.");
                }
                else {
                    Log.d(MainActivity.class.getSimpleName(), "File download failed.");
                    // TODO show some notice to the user
                }
            }

            @Override
            public void onExternalPageRequest(String url) {
                //AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                //ad.setMessage(url);
                //ad.setButton("OK", new DialogInterface.OnClickListener() {
                //    @Override
                //    public void onClick(DialogInterface dialog, int which) {
                //        dialog.dismiss();
                //    }
                //});
                //ad.show();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

        });

        webview.loadUrl("http://www.facebook.com/");
        checkForPermission();
    }

    private void checkForPermission() {
        int permission = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.ACCESS_FINE_LOCATION")) {
            Toast.makeText(this, "Location permission denied, disabling geolocation support...", Toast.LENGTH_LONG).show();
            webview.setGeolocationEnabled(false);
            return;
        }
        else if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
            return;
        }

        webview.setGeolocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    webview.setGeolocationEnabled(true);
                } else {
                    Toast.makeText(this, "Location permission denied, disabling geolocation support...", Toast.LENGTH_SHORT).show();
                    webview.setGeolocationEnabled(false);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    // Making back key going to the previous site
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        webview.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (webview.onBackPressed()) {
            // your normal onBackPressed() code here
            super.onBackPressed();
        }
    }
}
