package com.vermouthx.stocker.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class StockerPinyinUtilTest {

    @Test
    fun `returns empty input unchanged`() {
        assertEquals("", StockerPinyinUtil.toPinyin(""))
    }

    @Test
    fun `returns non Chinese input unchanged`() {
        assertEquals("BRK.B-123", StockerPinyinUtil.toPinyin("BRK.B-123"))
    }

    @Test
    fun `converts Chinese characters to capitalized pinyin`() {
        assertEquals("GuiZhouMaoTai", StockerPinyinUtil.toPinyin("贵州茅台"))
    }

    @Test
    fun `preserves mixed input while converting Chinese characters`() {
        assertEquals("SH600519 GuiZhou，MaoTai!", StockerPinyinUtil.toPinyin("SH600519 贵州，茅台!"))
    }
}
