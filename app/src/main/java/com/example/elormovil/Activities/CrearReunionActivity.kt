package com.example.elormovil.Activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Reunion
import com.example.elormovil.model.User
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

class CrearReunionActivity : AppCompatActivity() {

    private lateinit var user: User

    private lateinit var etTitulo: EditText
    private lateinit var etAsunto: EditText
    private lateinit var etAula: EditText
    private lateinit var spCentro: Spinner
    private lateinit var spPersona: Spinner
    private lateinit var btnFecha: Button
    private lateinit var btnCrear: Button

    private var fechaSeleccionada: String = ""
    private var diaLectivo: String = ""
    private var horaLectiva: String = ""

    private val centrosIds = mutableListOf<String>()
    private val centrosNombres = mutableListOf<String>()
    private val personas = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_reunion)

        user = intent.getSerializableExtra("USER") as User

        etTitulo = findViewById(R.id.etTitulo)
        etAsunto = findViewById(R.id.etAsunto)
        etAula = findViewById(R.id.etAula)
        spCentro = findViewById(R.id.spCentro)
        spPersona = findViewById(R.id.spPersona)
        btnFecha = findViewById(R.id.btnFecha)
        btnCrear = findViewById(R.id.btnCrear)

        cargarCentros()
        cargarPersonas()

        btnFecha.setOnClickListener { seleccionarFecha() }
        btnCrear.setOnClickListener { crearReunionYEnviarCorreo() }
    }

    private fun cargarCentros() {
        val json = assets.open("centros.json")
        val br = BufferedReader(InputStreamReader(json))
        val text = br.readText()
        val root = JSONObject(text)
        val array = root.getJSONArray("CENTROS")

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            centrosIds.add(o.getString("CCEN"))
            centrosNombres.add(o.getString("NOM"))
        }

        spCentro.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            centrosNombres
        )
    }

    private fun cargarPersonas() {
        val call = if (user.tipoId == 4)
            RetrofitClient.apiService.getProfesores()
        else
            RetrofitClient.apiService.getAlumnosProfesor(user.id)

        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(c: Call<List<User>>, r: Response<List<User>>) {
                if (!r.isSuccessful || r.body().isNullOrEmpty()) {
                    Toast.makeText(this@CrearReunionActivity,"No hay personas",Toast.LENGTH_LONG).show()
                    return
                }

                personas.clear()
                personas.addAll(r.body()!!)

                val nombres = personas.map { "${it.nombre} ${it.apellidos}" }
                spPersona.adapter = ArrayAdapter(
                    this@CrearReunionActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    nombres
                )
            }

            override fun onFailure(c: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@CrearReunionActivity,t.message,Toast.LENGTH_LONG).show()
            }
        })
    }

    // ================= SELECCIONAR DÍA/HORA CONTROLADA =================
    private fun seleccionarFecha() {
        val dias = arrayOf("Lunes","Martes","Miércoles","Jueves","Viernes")
        val horas = arrayOf("1","2","3","4","5","6")

        var diaSeleccionado = 0
        var horaSeleccionada = 0

        AlertDialog.Builder(this)
            .setTitle("Seleccionar día")
            .setSingleChoiceItems(dias, -1) { _, which -> diaSeleccionado = which }
            .setPositiveButton("Siguiente") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Seleccionar hora")
                    .setSingleChoiceItems(horas, -1) { _, which -> horaSeleccionada = which }
                    .setPositiveButton("Aceptar") { _, _ ->
                        // Semana: primera de enero 2025, Lunes = 6 de enero
                        val cal = Calendar.getInstance()
                        cal.set(2025, Calendar.JANUARY, 6)
                        cal.add(Calendar.DAY_OF_MONTH, diaSeleccionado)

                        diaLectivo = dias[diaSeleccionado]

                        // Map hora 1-6 a 8-13
                        val horaReal = when (horaSeleccionada + 1) {
                            1 -> 8
                            2 -> 9
                            3 -> 10
                            4 -> 11
                            5 -> 12
                            6 -> 13
                            else -> 8
                        }

                        horaLectiva = (horaSeleccionada + 1).toString()

                        fechaSeleccionada = String.format(
                            "%04d-%02d-%02dT%02d:00:00",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH),
                            horaReal
                        )

                        btnFecha.text = fechaSeleccionada
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ================= CREAR REUNIÓN Y ENVIAR CORREO =================
    private fun crearReunionYEnviarCorreo() {

        if (spCentro.selectedItemPosition == -1 || centrosIds.isEmpty()) {
            Toast.makeText(this, "Selecciona un centro", Toast.LENGTH_SHORT).show()
            return
        }
        if (spPersona.selectedItemPosition == -1 || personas.isEmpty()) {
            Toast.makeText(this, "Selecciona persona", Toast.LENGTH_SHORT).show()
            return
        }
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona fecha", Toast.LENGTH_SHORT).show()
            return
        }

        val persona = personas[spPersona.selectedItemPosition]
        val centroNombre = centrosNombres[spCentro.selectedItemPosition]

        val reunion = Reunion(
            titulo = etTitulo.text.toString(),
            asunto = etAsunto.text.toString(),
            aula = etAula.text.toString(),
            fecha = fechaSeleccionada,
            estado = "pendiente",
            id_centro = centrosIds[spCentro.selectedItemPosition],
            profesor = if (user.tipoId == 4) persona else user,
            alumno = if (user.tipoId == 4) user else persona
        )

        // 1️⃣ Crear la reunión en el servidor
        RetrofitClient.apiService.crearReunion(reunion)
            .enqueue(object : Callback<Reunion> {
                override fun onResponse(c: Call<Reunion>, r: Response<Reunion>) {
                    if (!r.isSuccessful || r.body() == null) {
                        Toast.makeText(this@CrearReunionActivity, "Error creando reunión", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val reunionCreada = r.body()!!

                     val alumno = reunionCreada.alumno!!
                    val profesor = reunionCreada.profesor!!

                    val asuntoCorreo = "Nueva reunión programada - ElorES"

                    val cuerpoAlumno = """
                        Título: ${reunionCreada.titulo}
                        Fecha: $diaLectivo
                        Hora: $horaLectiva
                        Aula: ${reunionCreada.aula}
                        Profesor: ${profesor.nombre} ${profesor.apellidos}
                        Centro: $centroNombre
                    """.trimIndent()

                    val cuerpoProfesor = """
                        Título: ${reunionCreada.titulo}
                        Fecha: $diaLectivo
                        Hora: $horaLectiva
                        Aula: ${reunionCreada.aula}
                        Alumno: ${alumno.nombre} ${alumno.apellidos}
                        Centro: $centroNombre
        2            """.trimIndent()

                 val payload = mapOf(
                        "emailAlumno" to (alumno.email ?: ""),
                        "emailProfesor" to (profesor.email ?: ""),
                        "cuerpoAlumno" to cuerpoAlumno,
                        "cuerpoProfesor" to cuerpoProfesor,
                        "asunto" to asuntoCorreo
                    )


                     RetrofitClient.apiService.enviarCorreo(payload).enqueue(object: Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Toast.makeText(this@CrearReunionActivity,"Reunión creada y correos enviados",Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@CrearReunionActivity,"Reunión creada pero error enviando correos",Toast.LENGTH_LONG).show()
                            finish()
                        }
                    })
                }

                override fun onFailure(c: Call<Reunion>, t: Throwable) {
                    Toast.makeText(this@CrearReunionActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }
}
