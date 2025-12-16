package uv.tc.clientemovilpaqueteria.dto

import com.google.gson.annotations.SerializedName
import uv.tc.clientemovilpaqueteria.poko.Colaborador

data class RSAutenticacion(
    val error : Boolean,
    val mensaje : String,
    @SerializedName("colaborador")
    var conductor : Colaborador?
)
