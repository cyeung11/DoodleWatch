package com.jkjk.doodlewatch

import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.TypeSafeMatcher


class ToastMatcher : TypeSafeMatcher<Root?>() {

    override fun describeTo(description: org.hamcrest.Description?) {
        description?.appendText("is toast")
    }

    override fun matchesSafely(item: Root?): Boolean {
        val type = item?.getWindowLayoutParams()?.get()?.type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken = item.decorView?.windowToken
            val appToken = item.decorView?.applicationWindowToken
            if (windowToken === appToken) {
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true
            }
        }
        return false
    }

}
