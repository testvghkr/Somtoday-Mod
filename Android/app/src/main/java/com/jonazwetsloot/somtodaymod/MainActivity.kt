package com.jonazwetsloot.somtodaymod

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.dismiss
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.mozilla.geckoview.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var geckoView: GeckoView
    private lateinit var geckoSession: GeckoSession
    private var canGoBackState: Boolean = false
    companion object {
        lateinit var appContext: Context
        private var sRuntime: GeckoRuntime? = null
    }

    private var pendingFilePrompt: GeckoSession.PromptDelegate.FilePrompt? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1001

    private val promptDelegate = object : GeckoSession.PromptDelegate {
        override fun onFilePrompt(
            session: GeckoSession,
            prompt: GeckoSession.PromptDelegate.FilePrompt
        ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"

            if (prompt.mimeTypes?.isNotEmpty() == true) {
                intent.type = prompt.mimeTypes?.joinToString(",")
            }

            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,
                prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE)

            startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_CHOOSER_REQUEST_CODE)

            pendingFilePrompt = prompt

            return GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = FrameLayout(this)
        setContentView(root)

        geckoView = GeckoView(this)
        geckoView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        root.addView(geckoView)

        if (sRuntime == null) {
            sRuntime = GeckoRuntime.create(this)
        }

        geckoSession = GeckoSession()
        geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                canGoBackState = canGoBack
            }
        }
        geckoSession.open(sRuntime!!)
        geckoView.setSession(geckoSession)
        geckoSession.promptDelegate = promptDelegate

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        root.requestApplyInsets()

        val webExtController = sRuntime!!.webExtensionController
        val extId = "somtodaymod@jonazwetsloot.nl"

        webExtController.ensureBuiltIn("resource://android/assets/extension/", extId)

        geckoSession.loadUri("https://leerling.somtoday.nl/")

        onBackPressedDispatcher.addCallback(this) {
            if (canGoBackState) {
                geckoSession.goBack()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val prompt = pendingFilePrompt ?: return
            pendingFilePrompt = null

            if (resultCode == Activity.RESULT_OK && data != null) {
                val uris = mutableListOf<Uri>()
                data.data?.let { uris.add(it) }
                data.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        uris.add(clip.getItemAt(i).uri)
                    }
                }
                prompt.confirm(this, uris.toTypedArray())
            } else {
                prompt.dismiss()
            }
        }
    }
}