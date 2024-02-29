import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonParser
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appmovilsimuladorweb.ApiService
import com.example.appmovilsimuladorweb.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val WORK_TAG = "notification_work"
    }

    override fun doWork(): Result {

        // Obten el valor del correo electrónico
        Log.d("CONSULTA NOTIFICACIONES", "Worker iniciado")
        val webAppInterface = WebAppInterface(applicationContext)

        // Acceder al valor del correo electrónico almacenado en SharedPreferences
        val email = webAppInterface.receivedEmail

        // Configura Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend.talionis.eu:8443")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crea una instancia de tu interfaz ApiService
        val apiService = retrofit.create(ApiService::class.java)

        // Realiza la consulta GET con los parámetros de URL
        val call = apiService.getNotificacion(email)

        Log.d("CONSULTA NOTIFICACIONES", "Petición enviada")

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                // La solicitud fue exitosa, puedes acceder a los datos de la respuesta
                val responseBody = response.body()?.toString() // Obtén el contenido del cuerpo de la respuesta como String

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

                    // Generar notificaciones para cada texto
                    for (text in textList) {
                        showNotification(text)
                    }

                } else {
                    Log.d("CONSULTA NOTIFICACIONES", "Respuesta vacía o nula")
                }


                return Result.success()
            } else {
                // La solicitud no fue exitosa
                Log.d("CONSULTA NOTIFICACIONES", "Error en la consulta")
                return Result.failure()
            }
        } catch (e: IOException) {
            Log.d("CONSULTA NOTIFICACIONES", "Error en la consulta")
            // Ocurrió un error de red
            return Result.retry()
        }
    }

    private fun showNotification(text: String) {

        val channelId = "notification_channel"

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logotalionis)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NOTIFICACION", "Falta de permisos")
            // Aquí puedes manejar la solicitud de permisos si es necesario.
            // Por ejemplo, puedes solicitar permisos al usuario.
        } else {
            notificationManager.notify(0, notificationBuilder)
            Log.d("NOTIFICACION", "Notificación enviada")
        }
    }
}
