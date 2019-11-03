package pl.szczodrzynski.synctest

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SyncWorker(val ctx: Context, val params: WorkerParameters) : Worker(ctx, params) {
    companion object {
        const val TAG = "SyncWorker"
    }

    override fun doWork(): Result {

        val date = Date();
        val formatter = SimpleDateFormat("HH:mm:ss")
        val answer: String = formatter.format(date)

        val notification = NotificationCompat.Builder(ctx, "TEST_CHANNEL_WORK_MANAGER").apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Work manager")
            setContentText("Ran at $answer - ID ${params.id}")
        }.build()
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag(TAG)
            .build()

        WorkManager.getInstance(ctx).enqueue(syncWorkRequest)

        return Result.success()
    }
}