package com.example.elormovil.instances

import com.example.elormovil.model.Horario
import com.example.elormovil.model.LoginRequest
import com.example.elormovil.model.User
import okhttp3.MultipartBody
//import com.example.elormovil.model.Horario
//import com.example.elormovil.model.Reunion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
interface ApiService {

    @GET("auth/public-key")
    fun getPublicKey(): Call<Map<String, String>>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<User>

    @POST("auth/forgot-password")
    fun recoverPassword(@Body body: Map<String, String>): Call<Map<String, String>>

    // Obtener datos de usuario por ID
    @GET("users/{id}")
    fun getUserById(@Path("id") userId: Int): Call<User>

    @POST("user/upload-photo")
    fun uploadProfilePhoto(@Body body: Map<String, String>): Call<Map<String, String>>

    @Multipart
    @POST("users/{id}/upload-photo")
    fun uploadPhoto(
        @Path("id") userId: Int,
        @Part photo: MultipartBody.Part
    ): Call<User>

    @GET("horarios/profesor/{id}")
    fun getHorarioProfesor(@Path("id") id: Int): Call<List<Horario>>

    @GET("horarios/alumno/{id}")
    fun getHorarioAlumno(@Path("id") id: Int): Call<List<Horario>>




}
