package killua.dev.aitalk.repository

import android.net.Uri
import killua.dev.aitalk.models.AIModel
import killua.dev.aitalk.states.AIResponseState

interface FileRepository {
    suspend fun saveResponseToFile(
        model: AIModel,
        prompt: String,
        response: String,
        directoryUri: Uri?
    ): Result<String>

    suspend fun saveAllResponsesToFile(
        prompt: String,
        responses: Map<AIModel, AIResponseState>,
        directoryUri: Uri?
    ): Result<List<String>>
}