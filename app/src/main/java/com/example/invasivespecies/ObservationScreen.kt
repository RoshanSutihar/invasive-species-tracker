package com.example.invasivespecies

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun AddObservationScreen(
    speciesImageUrl: String,
    navController: NavController,
    userId: Int,
    speciesId: Int,
    directoryModel: DbFunctions,
    speciesModel : SpeciesModel
) {
    // Access parks with .value
    val parks = directoryModel.parks.value

    var selectedPark by remember { mutableStateOf<Parks?>(null) }
    var comment by remember { mutableStateOf("") }
    val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // State for error message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(16.dp, 50.dp, 16.dp, 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Add Observation", style = MaterialTheme.typography.headlineMedium)




        // TextField for searching and selecting park
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                isDropdownExpanded = true // Expand dropdown when text changes
            },
            label = { Text("Search & Select Park") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown menu using LazyColumn
        if (isDropdownExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit height for dropdown
            ) {
                // Filter parks based on search text
                val filteredParks = parks.filter { park ->
                    park.name.contains(searchText, ignoreCase = true) // Filter by park name
                }

                items(filteredParks) { park ->
                    Text(
                        text = park.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                selectedPark = park
                                searchText = park.name
                                isDropdownExpanded = false // Close dropdown after selection
                            }
                    )
                }
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 6,
            minLines = 5
        )

        Text("Date: $date")
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(onClick = {
            if (selectedPark != null) {
                speciesModel.addObservation(
                    userId = userId,
                    parkId = selectedPark!!.idpark,
                    speciesId = speciesId,
                    comment = comment,
                    date = date,
                    onSuccess = {

                        navController.popBackStack(route = "", inclusive = false)
                    },
                    onFailure = { error ->
                        // Display an error message or handle the error as needed
                        errorMessage = error
                    }
                )
            } else {
                errorMessage = "Please select a park."
            }
        })
         {
            Text("Submit Observation")
        }
    }
}
