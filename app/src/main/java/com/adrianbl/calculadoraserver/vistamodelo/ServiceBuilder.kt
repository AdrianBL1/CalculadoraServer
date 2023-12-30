package com.adrianbl.calculadoraserver.vistamodelo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private const val baseUrl = "https://accepted-evolving-foxhound.ngrok-free.app/desarrolloweb/server_movil/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}