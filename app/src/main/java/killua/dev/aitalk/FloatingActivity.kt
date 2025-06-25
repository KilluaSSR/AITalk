package killua.dev.aitalk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import killua.dev.aitalk.ui.pages.FloatingWindowContent
import killua.dev.aitalk.ui.theme.AITalkTheme
import kotlin.math.roundToInt

class FloatingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedText = intent?.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
        enableEdgeToEdge()
        setContent {
            AITalkTheme {

                Box(
                    modifier = Modifier
                ) {
                    FloatingWindowContent(
                        selectedText = selectedText,
                        onClose = { finish() },
                        onSearch = { query ->
                            // 简单示例：使用浏览器搜索
                            val searchUri = Uri.parse("https://www.google.com/search?q=$query")
                            val intent = Intent(Intent.ACTION_VIEW, searchUri)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
