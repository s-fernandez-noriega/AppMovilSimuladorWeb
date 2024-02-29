package com.example.appmovilsimuladorweb
import WebAppInterface
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var apiService: ApiService

    private lateinit var loadingLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa los componentes de la pantalla de carga
        loadingLayout = findViewById(R.id.loadingLayout)
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)

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


        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings

        // Habilitar JavaScript (si es necesario)
        webSettings.javaScriptEnabled = true

        // Habilitar el almacenamiento DOM (que incluye localStorage)
        webSettings.domStorageEnabled = true
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.90 Mobile Safari/537.36"

        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Configurar el WebViewClient para manejar eventos de carga
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Muestra la pantalla de carga al comenzar la carga de la página
                showLoadingScreen()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                Log.d("WebView", "Página cargada: $url")

                hideLoadingScreen()
            }
        }

        val webAppInterface = WebAppInterface(this)
        webView.addJavascriptInterface(webAppInterface, "AndroidInterface")

        // Cargar la URL principal después de cargar la API de Google
        //SI NO FUNCIONA CARGAR PAGINA /login
        webView.loadUrl("https://cuidacontic.talionis.eu:3000/")

    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // Si hay historial de navegación en el WebView, retrocede una página
            webView.goBack()
        } else {
            // Si no hay historial de navegación, permite que el comportamiento predeterminado maneje el botón de retroceso
            super.onBackPressed()
        }
    }

    private fun showLoadingScreen() {
        loadingLayout.visibility = View.VISIBLE
    }

    private fun hideLoadingScreen() {
        loadingLayout.visibility = View.GONE
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


}