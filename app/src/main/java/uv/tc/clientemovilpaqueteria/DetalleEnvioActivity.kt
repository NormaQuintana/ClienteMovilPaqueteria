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
import com.google.gson.reflect.TypeToken
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.databinding.ActivityDetalleEnvioBinding
import uv.tc.clientemovilpaqueteria.dto.Respuesta
import uv.tc.clientemovilpaqueteria.poko.DetalleEnvio
import uv.tc.clientemovilpaqueteria.poko.EstatusEnvio
import uv.tc.clientemovilpaqueteria.poko.Paquete
import uv.tc.clientemovilpaqueteria.util.Constantes

class DetalleEnvioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleEnvioBinding
    private var listaEstatusCatalogo: List<EstatusEnvio> = listOf()
    private var detalleCargado: DetalleEnvio?  = null
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

        binding.btnCambiarEstatus.setOnClickListener {
            if (listaEstatusCatalogo.isEmpty()) {
                obtenerCatalogoEstatus()
            } else {
                mostrarOpcionesEstatus()
            }
        }

    }

    private fun obtenerCatalogoEstatus() {
        val url = "${Constantes().URL_API}catalogo/estatusEnvio"

        Ion.with(this)
            .load("GET", url)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val tipoLista = object : TypeToken<List<EstatusEnvio>>() {}.type
                    val jsonString = String(result, Charsets.UTF_8)
                    listaEstatusCatalogo = Gson().fromJson(jsonString, tipoLista)
                    mostrarOpcionesEstatus()
                } else {
                    Toast.makeText(this, "No se pudo cargar el catálogo", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun mostrarOpcionesEstatus() {
        val estatusFiltrados = listaEstatusCatalogo.filter { it.idEstatusEnvio >= 7 }
        val nombresEstatus = estatusFiltrados.map { it.estatusEnvio }.toTypedArray()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Actualizar Estatus del Envío")
        builder.setItems(nombresEstatus) { _, which ->
            val estatusSeleccionado = estatusFiltrados[which]

            if (estatusSeleccionado.idEstatusEnvio == 8 || estatusSeleccionado.idEstatusEnvio == 10) {
                mostrarDialogoComentario(estatusSeleccionado)
            } else {
                actualizarEstatusEnServidor(estatusSeleccionado, "Cambio de estado operativo")
            }
        }
        builder.show()
    }

    private fun mostrarDialogoComentario(estatus: EstatusEnvio) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Motivo de: ${estatus.estatusEnvio}")

        val input = android.widget.EditText(this)
        input.hint = "Escribe el motivo aquí..."
        input.setPadding(50, 40, 50, 40)

        val container = android.widget.FrameLayout(this)
        container.addView(input)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(40, 0, 40, 0)
        input.layoutParams = params
        builder.setView(container)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val comentario = input.text.toString()
            if (comentario.isNotEmpty()) {
                actualizarEstatusEnServidor(estatus, comentario)
            } else {
                Toast.makeText(this, "El motivo es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun actualizarEstatusEnServidor(estatus: EstatusEnvio, comentario: String) {
        val url = "${Constantes().URL_API}envio/actualizar-estatus"

        val sharedPreferences = getSharedPreferences("SESION_CONDUCTOR", MODE_PRIVATE)
        val idConductor = sharedPreferences.getInt("idColaborador", 0)

        val datosCuerpo = HashMap<String, Any>()
        datosCuerpo["idEnvio"] = detalleCargado?.idEnvio ?: 0
        datosCuerpo["idEstatusEnvio"] = estatus.idEstatusEnvio
        datosCuerpo["idColaborador"] = idConductor
        datosCuerpo["comentario"] = comentario

        val jsonCuerpo = Gson().toJson(datosCuerpo)

        Ion.with(this)
            .load("PUT", url)
            .setHeader("Content-Type", "application/json")
            .setStringBody(jsonCuerpo)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val respuestaJson = String(result, Charsets.UTF_8)
                    val respuestaObj = Gson().fromJson(respuestaJson, Respuesta::class.java)
                    if (respuestaObj!= null && !respuestaObj.error) {
                        Toast.makeText(this, "Estatus actualizado correctamente", Toast.LENGTH_SHORT).show()

                        detalleCargado?.estatus = estatus.estatusEnvio
                        llenarVista(detalleCargado!!)
                        binding.tvDetalleEstatus.text = estatus.estatusEnvio.uppercase()

                        if(estatus.idEstatusEnvio == 9) {
                            binding.tvDetalleEstatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        }
                        if(estatus.idEstatusEnvio == 10) {
                            binding.tvDetalleEstatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                    } else {
                        val msg = respuestaObj?.mensaje ?: "Respuesta inesperada"
                        Toast.makeText(this, "Error: $result", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Error de red: ${e?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun obtenerDetallesServicio(noGuia: String) {
        val url = "${Constantes().URL_API}envio/obtener-por-guia/$noGuia"

        Ion.with(this)
            .load("GET", url)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val jsonString = String(result, Charsets.UTF_8)
                        detalleCargado = Gson().fromJson(jsonString, DetalleEnvio::class.java)
                        detalleCargado?.let {
                            obtenerEstatusReciente(noGuia, it)
                        }
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
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    try {
                        val jsonString = String(result, Charsets.UTF_8)
                        val jsonArray = com.google.gson.JsonParser.parseString(jsonString).asJsonArray
                        if (jsonArray.size() > 0) {
                            val objEstatus = jsonArray.get(0).asJsonObject.getAsJsonObject("estatusEnvio")
                            detalle.estatus = objEstatus.get("estatusEnvio").asString
                        }
                    } catch (ex: Exception) {
                        detalle.estatus = "Pendiente"
                    }
                }
                obtenerPaquetes(detalle.idEnvio)
                llenarVista(detalle)
            }
    }

    private fun llenarVista(dEnvio: DetalleEnvio) {
        binding.tvDetalleGuia.text = "Guía: #${dEnvio.noGuia}"
        binding.tvDetalleEstatus.text = dEnvio.estatus ?: "No disponible"

        binding.tvNombreReceptor.text = "${dEnvio.nombreReceptor} ${dEnvio.apellidoPaternoReceptor} ${dEnvio.apellidoMaternoReceptor}"

        binding.tvDireccionCompleta.text = "${dEnvio.calleDestino} #${dEnvio.numeroDestino}, " +
                "${dEnvio.ciudad}, ${dEnvio.estado}. CP: ${dEnvio.codigoPostal}"

        binding.tvSucursalOrigen.text = dEnvio.nombreSucursal ?: "Sucursal no asignada"
        binding.tvTelefonoCliente.text = "Tel: ${dEnvio.telefono ?: "N/A"}"
        binding.tvCorreoCliente.text = dEnvio.correo ?: "N/A"
        if (dEnvio.estatus == "Entregado" || dEnvio.estatus == "Cancelado") {
            binding.btnCambiarEstatus.visibility = View.GONE
        } else {
            binding.btnCambiarEstatus.visibility = View.VISIBLE
        }
    }

    private fun obtenerPaquetes(idEnvio: Int) {
        val urlPaquetes = "${Constantes().URL_API}paquete/obtener-por-envio/$idEnvio"

        Ion.with(this)
            .load("GET", urlPaquetes)
            .asByteArray()
            .setCallback { e, result ->
                if (e == null && result != null) {
                    val tipoListaPaquete = object : com.google.gson.reflect.TypeToken<List<Paquete>>() {}.type
                    val jsonString = String(result, Charsets.UTF_8)
                    val listaPaquetes: List<Paquete> = Gson().fromJson(jsonString, tipoListaPaquete)
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