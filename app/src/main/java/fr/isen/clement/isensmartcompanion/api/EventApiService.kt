package fr.isen.clement.isensmartcompanion.api

import fr.isen.clement.isensmartcompanion.models.Event
import retrofit2.Call
import retrofit2.http.GET

interface EventApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}
