package com.example.elormovil.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdapterFila(
    private val context: Context,
    private val filas: Array<Array<String>>
) : RecyclerView.Adapter<AdapterFila.ViewHolder>() {

    class ViewHolder(val rvFila: RecyclerView) : RecyclerView.ViewHolder(rvFila)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rv = RecyclerView(context)
        rv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        return ViewHolder(rv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rvFila.adapter = AdapterCelda(filas[position])
    }

    override fun getItemCount(): Int = filas.size
}
