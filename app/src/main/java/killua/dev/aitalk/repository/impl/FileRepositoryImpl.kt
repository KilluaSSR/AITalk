package killua.dev.aitalk.repository.impl

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.aitalk.consts.PKG_NAME
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.repository.FileRepository
import killua.dev.aitalk.states.AIResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {
    override suspend fun saveResponseToFile(
        model: AIModel,
        prompt: String,
        response: String,
        directoryUri: Uri?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val now = java.time.LocalDateTime.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val fileName = "${prompt}_${now.format(formatter)}.txt"
            val dirName = model.name

            val uri = if (directoryUri != null) {
                val fileUri = createFileInDirectory(directoryUri, dirName, fileName)
                writeTextToUri(fileUri, response)
                fileUri
            } else {
                // 默认目录
                val defaultDir = File(context.getExternalFilesDir(null), "Documents/$PKG_NAME/$dirName")
                if (!defaultDir.exists()) defaultDir.mkdirs()
                val file = File(defaultDir, fileName)
                file.writeText(response)
                Uri.fromFile(file)
            }
            Result.success(uri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAllResponsesToFile(
        prompt: String,
        responses: Map<AIModel, AIResponseState>,
        directoryUri: Uri?
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val results = responses.mapNotNull { (model, state) ->
            val content = state.content ?: return@mapNotNull null
            saveResponseToFile(model, prompt, content, directoryUri).getOrNull()
        }
        Result.success(results)
    }

    private fun createFileInDirectory(
        parentUri: Uri,
        modelSubDir: String,
        fileName: String
    ): Uri {
        val userPickedDir = DocumentFile.fromTreeUri(context, parentUri)
            ?: throw IOException("无法访问用户授权的目录")
        val appRootDir = userPickedDir.findFile(PKG_NAME)
            ?: userPickedDir.createDirectory(PKG_NAME)
            ?: throw IOException("无法创建应用根目录: $PKG_NAME")
        val modelDir = appRootDir.findFile(modelSubDir)
            ?: appRootDir.createDirectory(modelSubDir)
            ?: throw IOException("无法创建模型子目录: $modelSubDir")

        val file = modelDir.createFile("text/plain", fileName)
            ?: throw IOException("无法在 $modelSubDir 中创建文件")

        return file.uri
    }

    private fun writeTextToUri(uri: Uri, text: String) {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            os.write(text.toByteArray())
        } ?: throw IOException("无法写入文件")
    }


}