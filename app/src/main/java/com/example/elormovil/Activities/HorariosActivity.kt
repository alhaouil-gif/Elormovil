package com.example.elormovil.Activities

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Ciclo
import com.example.elormovil.model.Horario
import com.example.elormovil.model.Reunion
import com.example.elormovil.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HorariosActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private val dias = listOf("L", "M", "X", "J", "V")

    private lateinit var spinnerModo: Spinner
    private lateinit var spinnerFiltro: Spinner
    private lateinit var spinnerProfesor: Spinner

    private lateinit var radioCurso: RadioGroup
    private lateinit var radioCurso1: RadioButton
    private lateinit var radioCurso2: RadioButton

    private lateinit var labelCurso: TextView
    private lateinit var labelProfesor: TextView

    private var cicloSeleccionado: Int? = null
    private var cursoSeleccionado: Int? = null

    private var horarioOriginal: List<Horario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horarios)

        tableLayout = findViewById(R.id.rvFilas)

        spinnerModo = findViewById(R.id.spinnerModo)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        spinnerProfesor = findViewById(R.id.spinnerProfesor)

        radioCurso = findViewById(R.id.radioCurso)
        radioCurso1 = findViewById(R.id.radioCurso1)
        radioCurso2 = findViewById(R.id.radioCurso2)

        labelCurso = findViewById(R.id.labelCurso)
        labelProfesor = findViewById(R.id.labelProfesor)

        val user = intent.getSerializableExtra("USER") as? User ?: return

        // PROFESOR → NO SPINNERS
        if (user.tipoId == 3) {
            ocultarFiltros()
            cargarHorarioProfesor(user.id)
            return
        }

        // ALUMNO → SPINNERS ACTIVOS
        if (user.tipoId == 4) {
            cargarHorarioAlumno(user.id)
            inicializarSpinners()
        }

        radioCurso.setOnCheckedChangeListener { _, checkedId ->
            cursoSeleccionado = when (checkedId) {
                R.id.radioCurso1 -> 1
                R.id.radioCurso2 -> 2
                else -> null
            }

            if (cicloSeleccionado != null && cursoSeleccionado != null) {
                cargarProfesoresPorCurso()
            }
        }
    }

    // OCULTAR FILTROS (PROFESOR)
    private fun ocultarFiltros() {
        spinnerModo.visibility = View.GONE
        spinnerFiltro.visibility = View.GONE
        spinnerProfesor.visibility = View.GONE
        radioCurso.visibility = View.GONE
        labelCurso.visibility = View.GONE
        labelProfesor.visibility = View.GONE
    }

    // SPINNERS (ALUMNO)
    private fun inicializarSpinners() {

        val modos = listOf("Mis horarios", "Horarios de profesores", "Horarios por ciclo")

        spinnerModo.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, modos)

        spinnerModo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                when (position) {

                    0 -> { // MIS HORARIOS
                        spinnerFiltro.visibility = View.GONE
                        radioCurso.visibility = View.GONE
                        spinnerProfesor.visibility = View.GONE

                        labelCurso.visibility = View.GONE
                        labelProfesor.visibility = View.GONE

                        val user = intent.getSerializableExtra("USER") as User
                        cargarHorarioAlumno(user.id)
                    }

                    1 -> { // TODOS
                        spinnerFiltro.visibility = View.GONE
                        radioCurso.visibility = View.GONE

                        spinnerProfesor.visibility = View.VISIBLE
                        labelProfesor.visibility = View.VISIBLE
                        labelCurso.visibility = View.GONE

                        cargarTodosProfesores()
                    }

                    2 -> { // POR CICLO
                        spinnerProfesor.visibility = View.GONE
                        labelProfesor.visibility = View.GONE

                        spinnerFiltro.visibility = View.VISIBLE
                        cargarCiclos()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun cargarCiclos() {
        RetrofitClient.apiService.getCiclos()
            .enqueue(object : Callback<List<Ciclo>> {
                override fun onResponse(call: Call<List<Ciclo>>, response: Response<List<Ciclo>>) {

                    val ciclos = response.body() ?: return

                    spinnerFiltro.adapter = ArrayAdapter(
                        this@HorariosActivity,
                        android.R.layout.simple_spinner_item,
                        ciclos.map { it.nombre }
                    )

                    spinnerFiltro.visibility = View.VISIBLE

                    spinnerFiltro.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View?, position: Int, id: Long
                            ) {
                                cicloSeleccionado = ciclos[position].id
                                radioCurso.visibility = View.VISIBLE
                                labelCurso.visibility = View.VISIBLE
                                radioCurso.clearCheck()
                                cursoSeleccionado = null
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                }

                override fun onFailure(call: Call<List<Ciclo>>, t: Throwable) {}
            })
    }

    private fun cargarTodosProfesores() {
        RetrofitClient.apiService.getProfesores()
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    val lista = response.body()
                    if (lista.isNullOrEmpty()) {
                        Toast.makeText(this@HorariosActivity, "No hay profesores", Toast.LENGTH_SHORT).show()
                        return
                    }
                    mostrarProfesores(lista)
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(this@HorariosActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarProfesoresPorCurso() {
        RetrofitClient.apiService.getProfesoresPorCurso(cicloSeleccionado!!, cursoSeleccionado!!)
            .enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    mostrarProfesores(response.body() ?: emptyList())
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {}
            })
    }

    private fun mostrarProfesores(lista: List<User>) {
        spinnerProfesor.visibility = View.VISIBLE
        labelProfesor.visibility = View.VISIBLE

        spinnerProfesor.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item,
                lista.map { "${it.nombre} ${it.apellidos}" })

        spinnerProfesor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                cargarHorarioProfesor(lista[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // HORARIOS
    private fun cargarHorarioProfesor(id: Int) {
        RetrofitClient.apiService.getHorarioProfesor(id)
            .enqueue(object : Callback<List<Horario>> {
                override fun onResponse(call: Call<List<Horario>>, response: Response<List<Horario>>) {
                    mostrarTabla(response.body() ?: emptyList())
                }

                override fun onFailure(call: Call<List<Horario>>, t: Throwable) {}
            })
    }

    private fun cargarHorarioAlumno(id: Int) {
        RetrofitClient.apiService.getHorarioAlumno(id)
            .enqueue(object : Callback<List<Horario>> {
                override fun onResponse(call: Call<List<Horario>>, response: Response<List<Horario>>) {
                    horarioOriginal = response.body() ?: emptyList()
                    mostrarTabla(horarioOriginal)
                }

                override fun onFailure(call: Call<List<Horario>>, t: Throwable) {}
            })
    }

    // FUNCION MOSTRAR TABLA CORRECTA
    private fun mostrarTabla(listaHorarios: List<Horario>) {

        tableLayout.removeAllViews()

        // 6 filas (hora 1..6) x 5 columnas (dias)
        val tabla = Array(6) { Array(5) { mutableListOf<Horario>() } } // indices 0..5 para horas 1..6

        // ASIGNAR HORARIOS CORRECTAMENTE
        for (h in listaHorarios) {
            val fila = h.hora   // hora real 1..6 → índice 0..5
            val col = dias.indexOf(h.dia)
            if (fila in 0..5 && col in 0..4) tabla[fila][col].add(h)
        }

        // CABECERA
        val cabecera = TableRow(this)
        cabecera.addView(crearCelda("", true)) // celda vacía primera columna
        dias.forEach { cabecera.addView(crearCelda(it, true)) }
        tableLayout.addView(cabecera)

        // FILAS HORAS
        for (filaIdx in tabla.indices) { // filaIdx = 0..5
            val row = TableRow(this)

            // columna hora
            row.addView(crearCelda((filaIdx + 1).toString(), true))

            for (col in 0..4) {
                val items = tabla[filaIdx][col]
                val texto = if (items.isEmpty()) "-" else items.joinToString("\n") { it.texto }

                val tv = crearCelda(texto)

                // color según conflicto / estado
                val hayModulo = items.any { it.tipo != "REUNION" }
                val hayReunion = items.any { it.tipo == "REUNION" }
                val color = when {
                    hayModulo && hayReunion -> 0xFFCCCCCC.toInt() // conflicto
                    items.any { it.estado == "aceptada" } -> 0xFF99FF99.toInt()
                    items.any { it.estado == "pendiente" } -> 0xFFFFFF99.toInt()
                    items.any { it.estado == "denegada" } -> 0xFFFF0000.toInt() // denegada → rojo fuerte
                    else -> 0xFFFFFFFF.toInt() // vacía
                }

                tv.setBackgroundColor(color)
                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun crearCelda(texto: String, esCabecera: Boolean = false): TextView {
        val tv = layoutInflater.inflate(R.layout.celda_item, null) as TextView
        tv.text = texto
        tv.gravity = Gravity.CENTER
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.maxLines = if (esCabecera) 1 else 2
        tv.minHeight = 120
        return tv
    }
    private fun denegarReunion(id: Int) {
        RetrofitClient.apiService.cambiarEstadoReunion(id, "denegada")
            .enqueue(object : Callback<Reunion> {
                override fun onResponse(call: Call<Reunion>, response: Response<Reunion>) {
                 }
                override fun onFailure(call: Call<Reunion>, t: Throwable) {}
            })

    }

}
