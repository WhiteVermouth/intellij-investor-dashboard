package com.vermouthx.stocker.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

object StockerPinyinUtil {
    
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
        vCharType = HanyuPinyinVCharType.WITH_V
    }

    /**
     * Convert Chinese characters to Pinyin
     * @param chinese The Chinese text to convert
     * @return The Pinyin representation, or the original text if conversion fails
     */
    fun toPinyin(chinese: String): String {
        if (chinese.isEmpty()) {
            return chinese
        }

        val result = StringBuilder()
        try {
            for (char in chinese) {
                if (char.isChinese()) {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                    if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                        // Take the first pinyin (in case of multiple pronunciations)
                        result.append(pinyinArray[0].capitalize())
                    } else {
                        result.append(char)
                    }
                } else {
                    result.append(char)
                }
            }
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            // If conversion fails, return the original text
            return chinese
        }

        return result.toString()
    }

    /**
     * Check if a character is a Chinese character
     */
    private fun Char.isChinese(): Boolean {
        val ub = Character.UnicodeBlock.of(this)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION
    }

    /**
     * Capitalize the first character of a string
     */
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
