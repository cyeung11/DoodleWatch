package com.jkjk.doodlewatch.core.act

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jkjk.doodlewatch.core.DoodleWatchApp

/**
 *Created by chrisyeung on 26/3/2021.
 */
abstract class BaseAct : AppCompatActivity() {
    protected abstract val layoutResId: Int

    val myApp: DoodleWatchApp
        get() = application as DoodleWatchApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layoutResId != 0) {
            setContentView(layoutResId)
        }
    }

    protected open fun setupBody() {

    }
}