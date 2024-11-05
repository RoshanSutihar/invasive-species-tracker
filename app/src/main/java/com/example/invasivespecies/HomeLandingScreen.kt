package com.example.invasivespecies



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesListScreen(
    user: UserResponse,
    directoryModel: DbFunctions,
    navController: NavController
) {
    val speciesList = directoryModel.species.value // Access the species list

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Species List") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("profile/${user.iduser}/${user.username}/${user.realname}/${user.phone}")
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "See Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(speciesList) { species ->
                    SpeciesItem(species = species) {
                        val encodedImageUrl = URLEncoder.encode(species.image, "UTF-8")
                        navController.navigate("speciesDetail/${user.iduser}/${species.idspecies}/${species.name}/$encodedImageUrl")
                    }
                }
            }
        }
    }
}



@Composable
fun SpeciesItem(species: Species, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = species.name, // Display the species name
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}




@Composable
fun ProfileScreen(
    navController: NavController,
    userId: Int,
    username: String,
    realname: String,
    phone: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("User ID: $userId")
        Text("Username: $username")
        Text("Real Name: $realname")
        Text("Phone: $phone")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            // Implement sync functionality here
        }) {
            Text("Sync Data")
        }
    }
}