package pl.szczodrzynski.synctest

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.os.Build
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.PowerManager



class App : Application() {
    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()

        var tag = "pl.szczodrzynski.synctest:LOCK"

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && Build.MANUFACTURER == "Huawei") {
            tag = "LocationManagerService"
        }

        val wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(1, tag)
        wakeLock.acquire()

        // This is the Notification Channel ID. More about this in the next section
        val NOTIFICATION_CHANNEL_ID = "TEST_CHANNEL_WORK_MANAGER"
        val CHANNEL_NAME = "Work Manager"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance)
            //Boolean value to set if lights are enabled for Notifications from this Channel
            notificationChannel.enableLights(true)
            //Boolean value to set if vibration is enabled for Notifications from this Channel
            notificationChannel.enableVibration(true)
            //Sets the color of Notification Light
            notificationChannel.lightColor = Color.GREEN
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}