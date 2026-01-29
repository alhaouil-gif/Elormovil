package com.example.elormovil.Activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Horario
import com.example.elormovil.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HorariosActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private val dias = listOf("L", "M", "X", "J", "V")
    private val horas = 1..6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horarios)

        tableLayout = findViewById(R.id.rvFilas) // usamos el mismo TableLayout

        val user = intent.getSerializableExtra("USER") as? User
        if (user == null) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (user.tipoId == 3) cargarHorarioProfesor(user.id)
        else cargarHorarioAlumno(user.id)
    }

    private fun cargarHorarioProfesor(id: Int) {
        RetrofitClient.apiService.getHorarioProfesor(id)
            .enqueue(object : Callback<List<Horario>> {
                override fun onResponse(call: Call<List<Horario>>, response: Response<List<Horario>>) {
                    if (response.isSuccessful) {
                        mostrarTabla(response.body() ?: emptyList())
                    } else {
                        Toast.makeText(this@HorariosActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Horario>>, t: Throwable) {
                    Toast.makeText(this@HorariosActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun cargarHorarioAlumno(id: Int) {
        RetrofitClient.apiService.getHorarioAlumno(id)
            .enqueue(object : Callback<List<Horario>> {
                override fun onResponse(call: Call<List<Horario>>, response: Response<List<Horario>>) {
                    if (response.isSuccessful) {
                        mostrarTabla(response.body() ?: emptyList())
                    } else {
                        Toast.makeText(this@HorariosActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Horario>>, t: Throwable) {
                    Toast.makeText(this@HorariosActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun mostrarTabla(listaHorarios: List<Horario>) {

        tableLayout.removeAllViews()

        val dias = listOf("L", "M", "X", "J", "V")

        val tabla = Array(7) { Array(6) { mutableListOf<Horario>() } }

        // metemos cada item en su celda
        for (h in listaHorarios) {
            val fila = h.hora
            val col = dias.indexOf(h.dia)

            if (fila in 1..6 && col >= 0) {
                tabla[fila][col].add(h)
            }
        }

        // CABECERA
        val cabecera = TableRow(this)
        cabecera.addView(crearCelda(""))
        dias.forEach { cabecera.addView(crearCelda(it)) }
        tableLayout.addView(cabecera)

        // FILAS
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
