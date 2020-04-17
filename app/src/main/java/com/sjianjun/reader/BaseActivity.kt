package com.sjianjun.reader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.coroutineScope
import com.gyf.immersionbar.ImmersionBar
import com.sjianjun.reader.utils.handler
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).init()
    }

    fun viewLaunch(context: CoroutineContext = handler,
                   start: CoroutineStart = CoroutineStart.DEFAULT,
                   block: suspend CoroutineScope.() -> Unit): Job {
        return lifecycle.coroutineScope.launch(context, start, block)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}