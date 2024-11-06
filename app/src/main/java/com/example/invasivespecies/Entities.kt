package com.example.invasivespecies

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity
data class Species(
    @PrimaryKey  val idspecies: Int,
    val name: String,
    val image: String? = null
)


@Entity
data class Parks(
    @PrimaryKey  val idpark: Int,
    val name: String,
    val county: String?=null
)


@Entity
data class Observation(
    val user: Int ,
    val park: Int,
    val species: Int,
    @PrimaryKey val comment: String,
    val date: String
)

@Dao
interface InvasiveSpeciesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecies(species: List<Species>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<Species>)

    @Query("SELECT * FROM species")
    suspend fun getAllSpecies(): List<Species>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParks(parks: List<Parks>)

    @Query("SELECT * FROM parks")
    suspend fun getAllParks(): List<Parks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: Observation)

    @Query("SELECT * FROM observation")
    suspend fun getAllObservations(): List<Observation>

    @Delete
    suspend fun deleteObservation(observation: Observation)
}



@Database(entities = [Species::class, Parks::class, Observation::class], version = 1)
abstract class SpeciesDatabase : RoomDatabase() {
    abstract fun gradesDao() : InvasiveSpeciesDao
}