package com.example.invasivespecies
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface SpeciesAPIService {

    @POST("users")
    suspend fun newUser(@Body user : User) : UserResponse

    @GET("users")
    suspend fun login(@Query("user") user : String,@Query("password") password : String) : UserResponse

    @GET("species")
    suspend fun getSpecies(): List<Species>

    @GET("parks")
    suspend fun getAllParks(): List<Parks>

    @POST("observations")
    suspend fun newObservation(@Body user : Observation) :Response<String>
}