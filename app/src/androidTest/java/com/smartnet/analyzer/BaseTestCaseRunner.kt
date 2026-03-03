package com.smartnet.analyzer

import android.Manifest.permission
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.lang.reflect.Field
import java.lang.reflect.Method

@RunWith(AndroidJUnit4::class)
abstract class BaseTestCaseRunner {

    @get:Rule
    var testName: TestName = TestName()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        permission.INTERNET,
        permission.READ_PHONE_STATE,
        permission.ACCESS_FINE_LOCATION,
    )

    @get:Rule
    var watcher: TestRule = object : TestWatcher() {
        override fun starting(description: Description) {
            super.starting(description)
            LogFeast.info("Test started: $description")
        }

        override fun succeeded(description: Description) {
            super.succeeded(description)
            LogFeast.info("Test success: $description")
        }

        override fun failed(e: Throwable, description: Description) {
            super.failed(e, description)
            LogFeast.info("Test failed: $description")
            LogFeast.warn("Test failed Error: ",e)
        }
    }

    companion object {

        @OptIn(ExperimentalTestApi::class)
        fun ComposeContentTestRule.waitUntilExists(
            matcher: SemanticsMatcher,
            timeoutMillis: Long = 5000L
        ) {
            return waitUntilNodeCount(matcher, 1, timeoutMillis)
        }

        fun usePrivateMethod(methodClass: Class<*>, methodName: String): Method {
            val method = methodClass.getDeclaredMethod(methodName)
            method.isAccessible = true
            return method
        }

        fun usePrivateMethodParameterOne(methodClass:Class<*>, methodName: String, mParameter :Class<*>): Method {
            val method = methodClass.getDeclaredMethod(methodName,mParameter)
            method.isAccessible = true
            return method
        }

        fun usePrivateMethodParameterTwo(methodClass:Class<*>, methodName: String, parameter1 :Class<*>, parameter2 :Class<*>): Method {
            val method = methodClass.getDeclaredMethod(methodName, parameter1, parameter2)
            method.isAccessible = true
            return method
        }

        fun usePrivateMethodParameterFour(methodClass:Class<*>, methodName: String, mParameter1 :Class<*>,mParameter2 :Class<*>,mParameter3 :Class<*>,mParameter4 :Class<*>): Method {
            val method = methodClass.getDeclaredMethod(methodName,mParameter1,mParameter2,mParameter3,mParameter4)
            method.isAccessible = true
            return method
        }

        fun usePrivateVariable(methodClass: Class<*>, variableName: String): Field {
            val mField = methodClass.getDeclaredField(variableName)
            mField.isAccessible = true
            return mField
        }
    }
}