package com.example.elormovil.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import pl.droidsonroids.gif.GifDrawable

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifImageView = findViewById<pl.droidsonroids.gif.GifImageView>(R.id.gifImageView)
        gifImageView.setImageDrawable(GifDrawable(resources, R.drawable.elorrieta_logo))

        // Llamada para obtener clave pública
        RetrofitClient.apiService.getPublicKey().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful && response.body() != null) {
                    val publicKey = response.body()!!["publicKey"]
                    if (!publicKey.isNullOrEmpty()) {
                         Toast.makeText(this@SplashActivity, "Clave pública recibida", Toast
                             .LENGTH_SHORT).show()

                        // Abrir LoginActivity pasando la clave pública
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            intent.putExtra("PUBLIC_KEY", publicKey)
                            startActivity(intent)
                            finish()
                        }, 500) // pequeño retraso para mostrar GIF
                    } else {
                        Toast.makeText(this@SplashActivity, "Clave pública vacía", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@SplashActivity, "Error al obtener clave pública", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Fallo conexión servidor: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
