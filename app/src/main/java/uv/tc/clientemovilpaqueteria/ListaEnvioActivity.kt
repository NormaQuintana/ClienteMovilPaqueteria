package uv.tc.clientemovilpaqueteria

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.adaptador.EnvioAdaptador
import uv.tc.clientemovilpaqueteria.databinding.ActivityListaEnvioBinding
import uv.tc.clientemovilpaqueteria.poko.Envio
import uv.tc.clientemovilpaqueteria.util.Constantes

class ListaEnvioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListaEnvioBinding
    private var idColaborador: Int = 0
    private var listaOriginal: List<Envio> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEnvioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idColaborador = intent.getIntExtra("idColaborador", 0)
        binding.rvEnvios.layoutManager = LinearLayoutManager(this)

        configurarBuscador()

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        if (idColaborador > 0) {
            obtenerEnviosServicio()
        } else {
            Toast.makeText(this, "Error: No se identificó al conductor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarEnvios(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarEnvios(texto: String) {
        if (texto.isEmpty()) {
            binding.rvEnvios.adapter = EnvioAdaptador(listaOriginal)
        } else {
            val listaFiltrada = listaOriginal.filter { envio ->
                envio.noGuia?.contains(texto, ignoreCase = true) == true
            }
            binding.rvEnvios.adapter = EnvioAdaptador(listaFiltrada)
        }
    }

    private fun obtenerEnviosServicio() {
        val url = "${Constantes().URL_API}envio/obtener-todos/$idColaborador"


        Ion.with(this)
            .load("GET", url)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val respuestaJson = String(result, Charsets.UTF_8)
                    serializarInformacion(respuestaJson)
                } else {
                    Log.e("API_ERROR", "Error: ${e?.message}")
                    Toast.makeText(this, "Error de red: No se pudo conectar al servidor", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun serializarInformacion(json: String) {
        val gson = Gson()
        try {
            val tipoListaEnvio = object : TypeToken<List<Envio>>() {}.type
            listaOriginal = gson.fromJson(json, tipoListaEnvio)

            if (listaOriginal.isNotEmpty()) {
                val adaptador = EnvioAdaptador(listaOriginal)
                binding.rvEnvios.adapter = adaptador
            } else {
                Toast.makeText(this, "No tienes envíos asignados actualmente", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SERIALIZACION", "Error al convertir JSON: ${e.message}")
            Toast.makeText(this, "Error al procesar la lista de envíos", Toast.LENGTH_SHORT).show()
        }
    }
}