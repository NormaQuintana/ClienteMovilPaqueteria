package uv.tc.clientemovilpaqueteria

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.databinding.ActivityMainBinding
import uv.tc.clientemovilpaqueteria.dto.RSAutenticacion
import uv.tc.clientemovilpaqueteria.poko.Colaborador
import uv.tc.clientemovilpaqueteria.util.Constantes


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var conductor : Colaborador
    private var fotoPerfilBytes :ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mostrarInformacionColaborador()
        binding.ivMenu.setOnClickListener {
            mostrarMenu()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val nuevoJsonConductor = result.data?.getStringExtra("conductor_actualizado")

            if (nuevoJsonConductor != null) {
                val gson = Gson()
                val conductorActualizado = try {
                    gson.fromJson(nuevoJsonConductor, Colaborador::class.java)
                } catch (e: Exception) {
                    null
                }
                if (conductorActualizado != null) {
                    conductor = conductorActualizado
                    actualizarVistasConductor()
                    desacrgarFoto(conductor.idColaborador)
                    Toast.makeText(this, "Datos de perfil actualizados.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        "Error: Datos del conductor incompletos en la respuesta.",
                        Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this,
                    "Error: No se recibieron datos actualizados de la edición.",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Edición cancelada o fallida.",
                Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrarMenu(){
        val popup = PopupMenu(this, binding.ivMenu)
        popup.menuInflater.inflate(R.menu.menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            manejarOpcionesMenu(menuItem)
        }

        popup.show()
    }

    fun manejarOpcionesMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_editar_perfil -> {
                Toast.makeText(this, "Redirigiendo a Editar Perfil...", Toast.LENGTH_SHORT).show()
                obtenerDatosEdicion()
                true
            }

            R.id.action_cerrar_sesion -> {
                Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> false
        }
    }

    private fun obtenerDatosEdicion() {
        Ion.with(this)
            .load("GET", "${Constantes().URL_API}colaborador/obtener-por-noPerosnal/${conductor.noPersonal}")
            .asString()
            .setCallback { e, result ->
                if (e == null) {
                    enviarDatosAEdicion(result)
                }else {
                    Toast.makeText(this@MainActivity,
                        "Error de red al obtener datos: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        
    }

    private fun enviarDatosAEdicion(json: String?) {
        try {
            val intent = Intent(this, EdicionConductorActivity::class.java)
            intent.putExtra("datos_colaborador_json", json)
            activityResultLauncher.launch(intent)
        }catch (e : Exception){
            Toast.makeText(this@MainActivity,
                "Error al procesar los datos recibidos.",
                Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }


    }

    fun mostrarInformacionColaborador(){
        try {
            val jsonColaborador : String? = intent.getStringExtra("conductor")
            if(jsonColaborador != null){
                val gson = Gson()
                val respuestaLogin : RSAutenticacion
                = gson.fromJson(jsonColaborador, RSAutenticacion::class.java)
                conductor = respuestaLogin.conductor!!
                actualizarVistasConductor()
                desacrgarFoto(conductor.idColaborador)
            }

        }catch (e : Exception){
            Toast.makeText(this@MainActivity,
                "Error al cargar la informacion del alumno",
                Toast.LENGTH_LONG).show()
        }

    }

    fun desacrgarFoto(idColaborador : Int){
        Ion.with(this@MainActivity)
            .load("GET", "${Constantes().URL_API}colaborador/obtener-foto/${conductor.idColaborador}")
            .asString()
            .setCallback { e, result ->
                if(e == null){
                    cargarFotoPerfilAPI(result)
                }else{
                    Toast.makeText(this@MainActivity,
                        e.message,
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    fun cargarFotoPerfilAPI(json : String){
        try {
            if(json.isNotEmpty()){
                val gson = Gson()
                val colaborador : Colaborador = gson.fromJson(json, Colaborador::class.java)
                if(colaborador.fotoBase64 != null){
                    val imgBytes = Base64.decode(colaborador.fotoBase64, Base64.DEFAULT)
                    val imgBitMap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                    binding.ivFotoPerfil.setImageBitmap(imgBitMap)
                }else{
                    Toast.makeText(this@MainActivity,
                        "No cuenta con foto de perfil",
                        Toast.LENGTH_LONG).show()
                }

            }
        }catch (e : Exception){
            Toast.makeText(this@MainActivity,
                e.message,
                Toast.LENGTH_LONG).show()
        }
    }

    fun actualizarVistasConductor() {
        if (::conductor.isInitialized) {
            binding.tvNombre.text = "${conductor.nombre} ${conductor.apellidoPaterno} ${conductor.apellidoMaterno}"
            binding.tvLicenciaConductor.text = conductor.noLicencia
        }
    }
}