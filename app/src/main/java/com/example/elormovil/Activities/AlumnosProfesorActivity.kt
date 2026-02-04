package com.example.elormovil.Activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elormovil.Adapters.AlumnosAdapter
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlumnosProfesorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoAlumnos: TextView
    private lateinit var etBuscarAlumno: EditText
    private lateinit var user: User

    private var alumnosCompletos: List<User> = emptyList()
    private var alumnosFiltrados: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alumnos_profesor)

        // Obtener usuario del intent
        user = intent.getSerializableExtra("USER") as? User ?: run {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verificar que sea profesor
        if (user.tipoId != 3) {
            Toast.makeText(this, "Solo los profesores pueden acceder a esta sección", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewAlumnos)
        progressBar = findViewById(R.id.progressBar)
        tvNoAlumnos = findViewById(R.id.tvNoAlumnos)
        etBuscarAlumno = findViewById(R.id.etBuscarAlumno)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar búsqueda
        configurarBusqueda()

        // Cargar alumnos
        cargarAlumnos()
    }

    private fun configurarBusqueda() {
        etBuscarAlumno.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarAlumnos(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarAlumnos(texto: String) {
        if (alumnosCompletos.isEmpty()) return

        alumnosFiltrados = if (texto.isEmpty()) {
            alumnosCompletos
        } else {
            alumnosCompletos.filter { alumno ->
                val nombreCompleto = "${alumno.nombre} ${alumno.apellidos}".lowercase()
                nombreCompleto.contains(texto.lowercase())
            }
        }

        actualizarListaAlumnos(alumnosFiltrados)
    }

    private fun actualizarListaAlumnos(alumnos: List<User>) {
        if (alumnos.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoAlumnos.visibility = View.VISIBLE
            tvNoAlumnos.text = if (etBuscarAlumno.text.isNotEmpty()) {
                "No se encontraron alumnos con ese nombre"
            } else {
                "No tienes alumnos asignados"
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoAlumnos.visibility = View.GONE
            val adapter = AlumnosAdapter(alumnos)
            recyclerView.adapter = adapter
        }
    }

    private fun cargarAlumnos() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoAlumnos.visibility = View.GONE
        etBuscarAlumno.isEnabled = false

        RetrofitClient.apiService.getAlumnosProfesor(user.id)
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    progressBar.visibility = View.GONE
                    etBuscarAlumno.isEnabled = true

                    if (response.isSuccessful) {
                        val alumnos = response.body()

                        if (alumnos.isNullOrEmpty()) {
                            alumnosCompletos = emptyList()
                            alumnosFiltrados = emptyList()
                            tvNoAlumnos.visibility = View.VISIBLE
                            tvNoAlumnos.text = "No tienes alumnos asignados"
                        } else {
                            alumnosCompletos = alumnos
                            alumnosFiltrados = alumnos
                            actualizarListaAlumnos(alumnosFiltrados)
                        }
                    } else {
                        Toast.makeText(
                            this@AlumnosProfesorActivity,
                            "Error al cargar alumnos: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        tvNoAlumnos.visibility = View.VISIBLE
                        tvNoAlumnos.text = "Error al cargar los alumnos"
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    tvNoAlumnos.visibility = View.VISIBLE
                    tvNoAlumnos.text = "Error de conexión"
                    etBuscarAlumno.isEnabled = true
                    Toast.makeText(
                        this@AlumnosProfesorActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
