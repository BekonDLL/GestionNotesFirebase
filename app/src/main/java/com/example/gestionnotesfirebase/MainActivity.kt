package com.abdo.gestionnotesfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdo.gestionnotesfirebase.ui.theme.GestionNotesFirebaseTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

data class Student(
    val id: String = "",
    val name: String = "",
    val apogeeCode: String = "",
    val cin: String = "",
    val note: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestionNotesFirebaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

enum class Screen {
    Students, Results
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf(Screen.Students) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (currentScreen == Screen.Students) "Saisie Étudiants" else "Résultats d'Examen",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.Students,
                    onClick = { currentScreen = Screen.Students },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Étudiants") }
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.Results,
                    onClick = { currentScreen = Screen.Results },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Résultats") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.Students -> StudentsScreen(snackbarHostState)
                Screen.Results -> ResultsScreen()
            }
        }
    }
}

@Composable
fun StudentsScreen(snackbarHostState: SnackbarHostState) {
    var name by remember { mutableStateOf("") }
    var apogeeCode by remember { mutableStateOf("") }
    var cin by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Nouvelle Entrée",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'étudiant") },
                    placeholder = { Text("Ex: Ahmed Benjelloun") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apogeeCode,
                    onValueChange = { apogeeCode = it },
                    label = { Text("Code Apogée") },
                    placeholder = { Text("Ex: 21004567") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cin,
                    onValueChange = { cin = it },
                    label = { Text("CIN") },
                    placeholder = { Text("Ex: AB123456") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    placeholder = { Text("Ex: 15.5") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && note.isNotBlank() && apogeeCode.isNotBlank() && cin.isNotBlank()) {
                            val database = FirebaseDatabase.getInstance()
                            val ref = database.getReference("students")
                            val id = ref.push().key!!
                            val student = Student(id, name, apogeeCode, cin, note)
                            ref.child(id).setValue(student).addOnSuccessListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Étudiant ajouté avec succès !")
                                }
                            }.addOnFailureListener {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Erreur lors de l'ajout")
                                }
                            }
                            name = ""
                            apogeeCode = ""
                            cin = ""
                            note = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter à la base", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ResultsScreen() {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("students")

    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Student>()
                for (studentSnapshot in snapshot.children) {
                    try {
                        val student = studentSnapshot.getValue(Student::class.java)
                        if (student != null) {
                            list.add(student)
                        }
                    } catch (_: Exception) {
                    }
                }
                students = list
                isLoading = false
                errorMessage = null
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                errorMessage = error.message
            }
        }
        ref.addValueEventListener(listener)
        onDispose {
            ref.removeEventListener(listener)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erreur: $errorMessage", color = MaterialTheme.colorScheme.error)
        }
    } else if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aucun étudiant trouvé", color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(students) { student ->
                StudentCard(student)
            }
        }
    }
}

@Composable
fun StudentCard(student: Student) {
    val noteValue = student.note.toDoubleOrNull() ?: 0.0
    val isValidated = noteValue >= 10.0
    val statusColor = if (isValidated) Color(0xFF4CAF50) else Color(0xFFF44336)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apogée: ${student.apogeeCode} • CIN: ${student.cin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Note: ${student.note}/20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isValidated) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isValidated) "Validé" else "Échec",
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
