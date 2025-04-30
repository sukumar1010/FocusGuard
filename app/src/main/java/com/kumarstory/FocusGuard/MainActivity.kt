package com.kumarstory.FocusGuard

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import android.provider.Settings
import android.text.TextUtils


//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            VPNTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//
//                        Surface(
//                            modifier = Modifier.fillMaxSize(),
//                            color = MaterialTheme.colorScheme.background
//                        ) {
//
//                        }
//
//                }
//            }
//        }
//    }
//}

class MainActivity : ComponentActivity() {
    private lateinit var installedApps: List<AppData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BlockManager.loadBlockedApps(this)

        installedApps = getInstalledSocialApps()

        setContent {
            val apps = remember {
                mutableStateListOf<AppData>().apply {
                    addAll(installedApps)
                }
            }

            MainScreen(apps)
        }

    }

    private fun String.containsAny(keywords: List<String>): Boolean {
        return keywords.any { this.contains(it, ignoreCase = true) }
    }

    private fun getInstalledSocialApps(): List<AppData> {
        val pm = this.packageManager
//        val knownSocialApps = listOf(
//            "com.instagram.android",
//            "com.facebook.katana",
//            "com.whatsapp",
//            "com.snapchat.android",
//            "com.twitter.android",
//            "com.threads.android",
//            "com.google.android.youtube"
//        )

//        val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
//        Log.d("AppList", "Installed: ${installedPackages.map { it.packageName }}")
//
//        return installedPackages.filter { appInfo ->
//            knownSocialApps.contains(appInfo.packageName)
//
//        }.map { appInfo ->
//            AppData(
//                appName = pm.getApplicationLabel(appInfo).toString(),
//                packageName = appInfo.packageName,
//                icon = pm.getApplicationIcon(appInfo)
//            )
//        }
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                // Filter out system apps
                pm.getLaunchIntentForPackage(appInfo.packageName) != null
            }.map { appInfo ->
            AppData(
                appName = pm.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                icon = pm.getApplicationIcon(appInfo)
            )
        }
    }


}



object BlockManager {
    var blockedApps = mutableSetOf<String>()
    var blockEndTime: Long = 0L

    fun isBlockingActive(): Boolean {
        return System.currentTimeMillis() < blockEndTime
    }

    fun getRemainingTime(): Long {
        return (blockEndTime - System.currentTimeMillis()).coerceAtLeast(0L)
    }
    fun saveBlockedApps(context: Context) {
        val prefs = context.getSharedPreferences("zen_mode_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet("blocked_apps", blockedApps)
            .apply()
    }

    fun loadBlockedApps(context: Context) {
        val prefs = context.getSharedPreferences("zen_mode_prefs", Context.MODE_PRIVATE)
        blockedApps.clear()
        blockedApps.addAll(prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet())
    }

    fun storeLatestTime(context: Context,time:String){
        val prefs = context.getSharedPreferences("Timer was set about", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("Timer was set about", time)
            .apply()
    }
    fun returnLatestTime(context: Context):String{
        val prefs = context.getSharedPreferences("Timer was set about", Context.MODE_PRIVATE)
        return prefs.getString("Timer was set about","0H : 0M")?:""
    }
}
data class AppData(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    var isBlocked: Boolean = false
)
