package com.example.elormovil.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

data class User(
    val id: Int,
    val apellidos: String,
    @SerializedName("argazkia_url")
    val fotoUrl: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    val direccion: String?,
    val dni: String?,
    val email: String?,
    val nombre: String,
    val password: String?,
    val telefono1: String?,
    val telefono2: String?,
    @SerializedName("tipo_id")
    val tipoId: Int?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val username: String
) : Serializable
