package com.example.elormovil.model

import java.io.Serializable

data class Reunion(
    val id_reunion:Int?=null,
    val estado:String?=null,
    val profesor:User?=null,
    val alumno:User?=null,
    val id_centro:String?=null,
    val titulo:String?=null,
    val asunto:String?=null,
    val aula:String?=null,
    val fecha:String?=null//2025-01-09T12:00:00
):Serializable
