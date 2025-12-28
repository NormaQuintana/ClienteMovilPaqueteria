package uv.tc.clientemovilpaqueteria.poko

data class Paquete(
    val idPaquete: Int,
    val descripcion: String,
    val peso: Double,
    val alto: Double,
    val ancho: Double,
    val profundidad: Double,
    val idEnvio: Int
)
