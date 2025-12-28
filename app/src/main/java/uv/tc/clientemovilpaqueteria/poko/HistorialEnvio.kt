package uv.tc.clientemovilpaqueteria.poko

data class HistorialEnvio(
    val idEnvio: Int,
    val fechaHoraCambio: String,
    val comentario: String?,
    val nombreColaborador: String,
    val estatus: String
)
