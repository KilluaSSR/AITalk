package killua.dev.aitalk.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Conversation level metadata. One conversation can involve multiple models responding
 * to the same sequence of user messages. Title may be generated asynchronously.
 */
@Entity(
    tableName = "conversation",
    indices = [
        Index("updatedAt"),
        Index("createdAt")
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val firstUserMessagePreview: String? = null,
    /** Json encoded list of AIModel names participating in this conversation */
    val modelSetJson: String,
    val archived: Boolean = false
)

/**
 * A message inside a conversation. Assistant messages are per-model. Ordering ensures
 * stable chronological sequence independent from timestamp monotonicity issues.
 */
@Entity(
    tableName = "message",
    indices = [
        Index("conversationId"),
        Index("ordering"),
        Index("createdAt")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val ordering: Long,
    val role: String, // system | user | assistant
    val model: String? = null, // AIModel.name for assistant
    val content: String,
    val status: String, // streaming | completed | error | canceled
    val errorMessage: String? = null,
    val revision: Int = 0,
    val parentMessageId: Long? = null,
    val createdAt: Long
)
