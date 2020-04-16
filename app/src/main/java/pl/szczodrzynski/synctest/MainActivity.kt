package pl.szczodrzynski.synctest

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat.startActivity
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.view.View
import android.widget.Toast
import androidx.work.*
import androidx.work.impl.WorkManagerImpl


class MainActivity : AppCompatActivity() {

    private val workManager = WorkManager.getInstance(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workManagerUpdateInfo()

        workManagerSchedule20min.setOnClickListener {
            /*val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()*/

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInitialDelay(30, TimeUnit.SECONDS)
                //.setConstraints(constraints)
                .addTag(SyncWorker.TAG)
                .build()

            workManager.enqueue(syncWorkRequest)
            workManagerUpdateInfo()
        }

        workManagerClear.setOnClickListener {
            workManager.cancelAllWorkByTag(SyncWorker.TAG)
            workManagerUpdateInfo()
        }
        workManagerPrune.setOnClickListener {
            workManager.pruneWork()
            workManagerUpdateInfo()
        }
        workManagerRefresh.setOnClickListener {
            workManagerUpdateInfo()
        }



        optSystem.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                intent.action = ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Needs at least Marshmallow", Toast.LENGTH_LONG).show()
            }
        }

        optHuaweiAppStartup.setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
            startActivity(intent)
        }

        optAsusAutoStart.setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.MainActivity"
            )
            startActivity(intent)
        }

        optAlcatelAppStrategy.setOnClickListener {
            val intent = Intent()
            intent.component = ComponentName(
                "com.tct.onetouchbooster",
                "com.tct.onetouchbooster.module.appstrategy.activity.AppStrategyActivity"
            )
            startActivity(intent)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun workManagerUpdateInfo() {
        workManager.getWorkInfosByTag(SyncWorker.TAG).get().let { workInfos ->
            AsyncTask.execute {
                val workManager = workManager as WorkManagerImpl
                val scheduledWork = workManager.workDatabase.workSpecDao().scheduledWork
                // remove finished work and other than SyncWorker
                scheduledWork.removeAll { it.workerClassName != SyncWorker::class.java.canonicalName || it.isPeriodic || it.state.isFinished }
                // remove all enqueued work that had to (but didn't) run at some point in the past (at least 2min ago)
                val hasFailedWork = scheduledWork.removeAll { it.state == WorkInfo.State.ENQUEUED && it.periodStartTime+it.initialDelay < System.currentTimeMillis() - 15*1000 }
                failedText.post {
                    failedText.visibility = if (hasFailedWork) View.VISIBLE else View.GONE
                }
            }

            val sb = StringBuilder()
            workInfos.forEachIndexed { index, workInfo ->
                sb.append("${index+1} - ${workInfo.id} STATE = ${workInfo.state}\n\n")
            }
            workManagerInfo.text = sb.toString()
        }
    }
}
