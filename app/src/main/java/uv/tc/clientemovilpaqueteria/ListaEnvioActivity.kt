package uv.tc.clientemovilpaqueteria

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEnvioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idColaborador = intent.getIntExtra("idColaborador", 0)
        binding.rvEnvios.layoutManager = LinearLayoutManager(this)

        if (idColaborador > 0) {
            obtenerEnviosServicio()
        } else {
            Toast.makeText(this, "Error: No se identificó al conductor", Toast.LENGTH_SHORT).show()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun obtenerEnviosServicio() {
        val url = "${Constantes().URL_API}envio/obtener-todos/$idColaborador"

        Log.d("API_LISTA", "Consultando envíos en: $url")

        Ion.with(this)
            .load("GET", url)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    serializarInformacion(result)
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
            val listaEnvios: List<Envio> = gson.fromJson(json, tipoListaEnvio)

            if (listaEnvios.isNotEmpty()) {
                val adaptador = EnvioAdaptador(listaEnvios)
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