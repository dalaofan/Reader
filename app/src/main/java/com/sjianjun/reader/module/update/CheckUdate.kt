package com.sjianjun.reader.module.update

import android.app.Activity
import android.widget.Toast
import com.azhon.appupdate.manager.DownloadManager
import com.sjianjun.coroutine.withIo
import com.sjianjun.reader.BuildConfig
import com.sjianjun.reader.R
import com.sjianjun.reader.bean.ReleasesInfo
import com.sjianjun.reader.http.http
import com.sjianjun.reader.preferences.globalConfig
import com.sjianjun.reader.utils.*
import org.json.JSONObject
import sjj.alog.Log
import java.util.concurrent.TimeUnit

enum class Channels {
    FastGit {
        override fun getReleaseInfo(): ReleasesInfo {
            val url = "https://raw.fastgit.org/SJJ-dot/readerRepo/main/releases/checkUpdate.json"
            val releasesInfo = gson.fromJson<ReleasesInfo>(http.get(url))!!
            releasesInfo.channel = name
            releasesInfo.downloadApkUrl =
                "https://raw.fastgit.org/SJJ-dot/readerRepo/main/releases/${releasesInfo.lastVersion}/app.apk"
            return releasesInfo
        }
    },
    Github {
        override fun getReleaseInfo(): ReleasesInfo {
            val url = "https://api.github.com/repos/SJJ-dot/Reader/releases/latest"
            val info = JSONObject(http.get(url))

            val releasesInfo = ReleasesInfo()
            releasesInfo.channel = name
            releasesInfo.lastVersion = info.getString("tag_name")
            releasesInfo.updateContent = info.getString("body")
            releasesInfo.downloadApkUrl =
                info.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
            return releasesInfo
        }
    };

    abstract fun getReleaseInfo(): ReleasesInfo

}

suspend fun checkUpdate(ativity: Activity, fromUser: Boolean = false) = withIo {
    if (!fromUser && System.currentTimeMillis() - globalConfig.lastCheckUpdateTime <
        TimeUnit.MINUTES.toMillis(30)
    ) {
        return@withIo
    }
    var releasesInfo: ReleasesInfo? = null
    for (ch in Channels.values()) {
        try {
            releasesInfo = ch.getReleaseInfo()
            globalConfig.releasesInfo = gson.toJson(releasesInfo)
            break
        } catch (e: Exception) {
            Log.e("版本信息加载失败：${e.message}")
        }
    }
    if (releasesInfo == null && !fromUser) {
        val info = globalConfig.releasesInfo
        if (info != null) {
            releasesInfo = gson.fromJson<ReleasesInfo>(info)!!
        }
    }
    if (releasesInfo == null) {
        if (fromUser) {
            toast("版本信息加载失败", Toast.LENGTH_LONG)
        }
        return@withIo
    }
    if (!BuildConfig.DEBUG) {
        globalConfig.lastCheckUpdateTime = System.currentTimeMillis()
    }

    if (releasesInfo.isNewVersion) {
        val manager = DownloadManager.Builder(ativity).run {
            apkUrl(releasesInfo.downloadApkUrl!!)
            apkName("reader-${releasesInfo.lastVersion}.apk")
            smallIcon(R.mipmap.ic_xue_xi)
            //设置了此参数，那么内部会自动判断是否需要显示更新对话框，否则需要自己判断是否需要更新
            apkVersionCode(BuildConfig.VERSION_CODE + 1)
            //同时下面三个参数也必须要设置
            apkVersionName(releasesInfo.lastVersion!!)
            apkDescription(releasesInfo.updateContent!!)
            //省略一些非必须参数...
            build()
        }
        manager.download()
    } else {
        if (fromUser) {
            toast("当前已经是最新版本", Toast.LENGTH_LONG)
        }
    }


}