package com.example.invasivespecies

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MiscModel (application: Application) : AndroidViewModel(application) {
    private lateinit var restInterface: SpeciesAPIService
    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://cmsc106.net/invasive/")
            .build()
        restInterface = retrofit.create(SpeciesAPIService::class.java)

    }

    fun newUser(name: String, password: String, realname: String, phone: String, onSuccess: (Int) -> Unit) {
        val newUser = User(name, password, realname, phone)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("newUser", "Attempting to create new user: $newUser")
                val result: UserResponse = restInterface.newUser(newUser)
                withContext(Dispatchers.Main) {
                    val userid = result.iduser
                    Log.d("newUser", "User created successfully with ID: $userid")
                    onSuccess(userid)
                }
            } catch (e: Exception) {
                Log.e("newUser", "Error creating user", e)
                e.printStackTrace()
            }
        }
    }



    fun login(
        name: String,
        password: String,
        onSuccess: (UserResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val user = restInterface.login(name, password)
                withContext(Dispatchers.Main) {
                    if (user != null && user.username.isNotEmpty()) {
                        onSuccess(user)
                    } else {
                        onError("Username or password is incorrect.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Username or password is wrong")
                }
            }
        }
    }


}