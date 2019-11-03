package pl.szczodrzynski.synctest

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat.startActivity
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType


class MainActivity : AppCompatActivity() {

    private val workManager = WorkManager.getInstance(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workManagerUpdateInfo()

        workManagerSchedule20min.setOnClickListener {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInitialDelay(15, TimeUnit.SECONDS)
                .setConstraints(constraints)
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

    private fun workManagerUpdateInfo() {
        workManager.getWorkInfosByTag(SyncWorker.TAG).get().let {
            val sb = StringBuilder()
            it.forEachIndexed { index, workInfo ->
                sb.append("${index+1} - ${workInfo.id} STATE = ${workInfo.state}\n\n")
            }
            workManagerInfo.text = sb.toString()
        }
    }
}
