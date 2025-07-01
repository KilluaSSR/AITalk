package killua.dev.aitalk

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import killua.dev.aitalk.ui.pages.FloatingWindowContent
import killua.dev.aitalk.ui.theme.AITalkTheme

@AndroidEntryPoint
class FloatingActivity : ComponentActivity() {
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedText = intent?.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
        enableEdgeToEdge()

        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { _ ->
            if (Settings.canDrawOverlays(this)) {
                setFloatingUI()
            } else {
                finish()
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            setFloatingUI()
        }
    }

    private fun setFloatingUI() {
        val selectedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT).orEmpty()
        enableEdgeToEdge()
        setContent {
            AITalkTheme {
                Box(modifier = Modifier) {
                    FloatingWindowContent(
                        selectedText = selectedText,
                        onClose = { finish() },
                    )
                }
            }
        }
    }


}
