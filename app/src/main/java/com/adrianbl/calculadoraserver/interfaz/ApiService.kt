package com.adrianbl.calculadoraserver.interfaz

import com.adrianbl.calculadoraserver.modelo.Datos
import com.adrianbl.calculadoraserver.modelo.Resultado
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("index.php")
    fun operacion(@Body request: Datos): Call<Resultado>
}
