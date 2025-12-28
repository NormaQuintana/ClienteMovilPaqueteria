package uv.tc.clientemovilpaqueteria.adaptador

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uv.tc.clientemovilpaqueteria.DetalleEnvioActivity
import uv.tc.clientemovilpaqueteria.databinding.TarjetaEnvioBinding
import uv.tc.clientemovilpaqueteria.poko.Envio

class EnvioAdaptador(private val listaEnvios: List<Envio>) :
    RecyclerView.Adapter<EnvioAdaptador.EnvioViewHolder>() {

    class EnvioViewHolder(val binding: TarjetaEnvioBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnvioViewHolder {
        val binding = TarjetaEnvioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EnvioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EnvioViewHolder, position: Int) {
        val envio = listaEnvios[position]
        val direccionCompleta = "${envio.calleDestino} #${envio.numeroDestino}, " +
                "${envio.ciudad}, ${envio.estado}. CP: ${envio.codigoPostal}"

        holder.binding.tvGuia.text = "Guía: #${envio.noGuia}"
        holder.binding.tvDireccion.text = direccionCompleta
        holder.binding.tvEstatus.text = "Estatus: ${envio.status}"
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetalleEnvioActivity::class.java)
            intent.putExtra("noGuia", envio.noGuia)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaEnvios.size

}