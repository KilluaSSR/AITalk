package killua.dev.aitalk.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [Index("timestamp"), Index("prompt")]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val timestamp: Long,
    val chatGPTContent: String? = null,
    val claudeContent: String? = null,
    val geminiContent: String? = null,
    val deepSeekContent: String? = null,
    val grokContent: String? = null
)