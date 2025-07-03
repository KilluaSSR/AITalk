package killua.dev.aitalk.models

import androidx.annotation.StringRes
import killua.dev.aitalk.BuildConfig
import killua.dev.aitalk.R

data class LanguageMenuItem(
    @StringRes val nameResId: Int,
    val localeTag: String
)

val localeToNameResMap: Map<String, Int> = mapOf(
    "en"    to R.string.english,
    "ja"    to R.string.japanese,
    "zh-CN" to R.string.zh_cn,
    "zh-HK" to R.string.zh_hk,
    "auto" to R.string.system
)

val supportedLanguageMenuItems: List<LanguageMenuItem> =
            BuildConfig.SUPPORTED_LOCALES.mapNotNull { localeTag ->
                val nameResId = localeToNameResMap[localeTag]
                nameResId?.let {
                    LanguageMenuItem(nameResId = it, localeTag = localeTag)
                }
            }