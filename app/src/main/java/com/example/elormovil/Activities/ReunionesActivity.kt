package com.example.elormovil.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elormovil.Activities.CrearReunionActivity
import com.example.elormovil.adapters.ReunionAdapter
import com.example.elormovil.databinding.ActivityReunionesBinding
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Reunion
import com.example.elormovil.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReunionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReunionesBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReunionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("USER") as User

        binding.recyclerReuniones.layoutManager = LinearLayoutManager(this)

        binding.btnCrear.setOnClickListener {
            startActivity(
                Intent(this, CrearReunionActivity::class.java)
                    .putExtra("USER", user)
            )
        }

        cargarReuniones()
    }

    private fun cargarReuniones() {

        val call = if (user.tipoId == 3)
            RetrofitClient.apiService.getReunionesProfesor(user.id)
        else
            RetrofitClient.apiService.getReunionesAlumno(user.id)

        call.enqueue(object : Callback<List<Reunion>> {

            override fun onResponse(
                call: Call<List<Reunion>>,
                response: Response<List<Reunion>>
            ) {

                if (response.isSuccessful) {


                    binding.recyclerReuniones.adapter =
                        ReunionAdapter(
                            this@ReunionesActivity,         // <- contexto correcto
                            response.body() ?: emptyList(),
                            user
                        ) {
                            cargarReuniones()
                        }



                } else {
                    Toast.makeText(this@ReunionesActivity, "Error cargando", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Reunion>>, t: Throwable) {
                Toast.makeText(this@ReunionesActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}
