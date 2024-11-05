package com.example.invasivespecies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.invasivespecies.ui.theme.InvasiveSpeciesTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vm = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(SpeciesModel::class.java)

        setContent {
            InvasiveSpeciesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpeciesApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}




@Composable
fun SpeciesApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val directoryModel: DbFunctions = viewModel()
    val miscModel: MiscModel = viewModel()
    val speciesModel: SpeciesModel = viewModel()
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                miscModel = miscModel,
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("main") // Navigate to the main screen
                },
                toRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                miscModel = miscModel,
                navController = navController, modifier = modifier
            )
        }

        composable("speciesList/{iduser}/{username}/{realname}/{phone}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("iduser")?.toIntOrNull()
            val username = backStackEntry.arguments?.getString("username")
            val realName = backStackEntry.arguments?.getString("realname")
            val phone = backStackEntry.arguments?.getString("phone")

            // Create a User object from the extracted parameters
            val user = userId?.let { UserResponse(it, username ?: "", "", realName ?: "", phone ?: "") }

            if (user != null) {
                SpeciesListScreen(
                    directoryModel = directoryModel,
                    navController = navController,
                    user = user
                )
            }
        }

        composable("profile/{userId}/{username}/{realname}/{phone}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val realname = backStackEntry.arguments?.getString("realname") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            ProfileScreen(
                navController = navController,
                userId = userId,
                username = username,
                realname = realname,
                phone = phone
            )
        }
        composable("speciesDetail/{userId}/{speciesId}/{speciesName}/{speciesImageUrl}") { backStackEntry ->
            val speciesId = backStackEntry.arguments?.getString("speciesId")?.toIntOrNull() ?: 0
            val speciesName = backStackEntry.arguments?.getString("speciesName") ?: ""
            val speciesImageUrl = backStackEntry.arguments?.getString("speciesImageUrl")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            // Pass these values to the SpeciesDetailScreen
            SpeciesDetailScreen(
                navController = navController,
                speciesId = speciesId,
                speciesName = speciesName,
                speciesImageUrl = speciesImageUrl,
                userId = userId
            )
        }


        composable("addObservation/{userId}/{speciesId}/{imageURL}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            val speciesId = backStackEntry.arguments?.getString("speciesId")?.toIntOrNull() ?: 0
            val speciesImageUrl = backStackEntry.arguments?.getString("imageURL")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            AddObservationScreen(speciesImageUrl,navController = navController, userId, speciesId,  directoryModel = directoryModel, speciesModel = speciesModel)
        }

        }


    }





@Composable
fun LoginScreen(
    miscModel: MiscModel,
    onLoginSuccess: (UserResponse) -> Unit,
    navController: NavController,
    toRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // State for error message

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            miscModel.login(username, password, { user ->
                navController.navigate("speciesList/${user.iduser}/${user.username}/${user.realname}/${user.phone}")
            }, { error ->
                errorMessage = error
            })
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = toRegister) {
            Text("Don't have an account? Register")
        }
    }
}





@Composable
fun RegisterScreen(
    miscModel: MiscModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val realName = remember { mutableStateOf("") }
    val userId = remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(20.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Create an Account",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // real name
        TextField(
            value = realName.value,
            onValueChange = { realName.value = it },
            label = { Text("Your Real Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        //phone number
        TextField(
            value = phone.value,
            onValueChange = { phone.value = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Password Field
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))


        Button(
            onClick = {
                miscModel.newUser(username.value, password.value, realName.value, phone.value) { userid ->
                    userId.value = userid.toString()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))


        userId.value?.let {
            Text("User ID: $it", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }


        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Log in")
        }


        userId.value?.let {
            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
            }
        }
    }
}






