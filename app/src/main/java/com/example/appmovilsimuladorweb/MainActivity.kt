package com.example.appmovilsimuladorweb

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var loadingLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView

    private var customTabsSession: CustomTabsSession? = null

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(
            componentName: ComponentName,
            client: CustomTabsClient
        ) {
            // Establece la CustomTabsSession
            customTabsSession = client.newSession(null)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            customTabsSession = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa los componentes de la pantalla de carga
        loadingLayout = findViewById(R.id.loadingLayout)
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)

        // Verificar si el usuario ha proporcionado su correo electrónico
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val correoElectronico = sharedPreferences.getString("email", null)

        if (correoElectronico == null) {
            // El usuario no ha proporcionado su correo electrónico, iniciar actividad EmailFormActivity
            val intent = Intent(this, EmailFormActivity::class.java)
            startActivity(intent)
            return
        }

        val storedEmail: String? = sharedPreferences.getString("email", null)
        Toast.makeText(this, "Correo electrónico almacenado: $storedEmail", Toast.LENGTH_SHORT).show()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend.talionis.eu:8443")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        Log.d("NOTIFICACION", "Comprobar solicitud permisos notificaciones necesario: " + shouldRequestNotificationPermission(this))

        if (shouldRequestNotificationPermission(this)) {
            Log.d("NOTIFICACION", "Solicitud de permisos necesaria")
            requestNotificationPermission()
        }

        // Inicia la conexión con el servicio de Chrome Custom Tabs
        CustomTabsClient.bindCustomTabsService(this, packageName, connection)

        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        // Crea un Intent para la actividad EmailFormActivity
        val emailFormIntent = Intent(this, EmailFormActivity::class.java)

        // Crea un PendingIntent con el Intent de la actividad EmailFormActivity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            emailFormIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val url = if (fromNotification) "https://cuidacontic.talionis.eu/misAvisos" else "https://cuidacontic.talionis.eu/login"


        val intent = CustomTabsIntent.Builder(customTabsSession)
            .addMenuItem(getString(R.string.cambiar_email_notificaciones), pendingIntent)
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
        intent.launchUrl(this@MainActivity, Uri.parse(url))

        val customTabsCallback = onCustomTabsClosed()
    }

    private fun onCustomTabsClosed() {
        finish()
    }

    private fun shouldRequestNotificationPermission(context: Context): Boolean {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        )
        val isEnabled = notificationManager?.areNotificationsEnabled()
        return !isEnabled!!
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        // Define un código para identificar la solicitud de permiso
        val notificationPermissionCode = 123

        // Verifica si se necesitan permisos y solicítalos
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NOTIFICACION", "Solicitud de permisos necesaria 2")
            // Si no se tienen permisos, solicita los permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                notificationPermissionCode
            )

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                notificationPermissionCode
            )
        } else {
            // Los permisos ya están concedidos, puedes realizar las acciones necesarias
        }

        // Actualiza la preferencia para indicar que se ha solicitado el permiso
        val preferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("notification_permission_requested", true).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detiene la conexión con el servicio de Chrome Custom Tabs
        unbindService(connection)
    }
}
