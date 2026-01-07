package uv.tc.clientemovilpaqueteria

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.databinding.ActivityEdicionConductorBinding
import uv.tc.clientemovilpaqueteria.dto.Respuesta
import uv.tc.clientemovilpaqueteria.poko.Colaborador
import uv.tc.clientemovilpaqueteria.util.Constantes

class EdicionConductorActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityEdicionConductorBinding
    private lateinit var conductor : Colaborador
    private val CURP_REGEX = "[A-Z]{4}[0-9]{6}[H|M][A-Z]{2}[B|C|D|F|G|H|J|K|L|M|N|Ñ|P|Q|R|S|T|V|W|X|Y|Z]{3}[0-9A-Z]{1}[0-9]{1}"
    private val SOLO_LETRAS_REGEX = "^[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ\\s]+\$"
    private val LICENCIA_REGEX = "^[A-Z0-9]{8,10}$"
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityEdicionConductorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        cargarDatosConductor()
        binding.btnAceptar.setOnClickListener {
            guardarCambios()
        }
        binding.btnVolver.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        mostrarDialogoConfirmacion()
    }

    private fun guardarCambios() {
        if(sonCamposValidos()){
            conductor.nombre = binding.etNombre.text.toString().trim()
            conductor.apellidoPaterno = binding.etApPaterno.text.toString().trim()
            conductor.apellidoMaterno = binding.etApMaterno.text.toString().trim()
            conductor.curp = binding.etCurp.text.toString().uppercase().trim()
            conductor.correo = binding.etCorreo.text.toString().trim()
            conductor.noLicencia = binding.etLicencia.text.toString().trim()

            enviarCambiosAPI()
        }

    }

    fun sonCamposValidos() : Boolean{
        var valido = true
        binding.etNombre.error = null
        binding.etApPaterno.error = null
        binding.etCurp.error = null
        binding.etCorreo.error = null
        binding.etLicencia.error = null

        if (binding.etNombre.text.isEmpty()){
            binding.etNombre.setError("Nombre obligatorio")
            valido = false
        }else if (!binding.etNombre.text.toString().matches(Regex(SOLO_LETRAS_REGEX))) {
            binding.etNombre.setError("Solo se permiten letras en el nombre")
            valido = false
        }
        if(binding.etApPaterno.text.isEmpty()){
            binding.etApPaterno.setError("Apellido paterno obligatorio")
            valido  = false
        }else if (!binding.etApPaterno.text.toString().matches(Regex(SOLO_LETRAS_REGEX))) {
            binding.etApPaterno.setError("Solo se permiten letras en el apellido paterno")
            valido = false
        }
        if(binding.etApMaterno.text.isNotEmpty() && !binding.etApMaterno.text.toString().matches(Regex(SOLO_LETRAS_REGEX))){
            binding.etApMaterno.setError("Solo se permiten letras en el apellido materno")
            valido = false
        }
        val curpTexto = binding.etCurp.text.toString().uppercase()
        if(curpTexto.isEmpty()){
            binding.etCurp.setError("CURP obligatorio")
            valido  = false
        } else if (!curpTexto.matches(Regex(CURP_REGEX))) {
            binding.etCurp.setError("Formato de CURP inválido (18 caracteres)")
            valido = false
        }
        val correoTexto = binding.etCorreo.text.toString()
        if(correoTexto.isEmpty()){
            binding.etCorreo.setError("Correo electronico obligatorio")
            valido  = false
        }else if (!correoTexto.matches(Regex(EMAIL_REGEX))) {
            binding.etCorreo.error = "Formato de correo electrónico no válido"
            valido = false
        }
        val licenciaTexto = binding.etLicencia.text.toString().uppercase().trim()
        if(licenciaTexto.isEmpty()){
            binding.etLicencia.setError("Numero de Licencia obligatorio")
            valido  = false
        } else if (!licenciaTexto.matches(Regex(LICENCIA_REGEX))) {
            binding.etLicencia.error = "Formato de licencia inválido (8-12 caracteres alfanuméricos)"
            valido = false
        }

        return valido
    }

    private fun enviarCambiosAPI() {
        val datosFiltrados = mapOf(
            "idColaborador" to conductor.idColaborador,
            "nombre" to conductor.nombre,
            "apellidoPaterno" to conductor.apellidoPaterno,
            "apellidoMaterno" to conductor.apellidoMaterno,
            "curp" to conductor.curp,
            "correo" to conductor.correo,
            "noLicencia" to conductor.noLicencia,
            "idSucursal" to conductor.idSucursal
        )
        val gson = Gson()
        val jsonConductor = gson.toJson(datosFiltrados)

        val urlActualizacion = "${Constantes().URL_API}colaborador/editar"
        Toast.makeText(this, "Guardando cambios...", Toast.LENGTH_SHORT).show()
        Ion.with(this)
            .load("PUT", urlActualizacion)
            .setHeader("Content-Type", "application/json")
            .setStringBody(jsonConductor)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null) {
                    val respuestaJson = String(result, Charsets.UTF_8)
                    manejarRespuestaAPI(respuestaJson)
                } else {
                    Toast.makeText(this,
                        "Error de red al actualizar: No se pudo conectar con el servidor.",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun manejarRespuestaAPI(jsonRespuesta: String?) {
        try {
            val gson = Gson()
            val respuestaServidor = gson.fromJson(jsonRespuesta, Respuesta::class.java)
            if (!respuestaServidor.error) {
                Toast.makeText(this, "¡Datos actualizados correctamente!",
                    Toast.LENGTH_LONG).show()
                val resultadoIntent = Intent()
                val conductorActualizadoJson = gson.toJson(conductor)
                resultadoIntent.putExtra("conductor_actualizado", conductorActualizadoJson)
                setResult(RESULT_OK, resultadoIntent)
                finish()
            }else {
                Toast.makeText(this,
                    "Error al guardar: ${respuestaServidor.mensaje}",
                    Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)

            }
        }catch (e : Exception){
            Toast.makeText(this, "Error al procesar la respuesta del servidor.",
                Toast.LENGTH_LONG).show()
        }

    }

    fun cargarDatosConductor(){
        val jsonDatos = intent.getStringExtra("datos_colaborador_json")
        if(jsonDatos != null){
            try{
                val gson = Gson()
                conductor = gson.fromJson(jsonDatos, Colaborador::class.java)
                binding.etNombre.setText(conductor.nombre)
                binding.etApPaterno.setText(conductor.apellidoPaterno)
                binding.etApMaterno.setText(conductor.apellidoMaterno)
                binding.etCurp.setText(conductor.curp)
                binding.etCorreo.setText(conductor.correo)
                binding.etLicencia.setText(conductor.noLicencia)

            }catch (e : Exception){
                Toast.makeText(this,
                    "Fallo al cargar la información para edición.",
                    Toast.LENGTH_LONG).show()
                e.printStackTrace()
                finish()

            }
        }else{
            Toast.makeText(this,
                "Error: No se recibieron datos del conductor.",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de salir de la pantalla de edición?")

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setPositiveButton("Salir") { _, _ ->
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }
}