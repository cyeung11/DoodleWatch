package com.jkjk.doodlewatch

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HomeActTest {

    @get: Rule
    var activityRule = ActivityScenarioRule(HomeAct::class.java)

    companion object {
        @BeforeClass
        @JvmStatic
        fun timeoutPolicy() {
            IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS)
            IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS)
        }
    }

    @Test
    fun testShowDialog() {
        onView(ViewMatchers.withId(R.id.rvDraw))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testShowToastWhenSync() {
            onView(ViewMatchers.withId(R.id.menuSync))
                .perform(ViewActions.click())

            onView(ViewMatchers.withText(R.string.syncing))
                .inRoot(ToastMatcher())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

}