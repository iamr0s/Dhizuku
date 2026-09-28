package com.rosan.dhizuku.ui.activity

import android.app.Activity
import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowInstrumentation

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [27, 37])
class SettingsActivityTest {
    private lateinit var activity: SettingsActivity
    private lateinit var powerManager: PowerManager
    private lateinit var devicePolicyManager: DevicePolicyManager

    @Before
    fun setUp() {
        // ShadowInstrumentation reports a missing handler only when this strict check is enabled.
        requireStrictActivityResolution()
        // Attach without create(), so Compose and Koin startup in onCreate do not run.
        activity = Robolectric.buildActivity(SettingsActivity::class.java).get()
        powerManager = activity.getSystemService(PowerManager::class.java)!!
        devicePolicyManager = activity.getSystemService(DevicePolicyManager::class.java)!!
        shadowOf(powerManager).setIgnoringBatteryOptimizations(activity.packageName, false)
    }

    @Test
    fun notExemptWithMatchingActivityLaunchesOneRequest() {
        installBatteryOptimizationSettingsActivity()
        assertNotNull(resolveBatteryRequest())
        assertNull(shadowOf(activity).peekNextStartedActivityForResult())

        activity.requestIgnoreBatteryOptimization()

        val started = shadowOf(activity).nextStartedActivityForResult
        assertNotNull(started)
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, started.intent.action)
        assertEquals("com.rosan.dhizuku", activity.packageName)
        assertEquals("package:com.rosan.dhizuku", started.intent.dataString)
        assertNull(shadowOf(activity).nextStartedActivityForResult)
    }

    @Test
    fun notExemptWithoutMatchingActivityReturnsNormally() {
        assertNull(resolveBatteryRequest())

        activity.requestIgnoreBatteryOptimization()

        assertNull(shadowOf(activity).nextStartedActivityForResult)
        assertFalse(powerManager.isIgnoringBatteryOptimizations(activity.packageName))
    }

    @Test
    fun alreadyExemptDoesNotLaunchEvenWhenNoHandlerExists() {
        shadowOf(powerManager).setIgnoringBatteryOptimizations(activity.packageName, true)
        assertNull(resolveBatteryRequest())

        activity.requestIgnoreBatteryOptimization()

        assertNull(shadowOf(activity).nextStartedActivityForResult)

        installBatteryOptimizationSettingsActivity()
        activity.requestIgnoreBatteryOptimization()

        assertNull(shadowOf(activity).nextStartedActivityForResult)
        assertTrue(powerManager.isIgnoringBatteryOptimizations(activity.packageName))
    }

    @Test
    fun repeatedRequestsWithoutPanelDoNotPersistExemptionOrDeviceOwner() {
        val owner = ComponentName("com.example.owner", "com.example.owner.Admin")
        shadowOf(devicePolicyManager).setDeviceOwner(owner)
        assertNull(resolveBatteryRequest())
        assertTrue(devicePolicyManager.isDeviceOwnerApp(owner.packageName))
        assertFalse(devicePolicyManager.isDeviceOwnerApp(activity.packageName))

        activity.requestIgnoreBatteryOptimization()
        activity.requestIgnoreBatteryOptimization()

        assertNull(shadowOf(activity).nextStartedActivityForResult)
        assertFalse(powerManager.isIgnoringBatteryOptimizations(activity.packageName))
        assertTrue(devicePolicyManager.isDeviceOwnerApp(owner.packageName))
        assertFalse(devicePolicyManager.isDeviceOwnerApp(activity.packageName))
    }

    @Test
    @Config(shadows = [SecurityExceptionStartShadow::class])
    fun securityExceptionFromLaunchIsNotSwallowed() {
        installBatteryOptimizationSettingsActivity()
        assertFalse(powerManager.isIgnoringBatteryOptimizations(activity.packageName))

        assertThrows(SecurityException::class.java) {
            activity.requestIgnoreBatteryOptimization()
        }

        assertFalse(powerManager.isIgnoringBatteryOptimizations(activity.packageName))
        assertFalse(devicePolicyManager.isDeviceOwnerApp(activity.packageName))
    }

    private fun batteryRequestIntent(): Intent {
        return Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            "package:${activity.packageName}".toUri()
        )
    }

    private fun resolveBatteryRequest() =
        activity.packageManager.resolveActivity(
            batteryRequestIntent(),
            PackageManager.MATCH_DEFAULT_ONLY
        )

    private fun installBatteryOptimizationSettingsActivity() {
        val component = ComponentName(
            "com.android.settings",
            "com.android.settings.fuelgauge.RequestIgnoreBatteryOptimizations"
        )
        val shadowPackageManager = shadowOf(activity.packageManager)
        val activityInfo = shadowPackageManager.addActivityIfNotPresent(component)
        activityInfo.exported = true
        activityInfo.enabled = true
        val filter = IntentFilter(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addDataScheme("package")
        shadowPackageManager.addIntentFilterForActivity(component, filter)
    }

    private fun requireStrictActivityResolution() {
        val shadowInstrumentation = shadowOf(ShadowInstrumentation.getInstrumentation())
        var type: Class<*>? = shadowInstrumentation.javaClass
        var method: java.lang.reflect.Method? = null
        while (type != null && method == null) {
            method = type.declaredMethods.firstOrNull { candidate ->
                candidate.name == "checkActivities" && candidate.parameterTypes.size == 1
            }
            type = type.superclass
        }
        checkNotNull(method) { "ShadowInstrumentation.checkActivities is unavailable" }
        method.isAccessible = true
        method.invoke(shadowInstrumentation, true)
    }
}

@Implements(Activity::class)
class SecurityExceptionStartShadow : ShadowActivity() {
    @Implementation
    fun startActivity(intent: Intent) {
        throw SecurityException("unrelated launch failure: ${intent.action}")
    }
}
