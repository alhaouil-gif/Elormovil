package com.example.elormovil.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.elormovil.R
import com.example.elormovil.Activities.ReunionesActivity
import com.example.elormovil.instances.RetrofitClient
import com.example.elormovil.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private val BASE_URL = "http://10.0.2.2:8080"

    private lateinit var tvNombre: TextView
    private lateinit var tvApellidos: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvTelefono1: TextView
    private lateinit var tvTipo: TextView
    private lateinit var imgFoto: ImageView

    private lateinit var btnHorarios: Button
    private lateinit var btnReuniones: Button
    private lateinit var btnTomarFoto: Button
    private lateinit var btnMisAlumnos: Button

    private lateinit var user: User

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alumno)

        tvNombre = findViewById(R.id.tvNombre)
        tvApellidos = findViewById(R.id.tvApellidos)
        tvEmail = findViewById(R.id.tvEmail)
        tvDireccion = findViewById(R.id.tvDireccion)
        tvTelefono1 = findViewById(R.id.tvTelefono1)
        tvTipo = findViewById(R.id.tvTipo)
        imgFoto = findViewById(R.id.imgFoto)

        btnHorarios = findViewById(R.id.btnHorarios)
        btnReuniones = findViewById(R.id.btnReuniones)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnMisAlumnos = findViewById(R.id.btnMisAlumnos)

        user = intent.getSerializableExtra("USER") as? User ?: run {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarInformacion(user)
        configurarBotonesSegunTipo(user.tipoId)

        // FOTO EXISTENTE (ARREGLADO)
        user.fotoUrl?.let {
            Glide.with(this)
                .load(BASE_URL + it)
                .placeholder(R.drawable.ic_person)
                .into(imgFoto)
        }
        btnHorarios.setOnClickListener {
            val intent = Intent(this, HorariosActivity::class.java)
            intent.putExtra("USER", user)
            startActivity(intent)
        }

        btnTomarFoto.setOnClickListener {
            if (checkCameraPermission()) abrirCamara()
            else requestCameraPermission()
        }

        btnReuniones.setOnClickListener {
            val i = Intent(this, ReunionesActivity::class.java)
            i.putExtra("USER", user)
            startActivity(i)
        }

        btnMisAlumnos.setOnClickListener {
            val i = Intent(this, AlumnosProfesorActivity::class.java)
            i.putExtra("USER", user)
            startActivity(i)
        }

    }

    private fun mostrarInformacion(user: User) {
        tvNombre.text = "Nombre: ${user.nombre}"
        tvApellidos.text = "Apellidos: ${user.apellidos}"
        tvEmail.text = "Email: ${user.email ?: "No disponible"}"
        tvDireccion.text = "Dirección: ${user.direccion ?: "No disponible"}"
        tvTelefono1.text = "Teléfono: ${user.telefono1 ?: "No disponible"}"
        tvTipo.text = when (user.tipoId) {
            4 -> "Tipo: Alumno"
            3 -> "Tipo: Profesor"
            else -> "Tipo: Otro"
        }
    }

    private fun configurarBotonesSegunTipo(tipoId: Int?) {
        btnHorarios.visibility = View.VISIBLE
        btnReuniones.visibility = View.VISIBLE
        btnMisAlumnos.visibility = View.GONE

        // Mostrar botón "Mis Alumnos" solo para profesores
        if (tipoId == 3) {
            btnMisAlumnos.visibility = View.VISIBLE
        }

        if (tipoId != 3 && tipoId != 4) {
            btnHorarios.visibility = View.GONE
            btnReuniones.visibility = View.GONE
        }
    }

    private fun checkCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CAMERA &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun abrirCamara() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap

            if (bitmap != null) {
                imgFoto.setImageBitmap(bitmap)
                subirFotoServidor(bitmap)
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(cacheDir, fileName)
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
        val fos = FileOutputStream(file)
        fos.write(bos.toByteArray())
        fos.close()
        return file
    }

    private fun subirFotoServidor(bitmap: Bitmap) {

        val file = bitmapToFile(bitmap, "perfil_${user.id}.jpg")

        val body = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        )

        RetrofitClient.apiService.uploadPhoto(user.id, body)
            .enqueue(object : Callback<User> {

                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {

                        user = response.body()!!

                        Glide.with(this@ProfileActivity)
                            .load(BASE_URL + user.fotoUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(imgFoto)

                        Toast.makeText(this@ProfileActivity, "Foto subida correctamente", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }
}
