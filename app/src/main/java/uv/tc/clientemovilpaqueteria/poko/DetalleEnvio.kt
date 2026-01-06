package uv.tc.clientemovilpaqueteria.poko

data class DetalleEnvio(
    val idEnvio: Int,
    val noGuia: String,
    val nombreReceptor: String,
    val apellidoPaternoReceptor: String,
    val apellidoMaternoReceptor: String,
    val calleDestino: String,
    val numeroDestino: String,
    val ciudad: String,
    val estado: String,
    val codigoPostal: Int,
    var estatus: String? = "Sin Estatus",
    val nombreSucursal: String?,
    val telefono: String?,
    val correo: String?
)
