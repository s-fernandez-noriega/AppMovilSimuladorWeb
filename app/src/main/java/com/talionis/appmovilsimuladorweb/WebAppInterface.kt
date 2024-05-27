import android.content.Context
import android.content.SharedPreferences
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {

    // Nombre del archivo SharedPreferences
    private val sharedPreferencesName = "MySharedPreferences"

    // SharedPreferences
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

    // Variable global para almacenar el correo electrónico
    var receivedEmail: String?
        get() = sharedPreferences.getString("email", null)
        set(value) {
            // Almacena el valor del correo electrónico en SharedPreferences
            sharedPreferences.edit().putString("email", value).apply()
        }

    @JavascriptInterface
    fun sendEmailToAndroid(email: String) {
        // Almacena el valor del correo electrónico en la variable global y SharedPreferences
        receivedEmail = email

        // Puedes realizar otras acciones aquí si es necesario

        // Por ejemplo, mostrar un Toast con el correo electrónico
        Toast.makeText(context, "Email recibido en Android: $email", Toast.LENGTH_SHORT).show()
    }
}
