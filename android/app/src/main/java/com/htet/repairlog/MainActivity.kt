package com.htet.repairlog

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import org.json.JSONObject

/**
 * Hosts the Repair Formula Log web app (bundled in assets/web, so it opens offline).
 * The page is served from https://appassets.androidplatform.net so localStorage, clipboard
 * and fetch behave as on a normal secure site. Links to other sites open in the browser.
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val data = res.data
        val clip = data?.clipData
        val uris: Array<Uri>? = when {
            res.resultCode != RESULT_OK || data == null -> null
            clip != null -> Array(clip.itemCount) { clip.getItemAt(it).uri }
            data.data != null -> arrayOf(data.data!!)
            else -> null
        }
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    private val authConsent = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        try {
            val r = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(res.data)
            sendDriveToken(r.accessToken, null)
        } catch (e: Exception) {
            sendDriveToken(null, describeAuthError(e, res.resultCode))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            addJavascriptInterface(WebBridge(this@MainActivity), "RFLNative")
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                    loader.shouldInterceptRequest(request.url)

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url
                    if (url.host == APP_HOST) return false
                    openExternal(url)
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams,
                ): Boolean {
                    fileCallback?.onReceiveValue(null)
                    fileCallback = callback
                    val intent = params.createIntent().apply {
                        if (params.mode == FileChooserParams.MODE_OPEN_MULTIPLE) putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                    return try {
                        pickFiles.launch(intent); true
                    } catch (e: ActivityNotFoundException) {
                        fileCallback = null; false
                    }
                }
            }
        }

        val root = FrameLayout(this)
        root.addView(web, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        // Keep the page clear of the status bar, navigation bar and keyboard (edge-to-edge on Android 15+).
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setContentView(root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back closes the full-screen viewer first, then leaves the app.
                web.evaluateJavascript(
                    "(function(){var v=document.getElementById('viewer');" +
                        "if(v&&v.classList.contains('on')){document.getElementById('vClose').click();return 1}return 0})()",
                ) { r ->
                    if (r != "1") {
                        if (web.canGoBack()) web.goBack() else finish()
                    }
                }
            }
        })

        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) web.loadUrl(START_URL)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    private fun openExternal(url: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, url.toString(), Toast.LENGTH_LONG).show()
        }
    }

    /** Google Drive access token for the page's Drive sync (scope drive.file only). */
    fun requestDriveToken(interactive: Boolean) {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_FILE_SCOPE)))
            .build()
        Identity.getAuthorizationClient(this).authorize(request)
            .addOnSuccessListener { r ->
                val pending = r.pendingIntent
                when {
                    !r.hasResolution() -> sendDriveToken(r.accessToken, null)
                    interactive && pending != null ->
                        authConsent.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                    else -> sendDriveToken(null, "consent")
                }
            }
            .addOnFailureListener { e -> sendDriveToken(null, describeAuthError(e, null)) }
    }

    /** Readable reason, with the Google status code, so a setup problem can be told from a real cancel. */
    private fun describeAuthError(e: Exception, resultCode: Int?): String {
        val code = (e as? ApiException)?.statusCode
        val hint = when (code) {
            CommonStatusCodes.DEVELOPER_ERROR ->
                "Google Cloud မှာ Android OAuth client (package com.htet.repairlog + SHA-1) မထည့်ရသေး / မကိုက်ပါ"
            CommonStatusCodes.CANCELED ->
                "ပယ်ဖျက်လိုက်သည် (OAuth consent screen က Testing ဆိုရင် ဒီ Gmail ကို Test user ထဲ ထည့်ပါ)"
            CommonStatusCodes.NETWORK_ERROR -> "အင်တာနက် စစ်ပါ"
            CommonStatusCodes.SIGN_IN_REQUIRED -> "ဖုန်းမှာ Google အကောင့် ဝင်ထားဖို့ လိုသည်"
            else -> e.message ?: "error"
        }
        val rc = if (resultCode != null && resultCode != RESULT_OK) " result=$resultCode" else ""
        return "$hint [code=${code ?: "-"}$rc]"
    }

    private fun sendDriveToken(token: String?, error: String?) {
        if (isDestroyed) return
        web.evaluateJavascript(
            "window.rflDriveToken&&rflDriveToken(${JSONObject.quote(token ?: "")},3600,${JSONObject.quote(error ?: "")})",
            null,
        )
    }

    companion object {
        const val APP_HOST = "appassets.androidplatform.net"
        const val START_URL = "https://$APP_HOST/assets/web/index.html"
        const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"
    }
}
