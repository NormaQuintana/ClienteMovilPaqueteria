package uv.tc.clientemovilpaqueteria

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import uv.tc.clientemovilpaqueteria.databinding.ActivityLoginBinding
import uv.tc.clientemovilpaqueteria.dto.RSAutenticacion
import uv.tc.clientemovilpaqueteria.util.Constantes

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnIngresar.setOnClickListener {
            verificarCredenciales()
        }

    }


    fun verificarCredenciales(){
        if(sonCamposValidos()){
            consumirAPI(binding.etNoPersonal.text.toString(), binding.etPassword.text.toString())

        }
    }
    fun sonCamposValidos() : Boolean{
        var valido = true
        if (binding.etNoPersonal.text.isEmpty()){
            binding.etNoPersonal.setError("No. de Personal obligatorio")
            valido = false
        }
        if(binding.etPassword.text.isEmpty()){
            binding.etPassword.setError("Constraseña obligatoria")
            valido  = false
        }

        return valido
    }

    fun consumirAPI(noPersonal : String, password : String){
        Ion.getDefault(this@LoginActivity).conscryptMiddleware.enable(false)
        Ion.with(this@LoginActivity)
            .load("POST", "${Constantes().URL_API}autenticacion/conductor/")
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .setBodyParameter("noPersonal", noPersonal)
            .setBodyParameter("password", password)
            .asByteArray()
            .setCallback { e, result ->
                if(e == null){
                    val respuestaJson = String(result, Charsets.UTF_8)
                    serializarRepsuesta(respuestaJson)
                }else{
                    //ERROR
                    Toast.makeText(this@LoginActivity,
                        "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun serializarRepsuesta(json : String){
        try {
            val gson : Gson = Gson()
            val respuestaLogin = gson.fromJson(json, RSAutenticacion::class.java)
            if(!respuestaLogin.error){
                val preferences = getSharedPreferences("SESION_CONDUCTOR", MODE_PRIVATE)
                val editor = preferences.edit()
                respuestaLogin.conductor?.idColaborador?.let { id ->
                    editor.putInt("idColaborador", id)
                    editor.apply()
                }
                Toast.makeText(this@LoginActivity,
                    "Bienvenido(a) ${respuestaLogin.conductor!!.nombre} a PacketWorld",
                    Toast.LENGTH_LONG).show()
                irPantallaPrincipal(json)
            }else{
                Toast.makeText(this@LoginActivity,
                    respuestaLogin.mensaje,
                    Toast.LENGTH_LONG).show()
            }
        }catch (e : Exception){
            Toast.makeText(this@LoginActivity,
                "Lo sentimos hubo un error en la solicitud",
                Toast.LENGTH_LONG).show()
        }
    }

    fun irPantallaPrincipal(json : String){
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("conductor", json)
        startActivity(intent)
        finish()
    }

}