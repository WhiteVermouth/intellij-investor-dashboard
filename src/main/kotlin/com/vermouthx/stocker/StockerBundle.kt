package com.vermouthx.stocker

import com.intellij.DynamicBundle
import com.vermouthx.stocker.settings.StockerSetting
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.*

private const val BUNDLE = "messages.StockerBundle"

object StockerBundle : DynamicBundle(BUNDLE) {

    private fun getPreferredLocale(): Locale {
        return try {
            val languageOverride = StockerSetting.instance.languageOverride
            when {
                languageOverride.isEmpty() -> Locale.getDefault()
                languageOverride == "zh_CN" -> Locale.SIMPLIFIED_CHINESE
                languageOverride == "en" -> Locale.ENGLISH
                else -> Locale.getDefault()
            }
        } catch (_: Exception) {
            Locale.getDefault()
        }
    }

    private fun getLocalizedBundle(): ResourceBundle {
        val locale = getPreferredLocale()
        return ResourceBundle.getBundle(BUNDLE, locale, StockerBundle::class.java.classLoader)
    }

    @Nls
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        val bundle = getLocalizedBundle()
        val template = bundle.getString(key)
        return if (params.isEmpty()) template else MessageFormat.format(template, *params)
    }

    @Nls
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}
