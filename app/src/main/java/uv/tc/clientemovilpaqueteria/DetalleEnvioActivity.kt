package uv.tc.clientemovilpaqueteria

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.databinding.ActivityDetalleEnvioBinding
import uv.tc.clientemovilpaqueteria.poko.DetalleEnvio
import uv.tc.clientemovilpaqueteria.poko.Paquete
import uv.tc.clientemovilpaqueteria.util.Constantes

class DetalleEnvioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleEnvioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEnvioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val noGuia = intent.getStringExtra("noGuia") ?: ""

        if (noGuia.isNotEmpty()) {
            obtenerDetallesServicio(noGuia)
        }else {
            Toast.makeText(this, "No se recibió número de guía", Toast.LENGTH_SHORT).show()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }

    }

    private fun obtenerDetallesServicio(noGuia: String) {
        val url = "${Constantes().URL_API}envio/obtener-por-guia/$noGuia"

        Ion.with(this)
            .load("GET", url)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val detalle = Gson().fromJson(result, DetalleEnvio::class.java)
                        obtenerEstatusReciente(noGuia, detalle)
                    } catch (ex: Exception) {
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error al cargar detalles", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun obtenerEstatusReciente(noGuia: String, detalle: DetalleEnvio) {
        val urlHistorial = "${Constantes().URL_API}historialEnvio/consultar/$noGuia"

        Ion.with(this)
            .load("GET", urlHistorial)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val jsonArray = com.google.gson.JsonParser.parseString(result).asJsonArray
                        if (jsonArray.size() > 0) {
                            val objEstatus = jsonArray.get(0).asJsonObject.getAsJsonObject("estatusEnvio")
                            detalle.status = objEstatus.get("estatusEnvio").asString
                        }
                    } catch (ex: Exception) {
                        detalle.status = "Pendiente"
                    }
                }
                obtenerPaquetes(detalle.idEnvio)
                llenarVista(detalle)
            }
    }

    private fun llenarVista(dEnvio: DetalleEnvio) {
        binding.tvDetalleGuia.text = "Guía: #${dEnvio.noGuia}"
        binding.tvDetalleEstatus.text = dEnvio.status ?: "No disponible"

        binding.tvNombreReceptor.text = "${dEnvio.nombreReceptor} ${dEnvio.apellidoPaternoReceptor} ${dEnvio.apellidoMaternoReceptor}"

        binding.tvDireccionCompleta.text = "${dEnvio.calleDestino} #${dEnvio.numeroDestino}, " +
                "${dEnvio.ciudad}, ${dEnvio.estado}. CP: ${dEnvio.codigoPostal}"

        binding.tvSucursalOrigen.text = dEnvio.nombreSucursal ?: "Sucursal no asignada"
        binding.tvTelefonoCliente.text = "Tel: ${dEnvio.telefono ?: "N/A"}"
        binding.tvCorreoCliente.text = dEnvio.correo ?: "N/A"
    }

    private fun obtenerPaquetes(idEnvio: Int) {
        val urlPaquetes = "${Constantes().URL_API}paquete/obtener-por-envio/$idEnvio"

        Ion.with(this)
            .load("GET", urlPaquetes)
            .asString()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val tipoListaPaquete = object : com.google.gson.reflect.TypeToken<List<Paquete>>() {}.type
                    val listaPaquetes: List<Paquete> = Gson().fromJson(result, tipoListaPaquete)
                    mostrarPaquetesEnVista(listaPaquetes)
                }
            }
    }

    private fun mostrarPaquetesEnVista(lista: List<Paquete>) {
        binding.containerPaquetes.removeAllViews()

        for (paquete in lista) {
            val textView = TextView(this)
            textView.text = "• ${paquete.descripcion}\n  Dims: ${paquete.alto}x${paquete.ancho}x${paquete.profundidad} cm | Peso: ${paquete.peso}kg"
            textView.setPadding(0, 15, 0, 15)
            binding.containerPaquetes.addView(textView)

            val divider = View(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            params.setMargins(0, 5, 0, 5)
            divider.layoutParams = params
            binding.containerPaquetes.addView(divider)
        }
    }

}