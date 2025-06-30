package killua.dev.aitalk.utils

import android.provider.DocumentsContract
import androidx.core.net.toUri

fun getVirtualPathFromTreeUri(uriString: String): String {
    return try {
        val uri = uriString.toUri()
        val docId = DocumentsContract.getTreeDocumentId(uri)
        // 例如 docId = "primary:Documents/AI"
        val split = docId.split(":")
        if (split.size == 2) {
            "/" + split[1]
        } else {
            "/" + docId.replace(':', '/')
        }
    } catch (e: Exception) {
        uriString
    }
}