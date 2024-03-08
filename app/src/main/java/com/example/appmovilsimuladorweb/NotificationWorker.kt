import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.JsonParser
import com.example.appmovilsimuladorweb.ApiService
import com.example.appmovilsimuladorweb.MainActivity
import com.example.appmovilsimuladorweb.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_TAG = "notification_work"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "MiPreferencia",
        Context.MODE_PRIVATE
    )

    override suspend fun doWork(): Result {
        try {
            Log.d("CONSULTA NOTIFICACIONES", "Worker iniciado")

            val email = sharedPreferences.getString("email", null)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://backend.talionis.eu:8443")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            val call = apiService.getNotificacion(email)

            Log.d("CONSULTA NOTIFICACIONES", "Petición enviada")

            val response = call.execute()

            if (response.isSuccessful) {
                val responseBody = response.body()?.toString()

                if (responseBody != null) {
                    val trimmedResponse = responseBody.trim()
                    Log.d("CONSULTA NOTIFICACIONES", "Respuesta: $trimmedResponse")

                    val jsonArray = JsonParser.parseString(trimmedResponse).asJsonArray
                    val textList = mutableListOf<String>()

                    for (jsonElement in jsonArray) {
                        val jsonObject = jsonElement.asJsonObject
                        val text = jsonObject.get("texto").asString
                        textList.add(text)
                    }

                    for (text in textList) {
                        showNotification(text)
                    }
                } else {
                    Log.d("CONSULTA NOTIFICACIONES", "Respuesta vacía o nula")
                }

                return Result.success()
            } else {
                Log.d("CONSULTA NOTIFICACIONES", "Error en la consulta")
                return Result.failure()
            }
        } catch (e: IOException) {
            Log.d("CONSULTA NOTIFICACIONES", "Error en la consulta: ${e.message}")
            return Result.retry()
        } catch (e: Exception) {
            Log.e("CONSULTA NOTIFICACIONES", "Error inesperado: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun showNotification(text: String) {
        val channelId = "notification_channel"

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasa información adicional que indique que la aplicación se abrió desde una notificación
            putExtra("fromNotification", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logotalionis)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent) // Establecer el PendingIntent
            .setAutoCancel(true) // Hacer que la notificación se cancele cuando se hace clic en ella
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NOTIFICACION", "Falta de permisos")
        } else {
            notificationManager.notify(0, notificationBuilder)
            Log.d("NOTIFICACION", "Notificación enviada")
        }
    }
}
