package com.example.appmovilsimuladorweb

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmailFormActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_form)

        // Inicializar vistas
        emailEditText = findViewById(R.id.textInputEditText)
        saveButton = findViewById(R.id.button)

        // Configurar clic del botón guardar
        saveButton.setOnClickListener {
            // Obtener el correo electrónico ingresado por el usuario
            val email = emailEditText.text.toString()

            // Guardar el correo electrónico en SharedPreferences
            saveEmailToSharedPreferences(email)

            // Finalizar la actividad
            finish()
        }
    }

    private fun saveEmailToSharedPreferences(email: String) {
        // Obtener la referencia a SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "MiPreferencia",
            Context.MODE_PRIVATE
        )

        // Guardar el correo electrónico en SharedPreferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.apply()

        val storedEmail: String? = sharedPreferences.getString("email", null)

        Toast.makeText(this, "Correo electrónico almacenado: $storedEmail", Toast.LENGTH_SHORT).show()

    }
}
