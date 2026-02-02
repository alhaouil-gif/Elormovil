package com.example.elormovil.instances

import com.example.elormovil.model.Horario
import com.example.elormovil.model.LoginRequest
import com.example.elormovil.model.Reunion
import com.example.elormovil.model.User
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ================= AUTH =================

    @GET("auth/public-key")
    fun getPublicKey(): Call<Map<String, String>>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<User>

    @POST("auth/forgot-password")
    fun recoverPassword(@Body body: Map<String, String>): Call<Map<String, String>>

    // ================= USERS =================

    @GET("users/{id}")
    fun getUserById(@Path("id") userId: Int): Call<User>

    @Multipart
    @POST("users/{id}/upload-photo")
    fun uploadPhoto(
        @Path("id") userId: Int,
        @Part photo: MultipartBody.Part
    ): Call<User>

    @GET("users/alumnos/{id}")
    fun getAlumnosProfesor(@Path("id") id: Int): Call<List<User>>

    @GET("users/profesores")
    fun getProfesores(): Call<List<User>>

    // ================= HORARIOS =================

    @GET("horarios/profesor/{id}")
    fun getHorarioProfesor(@Path("id") id: Int): Call<List<Horario>>

    @GET("horarios/alumno/{id}")
    fun getHorarioAlumno(@Path("id") id: Int): Call<List<Horario>>

    // ================= REUNIONES =================

    @POST("reuniones/crear")
    fun crearReunion(@Body reunion: Reunion): Call<Reunion>

    @GET("reuniones/profesor/{id}")
    fun getReunionesProfesor(@Path("id") id: Int): Call<List<Reunion>>

    @GET("reuniones/alumno/{id}")
    fun getReunionesAlumno(@Path("id") id: Int): Call<List<Reunion>>

    @PUT("reuniones/{id}/estado/{estado}")
    fun cambiarEstadoReunion(
        @Path("id") id: Int,
        @Path("estado") estado: String
    ): Call<Reunion>

    @DELETE("reuniones/{id}")
    fun borrarReunion(@Path("id") id: Int): Call<Void>

    @POST("/reuniones/enviarCorreo")
    fun enviarCorreo(@Body payload: Map<String, String>): Call<Void>
}
