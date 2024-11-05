package com.example.invasivespecies

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import android.net.NetworkCapabilities
import android.net.ConnectivityManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpeciesModel(application: Application) : AndroidViewModel(application) {
    private lateinit var restInterface: SpeciesAPIService
    private lateinit var dao: InvasiveSpeciesDao
    val key = mutableStateOf("")

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://cmsc106.net/invasive/")
            .build()
        restInterface = retrofit.create(SpeciesAPIService::class.java)


    }


    fun addObservation(
        userId: Int,
        parkId: Int,
        speciesId: Int,
        comment: String,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val newObservation = Observation(userId, parkId, speciesId, comment, date)


        viewModelScope.launch(Dispatchers.IO) {
            if (isConnectedToInternet()) {

                try {
                    Log.d("newObservation", "Attempting to create new observation: $newObservation")
                    val response = restInterface.newObservation(newObservation)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.code() == 200) {
                            Log.d("newObservation", "Observation successfully created.")
                            onSuccess()
                        } else {
                            Log.e(
                                "newObservation",
                                "Failed with response: ${response.errorBody()?.string()}"
                            )
                            onFailure("Failed with response code: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("newObservation", "Error creating observation", e)
                    withContext(Dispatchers.Main) {
                        onFailure("An error occurred: ${e.message}")
                    }
                }
            } else {

                try {
                    dao.insertObservation(newObservation)
                    withContext(Dispatchers.Main) {
                        Log.d("newObservation", "Observation saved locally due to no internet.")
                        onSuccess()
                    }
                } catch (e: Exception) {
                    Log.e("newObservation", "Error saving observation locally", e)
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to save observation locally: ${e.message}")
                    }
                }
            }
        }



        // end of main viewmodel
    }
    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Check if there's either a WiFi or cellular connection
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }





}
