package com.abdo.gestionnotesfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

enum class Screen {
    Students, Notes, Results
}

@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf(Screen.Students) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.Students,
                    onClick = { currentScreen = Screen.Students },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Étudiants") }
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.Notes,
                    onClick = { currentScreen = Screen.Notes },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("Notes") }
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.Results,
                    onClick = { currentScreen = Screen.Results },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Résultats") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.Students -> StudentsScreen()
                Screen.Notes -> NotesScreen()
                Screen.Results -> ResultsScreen()
            }
        }
    }
}

@Composable
fun StudentsScreen() {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Saisie étudiant", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom de l'étudiant") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("students")

            val id = ref.push().key!!

            val student = mapOf(
                "id" to id,
                "name" to name,
                "note" to note
            )

            ref.child(id).setValue(student)

            name = ""
            note = ""
        }) {
            Text("Ajouter étudiant")
        }
    }
}

@Composable
fun NotesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Saisie des notes", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Dans cette version, la note est saisie avec le nom de l'étudiant.")
    }
}

@Composable
fun ResultsScreen() {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("students")

    var resultText by remember {
        mutableStateOf("Chargement...")
    }

    LaunchedEffect(Unit) {
        ref.get().addOnSuccessListener { snapshot ->
            val result = StringBuilder()

            for (studentSnapshot in snapshot.children) {
                val name = studentSnapshot.child("name").value.toString()
                val note = studentSnapshot.child("note").value.toString()
                val status = if ((note.toDoubleOrNull() ?: 0.0) >= 10) "Validé" else "Non validé"

                result.append("Étudiant : $name\n")
                result.append("Note : $note\n")
                result.append("Résultat : $status\n\n")
            }

            resultText = if (result.isEmpty()) {
                "Aucun étudiant trouvé."
            } else {
                result.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Résultats", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        Text(resultText)
    }
}