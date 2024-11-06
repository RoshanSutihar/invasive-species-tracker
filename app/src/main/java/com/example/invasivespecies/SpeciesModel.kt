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

        val db = Room.databaseBuilder(
            application,
            SpeciesDatabase::class.java, "speciesDatabase"
        ).build()
        dao = db.gradesDao()

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
            try {
                // Attempt to create a new observation via the API first
                Log.d("newObservation", "Attempting to create new observation: $newObservation")
                val response = restInterface.newObservation(newObservation)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.code() == 200) {
                        Log.d("newObservation", "Observation successfully created.")
                        onSuccess() // Call onSuccess if the observation was created successfully
                    } else {
                        Log.e("newObservation", "Failed with response: ${response.errorBody()?.string()}")
                        onFailure("Failed with response code: ${response.code()}")
                        // If API fails, fall back to saving locally
                        saveObservationLocally(newObservation, onSuccess, onFailure)
                    }
                }
            } catch (e: Exception) {
                Log.e("newObservation", "Error creating observation", e)
                withContext(Dispatchers.Main) {
                    onFailure("No Internet. Data saved locally. Please sync data in your profile")
                }
                // If there is an error (e.g. network issue), save the observation locally
                saveObservationLocally(newObservation, onSuccess, onFailure)
            }
        }
    }

    private suspend fun saveObservationLocally(
        observation: Observation,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            dao.insertObservation(observation) // Save to local database
            withContext(Dispatchers.Main) {
                Log.d("newObservation", "Observation saved locally due to an error with the API.")
                onSuccess() // Call onSuccess when saved locally
            }
        } catch (e: Exception) {
            Log.e("newObservation", "Error saving observation locally", e)
            withContext(Dispatchers.Main) {
                onFailure("Failed to save observation locally: ${e.message}")
            }
        }
    }

    fun sync(onSuccess: (String) -> Unit,
             onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            val localObservations = dao.getAllObservations()

            if (localObservations.isEmpty()) {

                withContext(Dispatchers.Main) {

                    Log.d("Sync", "No data to sync.")
                    onFailure("No Data to sync!")

                }
            } else {

                try {
                    for (observation in localObservations) {

                        val response = restInterface.newObservation(observation)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Log.d("Sync", "Observation synced: $observation")

                                dao.deleteObservation(observation)
                                onSuccess("Data synced successfully!")
                            } else {
                                Log.e("Sync", "Failed to sync observation: ${observation.user}")
                                onFailure("Something went wrong!")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Sync", "Error during sync", e)
                    withContext(Dispatchers.Main) {

                    }
                }
            }
        }
    }



}
