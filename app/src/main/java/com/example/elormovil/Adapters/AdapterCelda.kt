package com.example.elormovil.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elormovil.R

class AdapterCelda(
    private val celdas: Array<String>
) : RecyclerView.Adapter<AdapterCelda.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCelda: TextView = view.findViewById(R.id.tvCelda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.celda_item, parent, false)
        // AQUÍ usa el nombre REAL del archivo XML
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val texto = celdas[position]
        holder.tvCelda.text = if (texto.isBlank()) "-" else texto
    }

    override fun getItemCount(): Int = celdas.size
}
