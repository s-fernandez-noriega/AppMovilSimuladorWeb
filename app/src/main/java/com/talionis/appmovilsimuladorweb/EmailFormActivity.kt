package com.talionis.appmovilsimuladorweb

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EmailFormActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_form)

        // Inicializar vistas
        emailEditText = findViewById(R.id.textInputEditText)
        saveButton = findViewById(R.id.button)

        // Configurar clic del botón guardar
        saveButton.setOnClickListener {
            // Obtener el correo electrónico ingresado por el usuario
            val email = emailEditText.text.toString().trim()

            // Realizar la llamada para verificar el email
            checkEmailOnServer(email)
        }

        // Detectar cuando se presiona "Enter" o "Hecho" en el teclado
        emailEditText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Obtener el correo electrónico ingresado por el usuario
                val email = emailEditText.text.toString().trim()

                // Realizar la llamada para verificar el email
                checkEmailOnServer(email)

                true // Indica que el evento ha sido manejado
            } else {
                false // Permitir que otros manejadores lo procesen
            }
        })

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend.talionis.eu:8443")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crear instancia de ApiService
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun checkEmailOnServer(email: String) {
        val emailRequest = EmailRequest(email)
        val call: Call<Boolean> = apiService.checkEmail(emailRequest)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val emailExists = response.body() ?: false
                    if (emailExists) {
                        // El email existe en el servidor, guardarlo en SharedPreferences
                        saveEmailLocally(email)
                    } else {
                        // El email no existe en el servidor, mostrar diálogo
                        showEmailExistsDialog(email)
                    }
                } else {
                    // Manejar el fallo de la solicitud
                    Toast.makeText(
                        applicationContext,
                        "Error al verificar el email en el servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                // Manejar el fallo de la solicitud
                Toast.makeText(
                    applicationContext,
                    "Error al conectar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
                // Imprimir el error en el Logcat
                Log.e("API_CALL_ERROR", "Error en la solicitud: ${t.message}", t)
            }
        })
    }

    private fun saveEmailLocally(email: String) {
        // Guardar el correo electrónico en SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "MiPreferencia",
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.apply()

        val storedEmail: String? = sharedPreferences.getString("email", null)

        Log.d("REGISTRO EMAIL", "Correo electrónico almacenado: $storedEmail")

        // Iniciar la MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showEmailExistsDialog(email: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("El email '$email' no está registrado. Pulsa continuar si te vas registrar, en caso contrario vuelve a introducir el email.")
            .setCancelable(false)
            .setPositiveButton("Volver a introducir email") { dialog, id ->
                // Permitir al usuario volver a introducir el email
                dialog.dismiss()
            }
            .setNegativeButton("Continuar") { dialog, id ->

                saveEmailLocally(email)

                // Iniciar la MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Finalizar la actividad actual
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Email no registrado")
        alert.show()
    }
}
