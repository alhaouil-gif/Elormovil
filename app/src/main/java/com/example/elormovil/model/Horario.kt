package com.example.elormovil.model

import java.io.Serializable

data class Horario(
    val dia: String,
    val hora: Int,
    val texto: String,
    val tipo: String,     // MODULO o REUNION
    val estado: String?  // solo REUNION
) : Serializable
