package com.example.elormovil.providers

import android.content.Context
import com.example.elormovil.model.Centro
import org.json.JSONObject

object CentrosProvider {

    fun cargar(context:Context):List<Centro>{

        val json = context.assets.open("centros.json")
            .bufferedReader().use { it.readText() }

        val root = JSONObject(json)
        val arr = root.getJSONArray("CENTROS")

        val lista = mutableListOf<Centro>()

        for(i in 0 until arr.length()){
            val o = arr.getJSONObject(i)
            lista.add(
                Centro(
                    o.getString("CCEN"),
                    o.getString("NOM"),
                    o.getDouble("LATITUD"),
                    o.getDouble("LONGITUD")
                )
            )
        }

        return lista
    }
}
