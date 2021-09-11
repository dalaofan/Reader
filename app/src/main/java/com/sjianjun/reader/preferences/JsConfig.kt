package com.sjianjun.reader.preferences

import androidx.core.content.edit
import com.sjianjun.reader.bean.JavaScript
import com.sjianjun.reader.utils.fromJson
import com.sjianjun.reader.utils.gson
import com.tencent.mmkv.MMKV
import sjj.alog.Log

object JsConfig : DelegateSharedPref(MMKV.mmkvWithID("AppConfig_JsConfig")) {
    var localJsVersion by intPref("localJsVersion", 0)

    /**
     * 全部小说源
     */
    var allJsSource by dataPref<MutableSet<String>>("allJsSource", mutableSetOf())
        private set
    private val allJs: MutableMap<String, JavaScript?> = mutableMapOf()

    fun saveJs(js: JavaScript) {
        edit {
            putString("Js_${js.source}", gson.toJson(js))
        }
        if (!allJsSource.contains(js.source)) {
            allJsSource = allJsSource.apply {
                add(js.source)
            }
        }
        allJs[js.source] = js
        Log.i("保存脚本:${js.source}")
    }

    fun removeJs(vararg sources: String) {
        if (sources.isEmpty()) {
            return
        }
        allJsSource = allJsSource.apply {
            sources.forEach {
                remove(it)
                allJs.remove(it)
            }
        }

        edit {
            sources.forEach {
                remove("Js_${it}")
            }
        }

        Log.i("删除脚本:${sources.toList()}")
    }

    fun getJs(source: String): JavaScript? {
        if (allJs.containsKey(source)) {
            return allJs[source]
        }
        val script = gson.fromJson<JavaScript>(getString("Js_${source}", null))
        allJs[source] = script
        return script
    }

    fun getAllJs(): List<JavaScript> {
        val source2 = allJsSource.toMutableSet()
        allJs.forEach { (t, _) ->
            source2.remove(t)
        }
        source2.forEach {
            getJs(it)
        }
        return allJs.values.mapNotNull { it }
    }

}