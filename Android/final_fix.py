import os
import re

gradle_path = 'app/build.gradle'
manifest_path = 'app/src/main/AndroidManifest.xml'
activity_path = 'app/src/main/java/com/jonazwetsloot/somtodaymod/MainActivity.kt'

# 1. Maak de app uniek
if os.path.exists(gradle_path):
    with open(gradle_path, 'r') as f: content = f.read()
    content = re.sub(r'applicationId\s+"[^"]+"', 'applicationId "com.somtoday.mod.clean"', content)
    with open(gradle_path, 'w') as f: f.write(content)

# 2. Fix Manifest
if os.path.exists(manifest_path):
    with open(manifest_path, 'r') as f: content = f.read()
    content = re.sub(r'package="[^"]+"', 'package="com.somtoday.mod.clean"', content)
    content = content.replace('android:label="Somtoday Mod"', 'android:label="Somtoday Clean"')
    with open(manifest_path, 'w') as f: f.write(content)

# 3. MainActivity Code
new_code = """package com.jonazwetsloot.somtodaymod
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var callback: ValueCallback<Array<Uri>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wv = WebView(this)
        setContentView(wv)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.settings.allowFileAccess = true
        wv.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean = false
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(w: WebView, c: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                if (callback != null) return true
                callback = c
                val i = Intent(Intent.ACTION_GET_CONTENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" }
                startActivityForResult(Intent.createChooser(i, "Kies bestand"), 1)
                return true
            }
        }
        wv.loadUrl("https://somtoday.nl/login")
    }
    override fun onActivityResult(req: Int, res: Int, d: Intent?) {
        super.onActivityResult(req, res, d)
        if (req == 1) { callback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, d)); callback = null }
    }
}"""
with open(activity_path, 'w') as f: f.write(new_code)
print("✅ Bestanden aangepast.")
