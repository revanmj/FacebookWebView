package pl.revanmj.facebookwebview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

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
        webview.setGeolocationEnabled(true);
        webview.addPermittedHostname("www.facebook.com");
        webview.addPermittedHostname("touch.facebook.com");
        webview.addPermittedHostname("m.facebook.com");

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
            public void onExternalPageRequest(String url) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

            @Override
            public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // some file is available for download
            }

        });

        webview.loadUrl("http://www.facebook.com/");
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
        // your normal onActivityResult(...) code here
    }

    @Override
    public void onBackPressed() {
        if (webview.onBackPressed()) {
            // your normal onBackPressed() code here
            super.onBackPressed();
        }
    }
}
