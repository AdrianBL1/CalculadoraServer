package com.adrianbl.calculadoraserver.vista

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.adrianbl.calculadoraserver.R
import com.adrianbl.calculadoraserver.vistamodelo.ServiceBuilder
import com.adrianbl.calculadoraserver.interfaz.ApiService
import com.adrianbl.calculadoraserver.modelo.Datos
import com.adrianbl.calculadoraserver.modelo.Resultado
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val apiService = ServiceBuilder.buildService(ApiService::class.java)

    private var textoSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setup()
    }

    private fun setup(){
        val notificacion = findViewById<ImageView>(R.id.posterImageView)
        val estado = findViewById<TextView>(R.id.txtEstado)

        // Verificar si hay conexión a Internet antes de realizar la llamada al servidor
        if (isNetworkAvailable()) {
            notificacion.setBackgroundColor(Color.GREEN)
            estado.setText(R.string.estado_conectado)
            ejecutarOperacion()
        } else {
            sweetAlertError("No hay conexión a Internet")
            notificacion.setBackgroundColor(Color.RED)
            estado.setText(R.string.estado_no_internet)
        }
    }

    private fun ejecutarOperacion(){
        val btnEnviar = findViewById<Button>(R.id.enviar)
        btnEnviar.setOnClickListener {
            validarDatos()
        }
    }

    private fun validarDatos(){
        val num1 = findViewById<EditText>(R.id.txt_num1).text
        val num2 = findViewById<EditText>(R.id.txt_num2).text

        if (num1.isNotEmpty() && num2.isNotEmpty()){
            tomarValoresTG()
            if (textoSeleccionado.isNotEmpty()){
                Log.d("Datos enviados","Servicio: ${textoSeleccionado.toUpperCase()},x: ${num1}, y: ${num2}")
                enviarDatosAlServidor(textoSeleccionado.toUpperCase(),num1.toString().toInt(),num2.toString().toInt())
            } else {
                sweetAlertAdvertencia("No ha seleccionado una operación")
            }
        } else {
            sweetAlertAdvertencia("Uno o mas campos están vacios")
        }
    }

    private fun tomarValoresTG(){
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.tgOperaciones)

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedButton: MaterialButton = findViewById(checkedId)
                textoSeleccionado = selectedButton.text.toString()
                //Log.d("TAG","Texto seleccionado: $textoSeleccionado")
            }
        }
    }

    private fun enviarDatosAlServidor(servicio: String, x: Int, y: Int) {
        val operacionRequest = Datos(servicio, x, y)

        val call = apiService.operacion(operacionRequest)

        call.enqueue(object : Callback<Resultado> {
            override fun onResponse(call: Call<Resultado>, response: Response<Resultado>) {
                if (response.isSuccessful) {
                    val resultado = response.body()?.resultado
                    Log.d("Respuesta del servidor", "Resultado: $resultado")
                    // Mostrar resultado
                    val txt_resultado = findViewById<TextView>(R.id.txtResultado)
                    txt_resultado.text = "Resultado: $resultado"

                    val notificacion = findViewById<ImageView>(R.id.posterImageView)
                    val estado = findViewById<TextView>(R.id.txtEstado)
                    notificacion.setBackgroundColor(Color.GREEN)
                    estado.setText(R.string.estado_conectado)
                } else {
                    val errorResponse = response.errorBody()?.string()
                    Log.e("Error en la respuesta", errorResponse ?: "Error desconocido")
                    // Manejar el error de la respuesta
                    sweetAlertError(errorResponse ?: "Error desconocido")
                    val notificacion = findViewById<ImageView>(R.id.posterImageView)
                    val estado = findViewById<TextView>(R.id.txtEstado)
                    notificacion.setBackgroundColor(Color.YELLOW)
                    estado.setText(R.string.error_servidor)
                }
            }

            override fun onFailure(call: Call<Resultado>, t: Throwable) {
                Log.e("Error en la llamada", "Falló la llamada al servidor", t)
                // Manejar el fallo de la llamada
            }
        })
    }

    private fun sweetAlertError(mensaje: String){
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(R.string.error)
            .setContentText(mensaje)
            .show()
    }

    private fun sweetAlertAdvertencia(mensaje: String){
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(R.string.advertencia)
            .setContentText(mensaje)
            .show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}