package com.example.elormovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elormovil.R
import com.example.elormovil.model.User

class AlumnosAdapter(
    private val alumnos: List<User>,
    private val baseUrl: String = "http://10.0.2.2:8080"
) : RecyclerView.Adapter<AlumnosAdapter.AlumnoViewHolder>() {

    inner class AlumnoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFoto: ImageView = itemView.findViewById(R.id.ic_person)
        val tvNombre: TextView = itemView.findViewById(R.id.tvAlumnoNombre)
        val tvEmail: TextView = itemView.findViewById(R.id.tvAlumnoEmail)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvAlumnoTelefono)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno, parent, false)
        return AlumnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = alumnos[position]

        holder.tvNombre.text = "${alumno.nombre} ${alumno.apellidos}"
        holder.tvEmail.text = alumno.email ?: "Sin email"
        holder.tvTelefono.text = "Teléfono: ${alumno.telefono1 ?: "No disponible"}"

        // Cargar foto del alumno
        if (!alumno.fotoUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(baseUrl + alumno.fotoUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.imgFoto)
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getItemCount(): Int = alumnos.size
}

