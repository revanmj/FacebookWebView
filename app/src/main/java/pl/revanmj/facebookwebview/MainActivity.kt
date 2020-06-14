package pl.revanmj.facebookwebview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import im.delight.android.webview.AdvancedWebView

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.ArrayList

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
        
        private const val FB_URL = "https://www.facebook.com/"
        private const val FB_SHARE_URL = "https://www.facebook.com/sharer.php?u="
        private const val PERMISSION_REQUEST_CODE = 1
        private val PERMITTED_HOSTNAMES = object : ArrayList<String>() {
            init {
                add("www.facebook.com")
                add("touch.facebook.com")
                add("m.facebook.com")
                add("h.facebook.com")
                add("facebook.com")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.createInstance(applicationContext)
        CookieManager.getInstance().setAcceptCookie(true)

        webView.settings.setAppCacheEnabled(true)
        webView.addPermittedHostnames(PERMITTED_HOSTNAMES)
        webView.setListener(this, object : AdvancedWebView.Listener {
            override fun onPageFinished(url: String) {
                if (Build.VERSION.SDK_INT > 20)
                    CookieManager.getInstance().flush()
                else if (Build.VERSION.SDK_INT < 21)
                    CookieSyncManager.getInstance().sync()
            }

            override fun onPageStarted(url: String, favicon: Bitmap?) {
                // a new page started loading
            }

            override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
                Log.e(LOG_TAG, "onPageError: $errorCode - $description")
            }

            override fun onDownloadRequested(url: String, suggestedFilename: String?, mimeType: String?,
                                             contentLength: Long, contentDisposition: String?, userAgent: String?) {
                if (AdvancedWebView.handleDownload(this@MainActivity, url, suggestedFilename)) {
                    Log.d(LOG_TAG, getString(R.string.download_finished))
                } else {
                    Log.e(LOG_TAG, "File download failed.")
                    Toast.makeText(this@MainActivity, R.string.download_failed, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onExternalPageRequest(url: String) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        })
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val type = intent.type

            checkForPermission()

            if (Intent.ACTION_SEND == action && type != null) {
                val shareText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (Patterns.WEB_URL.matcher(shareText).matches()) {
                    try {
                        webView.loadUrl(FB_SHARE_URL + URLEncoder.encode(shareText, "UTF-8"))
                        super.onNewIntent(intent)
                        return
                    } catch (e: UnsupportedEncodingException) { }
                } else {
                    Toast.makeText(this, R.string.error_only_url, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        webView.loadUrl(FB_URL)
    }

    private fun checkForPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
            webView.setGeolocationEnabled(false)
            return
        } else if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            return
        }
        webView.setGeolocationEnabled(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                webView.setGeolocationEnabled(true)
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
                webView.setGeolocationEnabled(false)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.getInstance().stopSync()
    }

    public override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 21)
            CookieSyncManager.getInstance().startSync()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        // Making back key going to the previous site
        webView.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        if (webView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
