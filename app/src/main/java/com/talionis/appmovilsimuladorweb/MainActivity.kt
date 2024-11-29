package com.talionis.appmovilsimuladorweb

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
import com.onesignal.OneSignal

class MainActivity : AppCompatActivity() {

    private lateinit var loadingLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView

    private var customTabsSession: CustomTabsSession? = null
    private var isServiceBound = false

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(
            componentName: ComponentName,
            client: CustomTabsClient
        ) {
            // Establece la CustomTabsSession
            customTabsSession = client.newSession(null)
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            customTabsSession = null
            isServiceBound = false
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
        if (storedEmail != null) {
            // Asocia el correo electrónico con el usuario actual
            //OneSignal.User.addEmail(storedEmail)
            OneSignal.login(storedEmail)
            Toast.makeText(this, "Correo electrónico almacenado: $storedEmail "+  OneSignal.User.onesignalId , Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(this, "No se encontró correo electrónico almacenado.", Toast.LENGTH_SHORT).show()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://cuidacontic.citic.udc.es/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()




        // Inicia la conexión con el servicio de Chrome Custom Tabs
        CustomTabsClient.bindCustomTabsService(this, packageName, connection)


        // Crea un Intent para la actividad EmailFormActivity
        val emailFormIntent = Intent(this, EmailFormActivity::class.java)

        //Crea un PendingIntent con el Intent de la actividad EmailFormActivity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            emailFormIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val url = "https://cuidacontic.citic.udc.es/Login"

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





    override fun onDestroy() {
        super.onDestroy()
        // Detiene la conexión con el servicio de Chrome Custom Tabs solo si está enlazado
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}
