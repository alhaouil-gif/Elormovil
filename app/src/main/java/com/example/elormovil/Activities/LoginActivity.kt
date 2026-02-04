package com.example.elormovil.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.LoginRequest
import com.example.elormovil.model.User
import com.example.elormovil.utils.RsaEncryptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var publicKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        publicKey = intent.getStringExtra("PUBLIC_KEY") ?: ""
        if (publicKey.isBlank()) {
            Toast.makeText(this, "Clave pública no disponible", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnForgot = findViewById<Button>(R.id.btnForgotPassword)

        // -------------------------------
        // AUTOCOMPLETAR USUARIO Y CONTRASEÑA
        // -------------------------------
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val lastUser = prefs.getString("LAST_USER", "")
        val lastPass = prefs.getString("LAST_PASS", "")

        if (!lastUser.isNullOrEmpty()) etUsername.setText(lastUser)
        if (!lastPass.isNullOrEmpty()) etPassword.setText(lastPass)

        // -------------------------------
        // LOGIN
        // -------------------------------
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordCifrada = RsaEncryptor.encrypt(password, publicKey)
            val loginRequest = LoginRequest(username, passwordCifrada)

            RetrofitClient.apiService.login(loginRequest)
                .enqueue(object : Callback<User> {

                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        when {
                            response.isSuccessful -> {
                                val user = response.body()!!

                                // Guardar usuario y contraseña
                                prefs.edit()
                                    .putString("LAST_USER", user.username)
                                    .putString("LAST_PASS", password)
                                    .apply()

                                val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                intent.putExtra("USER", user)
                                startActivity(intent)
                                finish()
                            }

                            response.code() == 404 ->
                                Toast.makeText(this@LoginActivity, "Usuario no existe", Toast.LENGTH_SHORT).show()

                            response.code() == 401 ->
                                Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()

                            else ->
                                Toast.makeText(this@LoginActivity, "Error de login: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // -------------------------------
        // OLVIDÉ CONTRASEÑA
        // -------------------------------
        btnForgot.setOnClickListener {
            val emailInput = EditText(this)
            emailInput.hint = "Introduce tu email"
            emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Recuperar contraseña")
                .setMessage("Introduce tu email para recibir la nueva contraseña")
                .setView(emailInput)
                .setPositiveButton("Enviar") { dialog, _ ->
                    val email = emailInput.text.toString().trim()

                    if (email.isBlank()) {
                        Toast.makeText(this, "Email vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        RetrofitClient.apiService.recoverPassword(mapOf("email" to email))
                            .enqueue(object : Callback<Map<String, String>> {

                                override fun onResponse(
                                    call: Call<Map<String, String>>,
                                    response: Response<Map<String, String>>
                                ) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Se ha enviado la nueva contraseña a tu email",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Error al recuperar contraseña: ${response.code()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Error de conexión: ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}
