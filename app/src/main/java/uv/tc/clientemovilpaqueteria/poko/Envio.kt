package uv.tc.clientemovilpaqueteria.poko

data class Envio(
    val noGuia: String,
    val calleDestino: String,
    val numeroDestino: String,
    val ciudad: String,
    val estado: String,
    val codigoPostal: Int,
    var estatus: String? = "Sin Estatus",
)
