package com.example.invasivespecies

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DbFunctions(application: Application) : AndroidViewModel(application) {

    private lateinit var restInterface: SpeciesAPIService

    val key = mutableStateOf("")
    lateinit var dao: InvasiveSpeciesDao

    val species = mutableStateOf<List<Species>>(emptyList())
    val parks = mutableStateOf<List<Parks>>(emptyList())
    val firstSpecies = mutableStateOf<Species?>(null)
    val firstPark = mutableStateOf<Parks?>(null)


    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://cmsc106.net/invasive/")
            .build()
        restInterface = retrofit.create(SpeciesAPIService::class.java)

        // database initialing here

        val db = Room.databaseBuilder(
            application,
            SpeciesDatabase::class.java, "speciesDatabase"
        ).build()
        dao = db.gradesDao()


        viewModelScope.launch {
            loadSpecies()
            loadParks()
            fetchSpeciesFromDatabase()
            fetchParksFromDatabase()
        }
        // end of init function
    }


     suspend fun insertObservation(observation: Observation) {

            try {
                dao.insertObservation(observation)
                Log.d("saveObservationLocally", "Observation saved locally: $observation")
            } catch (e: Exception) {
                Log.e("saveObservationLocally", "Failed to save observation locally", e)
            }

    }





    // for room db fetching
    private suspend fun loadSpecies() {

        val existingSpecies = withContext(Dispatchers.IO) {
            dao.getAllSpecies()
        }

        if (existingSpecies.isEmpty()) {

            val speciesList = fetchSpeciesFromApi()
            if (speciesList.isNotEmpty()) {

                withContext(Dispatchers.IO) {
                    dao.insertAll(speciesList)
                }

                species.value = speciesList
            }
        } else {

            species.value = existingSpecies
        }


        firstSpecies.value = species.value.firstOrNull()
    }

    // load parks for room
    private suspend fun loadParks() {

        val existingParks = withContext(Dispatchers.IO) {
            dao.getAllParks()
        }

        if (existingParks.isEmpty()) {

            val parksList = fetchParksFromApi()
            if (parksList.isNotEmpty()) {

                withContext(Dispatchers.IO) {
                    dao.insertAllParks(parksList)
                }

                parks.value = parksList
            }
        } else {

            parks.value = existingParks
        }


        firstPark.value = parks.value.firstOrNull()
    }

    private suspend fun fetchSpeciesFromApi(): List<Species> {
        return withContext(Dispatchers.IO) {
            try {

                restInterface.getSpecies()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private suspend fun fetchParksFromApi(): List<Parks> {
        return withContext(Dispatchers.IO) {
            try {

                restInterface.getAllParks()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private suspend fun fetchSpeciesFromDatabase() {
        val speciesList = withContext(Dispatchers.IO) {
            dao.getAllSpecies()
        }
        species.value = speciesList
    }

    private suspend fun fetchParksFromDatabase() {
        val parksList = withContext(Dispatchers.IO) {
            dao.getAllParks()
        }
        parks.value = parksList
    }


}