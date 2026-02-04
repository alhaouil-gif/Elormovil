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
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.elormovil.R
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
    private lateinit var btnCiclos: Button

    private lateinit var btnCambiarTema: Button
    private lateinit var btnEs: Button
    private lateinit var btnEu: Button
    private lateinit var btnEn: Button

    private lateinit var user: User

    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {

        aplicarTemaGuardado()

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
        btnCiclos = findViewById(R.id.btnCiclos)

        btnCambiarTema = findViewById(R.id.btnCambiarTema)
        btnEs = findViewById(R.id.btnEs)
        btnEu = findViewById(R.id.btnEu)
        btnEn = findViewById(R.id.btnEn)

        user = intent.getSerializableExtra("USER") as? User ?: run {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarInformacion(user)
        configurarBotonesSegunTipo(user.tipoId)

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

        btnCambiarTema.setOnClickListener {
            val modoActual = AppCompatDelegate.getDefaultNightMode()
            val nuevoModo =
                if (modoActual == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES

            AppCompatDelegate.setDefaultNightMode(nuevoModo)
            guardarTema(nuevoModo)
        }

        btnEs.setOnClickListener { cambiarIdioma("es") }
        btnEu.setOnClickListener { cambiarIdioma("eu") }
        btnEn.setOnClickListener { cambiarIdioma("en") }

        btnCiclos.setOnClickListener {
            val i = Intent(this, AlumnosProfesorActivity::class.java)
            i.putExtra("USER", user)
            startActivity(i)
        }
    }

    private fun guardarTema(modo: Int) {
        val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
        prefs.edit().putInt("tema", modo).apply()
    }

    private fun aplicarTemaGuardado() {
        val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
        val tema = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(tema)
    }

    private fun cambiarIdioma(tag: String) {

        val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
        prefs.edit().putString("idioma", tag).apply()

        imgFoto.setImageDrawable(null)
        imgFoto.setImageBitmap(null)

        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)

        val i = intent
        finish()
        overridePendingTransition(0, 0)
        startActivity(i)
        overridePendingTransition(0, 0)
    }

    private fun mostrarInformacion(user: User) {
        tvNombre.text = "${getString(R.string.nombre)} ${user.nombre}"
        tvApellidos.text = "${getString(R.string.apellidos)} ${user.apellidos}"
        tvEmail.text = "${getString(R.string.email)} ${user.email ?: getString(R.string.no_disponible)}"
        tvDireccion.text = "${getString(R.string.direccion)} ${user.direccion ?: getString(R.string.no_disponible)}"
        tvTelefono1.text = "${getString(R.string.telefono)} ${user.telefono1 ?: getString(R.string.no_disponible)}"

        tvTipo.text = when (user.tipoId) {
            4 -> "${getString(R.string.tipo)} Alumno"
            3 -> "${getString(R.string.tipo)} Profesor"
            else -> "${getString(R.string.tipo)} Otro"
        }
    }

    private fun configurarBotonesSegunTipo(tipoId: Int?) {
        btnHorarios.visibility = View.VISIBLE
        btnReuniones.visibility = View.VISIBLE
        btnCiclos.visibility = View.GONE

        if (tipoId != 3 && tipoId != 4) {
            btnHorarios.visibility = View.GONE
            btnReuniones.visibility = View.GONE
         }
        if (tipoId == 3) {
            btnCiclos.visibility = View.VISIBLE
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
