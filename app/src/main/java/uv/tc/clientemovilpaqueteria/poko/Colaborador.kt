package uv.tc.clientemovilpaqueteria.poko

data class Colaborador(
    var idColaborador : Int,
    var nombre : String,
    var apellidoPaterno : String,
    var apellidoMaterno: String?,
    var curp : String? = null,
    var correo : String? = null,
    var noPersonal : String,
    var fotoBase64 : String? = null,
    var noLicencia : String,
    var idRol : Int,
    var nombreRol : String,
    var idSucursal : Int? = null,
    var nombreSucursal : String? = null
)
