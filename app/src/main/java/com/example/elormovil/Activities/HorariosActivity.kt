package com.example.elormovil.Activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Ciclo
import com.example.elormovil.model.Horario
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
    lateinit var radioCurso: RadioGroup
    lateinit var radioCurso1: RadioButton
    lateinit var radioCurso2: RadioButton

    private var cicloSeleccionado:Int? = null
    private var cursoSeleccionado:Int? = null

    // guardamos horario original del alumno
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

        val user = intent.getSerializableExtra("USER") as? User ?: return

        if (user.tipoId == 3) cargarHorarioProfesor(user.id)
        else cargarHorarioAlumno(user.id)

        if(user.tipoId != 3){
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

    // ================= SPINNERS =================

    private fun inicializarSpinners(){

        val modos = listOf("MIS HORARIOS","TODOS", "POR CICLO")

        spinnerModo.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, modos)

        spinnerModo.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                when(position){

                    0 -> { // MIS HORARIOS

                        spinnerFiltro.visibility = View.GONE
                        radioCurso.visibility = View.GONE
                        spinnerProfesor.visibility = View.GONE

                        val user = intent.getSerializableExtra("USER") as User
                        cargarHorarioAlumno(user.id)
                    }

                    1 -> { // TODOS

                        spinnerFiltro.visibility = View.GONE
                        radioCurso.visibility = View.GONE
                        spinnerProfesor.visibility = View.VISIBLE

                        cargarTodosProfesores()
                    }

                    2 -> { // POR CICLO

                        spinnerProfesor.visibility = View.GONE
                        radioCurso.visibility = View.GONE
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

                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                                cicloSeleccionado = ciclos[position].id

                                radioCurso.visibility = View.VISIBLE
                                radioCurso.clearCheck()
                                cursoSeleccionado = null
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                }

                override fun onFailure(call: Call<List<Ciclo>>, t: Throwable) {}
            })
    }

    private fun cargarTodosProfesores(){

        RetrofitClient.apiService.getProfesores().enqueue(object:Callback<List<User>>{
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {

                val lista = response.body()

                if(lista.isNullOrEmpty()){
                    Toast.makeText(this@HorariosActivity,"No hay profesores",Toast.LENGTH_SHORT).show()
                    return
                }

                mostrarProfesores(lista)
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@HorariosActivity,t.message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarProfesoresPorCurso(){

        RetrofitClient.apiService.getProfesoresPorCurso(cicloSeleccionado!!,cursoSeleccionado!!)
            .enqueue(object:Callback<List<User>>{
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    mostrarProfesores(response.body() ?: emptyList())
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {}
            })
    }

    private fun mostrarProfesores(lista:List<User>){

        spinnerProfesor.visibility = View.VISIBLE

        spinnerProfesor.adapter =
            ArrayAdapter(this,android.R.layout.simple_spinner_item,
                lista.map{"${it.nombre} ${it.apellidos}"})

        spinnerProfesor.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                cargarHorarioProfesor(lista[position].id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ================= HORARIOS =================

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

    private fun mostrarTabla(listaHorarios: List<Horario>) {

        tableLayout.removeAllViews()

        val tabla = Array(7) { Array(6) { mutableListOf<Horario>() } }

        for (h in listaHorarios) {
            val fila = h.hora
            val col = dias.indexOf(h.dia)
            if (fila in 1..6 && col >= 0) tabla[fila][col].add(h)
        }

        val cabecera = TableRow(this)
        cabecera.addView(crearCelda(""))
        dias.forEach { cabecera.addView(crearCelda(it)) }
        tableLayout.addView(cabecera)

        for (hora in 1..6) {

            val row = TableRow(this)
            row.addView(crearCelda(hora.toString()))

            for (col in 0..4) {

                val celda = LinearLayout(this)
                celda.orientation = LinearLayout.VERTICAL
                celda.setPadding(8,8,8,8)

                val items = tabla[hora][col]

                if(items.isEmpty()){
                    celda.addView(crearCelda("-"))
                }else{
                    items.forEach { item ->
                        val tv = crearCelda(item.texto)

                        if(item.tipo == "REUNION"){
                            when(item.estado){
                                "pendiente" -> tv.setBackgroundColor(0xFFFFFF99.toInt())
                                "aceptada" -> tv.setBackgroundColor(0xFF99FF99.toInt())
                                "conflicto" -> tv.setBackgroundColor(0xFFFF9999.toInt())
                                "denegada" -> tv.setBackgroundColor(0xFFCCCCCC.toInt())
                            }
                        }

                        celda.addView(tv)
                    }
                }

                row.addView(celda)
            }

            tableLayout.addView(row)
        }
    }

    private fun crearCelda(texto:String): TextView {
        val tv = layoutInflater.inflate(R.layout.celda_item,null) as TextView
        tv.text = texto
        return tv
    }
}
