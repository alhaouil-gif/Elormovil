package com.example.elormovil.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.elormovil.R
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.Centro
import com.example.elormovil.model.Reunion
import com.example.elormovil.model.User
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar

class ReunionAdapter(
    private val context: Context,
    private val lista: List<Reunion>,
    private val user: User,
    private val refrescar: () -> Unit
) : RecyclerView.Adapter<ReunionAdapter.VH>() {

    private val centrosMap = mutableMapOf<Int, Centro>()

    init {
        // Configuración inicial de OSMDroid
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        cargarCentrosDesdeJson()
    }

    private fun cargarCentrosDesdeJson() {
        val json = context.assets.open("centros.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val array = root.getJSONArray("CENTROS")
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val id = o.getInt("CCEN")
            val nombre = o.getString("NOM")
            val lat = o.getDouble("LATITUD")
            val lng = o.getDouble("LONGITUD")
            centrosMap[id] = Centro(CCEN = id.toString(), NOM = nombre, LATITUD = lat, LONGITUD = lng)
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val detalle: TextView = v.findViewById(R.id.tvDetalleReunion)
        val estado: TextView = v.findViewById(R.id.tvEstado)
        val iconoUbicacion: ImageView = v.findViewById(R.id.iconoUbicacion)
        val aceptar: Button = v.findViewById(R.id.btnAceptar)
        val cancelar: Button = v.findViewById(R.id.btnCancelar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reunion, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(h: VH, i: Int) {




        val r = lista[i]

        // ================= CONVERSIÓN DE FECHA =================
        val fecha = r.fecha
        var diaTexto = ""
        var horaTexto = ""
        if (!fecha.isNullOrEmpty()) {
            try {
                val partes = fecha.split("T")
                val fechaPartes = partes[0].split("-").map { it.toInt() }
                val horaPartes = partes[1].split(":").map { it.toInt() }

                val cal = Calendar.getInstance()
                cal.set(fechaPartes[0], fechaPartes[1] - 1, fechaPartes[2])

                diaTexto = when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Lunes"
                    Calendar.TUESDAY -> "Martes"
                    Calendar.WEDNESDAY -> "Miércoles"
                    Calendar.THURSDAY -> "Jueves"
                    Calendar.FRIDAY -> "Viernes"
                    Calendar.SATURDAY -> "Sábado"
                    Calendar.SUNDAY -> "Domingo"
                    else -> ""
                }

                horaTexto = when (horaPartes[0]) {
                    8 -> "1"
                    9 -> "2"
                    10 -> "3"
                    11 -> "4"
                    12 -> "5"
                    13 -> "6"
                    else -> horaPartes[0].toString()
                }

            } catch (e: Exception) {
                diaTexto = ""
                horaTexto = ""
            }
        }

        // ================= USUARIO RELACIONADO =================
        val rolUsuario = when (user.tipoId) {
            3 -> "Alumno"
            4 -> "Profesor"
            else -> "Usuario"
        }

        val usuarioRelacionado = when (user.tipoId) {
            3 -> r.alumno?.nombre ?: "Alumno desconocido"
            4 -> r.profesor?.nombre ?: "Profesor desconocido"
            else -> "Usuario desconocido"
        }

        // ================= CENTRO =================
        val centro = r.id_centro?.toIntOrNull()?.let { centrosMap[it] }
        val nombreCentro = centro?.NOM ?: "Centro desconocido"

        // ================= DETALLE COMPLETO =================
        h.detalle.text = """
            Título: ${r.titulo ?: "Sin título"}
            Asunto: ${r.asunto ?: "Sin asunto"}
            Día: $diaTexto
            Hora: $horaTexto
            Aula: ${r.aula ?: "Sin aula"}
            $rolUsuario: $usuarioRelacionado
            Centro: $nombreCentro
        """.trimIndent()

        // ================= ESTADO =================
        h.estado.text = r.estado ?: ""

        // ================= ICONO UBICACION =================
// ================= ICONO UBICACION =================
        h.iconoUbicacion.setOnClickListener {

            val centro = centrosMap[r.id_centro?.toInt()] ?: return@setOnClickListener

            // 🔹 Depuración: mostrar coordenadas originales del JSON
            Log.d("DEBUG_MAP", "Centro original: ${centro.NOM} -> LAT: ${centro.LATITUD}, LNG: ${centro.LONGITUD}")

            // 🔹 Corregir coordenadas si tu JSON las tiene invertidas
            val lat = centro.LONGITUD  // LONGITUD en JSON es latitud real
            val lon = centro.LATITUD   // LATITUD en JSON es longitud real

            Log.d("DEBUG_MAP", "Centro corregido: ${centro.NOM} -> LAT: $lat, LNG: $lon")

            // 🔹 Configuración OSMDroid ANTES de crear MapView
            val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            val cfg = Configuration.getInstance()
            cfg.userAgentValue = context.packageName       // user-agent válido
            cfg.isDebugMode = true                         // logs de depuración
            cfg.isDebugTileProviders = true                // logs de tiles
            cfg.osmdroidBasePath = File(context.cacheDir, "osmdroid")
            cfg.osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
            cfg.load(context, prefs)

            // 🔹 Inflamos layout del mapa
            val mapLayout = LayoutInflater.from(context).inflate(R.layout.map_dialog_layout, null)
            val mapView = mapLayout.findViewById<MapView>(R.id.mapView)

            // 🔹 Configuración del mapa
            mapView.setTileSource(TileSourceFactory.MAPNIK)  // Mapnik online
            mapView.setMultiTouchControls(true)
            mapView.controller.setZoom(13.0)                 // zoom inicial más amplio
            mapView.controller.animateTo(GeoPoint(lat, lon)) // animación al centro

            // 🔹 Marcador
            val marker = Marker(mapView)
            marker.position = GeoPoint(lat, lon)
            marker.title = centro.NOM
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)

            // 🔹 Mostrar diálogo
            AlertDialog.Builder(context)
                .setTitle(centro.NOM)
                .setView(mapLayout)
                .setPositiveButton("Cerrar", null)
                .show()

            Log.d("OSMDroid", "MapView creado y centrado en: LAT $lat, LNG $lon")
        }

        if (user.tipoId == 3) {
            // Usuario tipo 3 ve los botones
            h.aceptar.visibility = View.VISIBLE
            h.cancelar.visibility = View.VISIBLE

            // Listeners
            h.aceptar.setOnClickListener {
                r.id_reunion?.let { id -> cambiarEstado(id, "aceptada") }
            }

            h.cancelar.setOnClickListener {
                r.id_reunion?.let { id -> borrar(id) }
            }

        } else {
            // Otros usuarios no ven los botones
            h.aceptar.visibility = View.GONE
            h.cancelar.visibility = View.GONE
        }
    }
    private fun cambiarEstado(id: Int, estado: String) {
        RetrofitClient.apiService
            .cambiarEstadoReunion(id, estado)
            .enqueue(object : Callback<Reunion> {
                override fun onResponse(call: Call<Reunion>, response: Response<Reunion>) {
                    refrescar()
                }

                override fun onFailure(call: Call<Reunion>, t: Throwable) {}
            })
    }

    private fun borrar(id: Int) {
        RetrofitClient.apiService
            .borrarReunion(id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    refrescar()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }
    }







