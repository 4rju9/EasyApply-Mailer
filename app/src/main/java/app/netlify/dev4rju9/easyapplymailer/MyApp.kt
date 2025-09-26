package app.netlify.dev4rju9.easyapplymailer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import app.netlify.dev4rju9.easyapplymailer.utils.Utility.getNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationManager(applicationContext)
                .createNotificationChannel(
                    NotificationChannel(
                        "easy_apply_mailer",
                        "EasyApply Mailer",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
        }

    }

}