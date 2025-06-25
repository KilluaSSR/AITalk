package killua.dev.aitalk.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ClipboardHelper {
    fun copy(text: String)
}

class ClipboardHelperImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ClipboardHelper {
    override fun copy(text: String) {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("AIResponse", text)
        clipboard.setPrimaryClip(clipData)
    }
}