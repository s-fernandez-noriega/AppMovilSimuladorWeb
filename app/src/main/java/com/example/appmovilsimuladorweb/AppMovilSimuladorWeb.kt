package com.example.appmovilsimuladorweb

import NotificationWorker
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AppMovilSimuladorWeb : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("AppMovilSimuladorWeb", "AppMovilSimuladorWeb iniciada")

        createNotificationChannel()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelAllWorkByTag(NotificationWorker.WORK_TAG)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requiere conexión a Internet
            .build()

        val notificationWorkRequest = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java, 15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(NotificationWorker.WORK_TAG) // Agrega la etiqueta al trabajo
            .build()

        workManager.enqueue(notificationWorkRequest)

    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "notification_channel"
            val channelName = "Channel Name"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}