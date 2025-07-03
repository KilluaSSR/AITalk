package killua.dev.aitalk.models

import androidx.annotation.StringRes
import killua.dev.aitalk.BuildConfig
import killua.dev.aitalk.R
import killua.dev.aitalk.consts.SYSTEM_LOCALE_TAG

data class LanguageMenuItem(
    @StringRes val nameResId: Int,
    val localeTag: String
)

private val localeToNameResMap: Map<String, Int> = mapOf(
    "en"    to R.string.english,
    "ja"    to R.string.japanese,
    "zh-CN" to R.string.zh_cn,
    "zh-HK" to R.string.zh_hk
)

val supportedLanguageMenuItems: List<LanguageMenuItem> =
    listOf(
        LanguageMenuItem(
            nameResId = R.string.system,
            localeTag = SYSTEM_LOCALE_TAG
        )
    ) +
            BuildConfig.SUPPORTED_LOCALES.mapNotNull { localeTag ->
                localeToNameResMap[localeTag]?.let { nameResId ->
                    LanguageMenuItem(nameResId = nameResId, localeTag = localeTag)
                }
            }