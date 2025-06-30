package killua.dev.aitalk.utils

import android.content.Context
import killua.dev.aitalk.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Context.timestampToDate(timestamp: Long): String {
    val pattern = this.getString(R.string.date_format)
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    return dateTime.format(formatter)
}