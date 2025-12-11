package com.example.eventmanagement.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventmanagement.Screen
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.ui.theme.*
import com.example.eventmanagement.ui.viewmodel.EventViewModel
import kotlin.apply
import kotlin.let
import kotlin.text.isNullOrBlank
import kotlin.text.trimIndent

/**
 * Event Detail Screen
 * Sesuai dengan:
 * - Class Diagram: EventDetailScreen
 * - Use Case: Get Event by ID
 * - Activity Diagram: Get Event by ID
 *
 * Flow sesuai Activity Diagram:
 * 1. Get Event ID → 2. Show Loading → 3. Send GET Request
 * 4. Check Response → 5. Display Details → 6. Show Action Buttons
 * 7. User Actions (Edit/Delete/Share)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    // States (Step 4-5 dari Activity Diagram)
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Delete Confirmation Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Step 1-3: Load Event by ID
    LaunchedEffect(eventId) {
        viewModel.getEventById(eventId)
    }

    // Handle Success Message
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            navController.popBackStack()
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Event", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelectedEvent()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    // Share Action (Step 7 dari Activity Diagram)
                    selectedEvent?.let { event ->
                        IconButton(onClick = {
                            val shareText = """
                                ${event.title}
                                
                                📅 ${event.date} 
                                ⏰ ${event.time}
                                📍 ${event.location}
                                ${event.description?.let { "\n$it" } ?: ""}
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Event"))
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Step 2: Loading State
                isLoading && selectedEvent == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Step 4: Error State (404 or Network Error)
                errorMessage != null && selectedEvent == null -> {
                    ErrorStateView(
                        message = errorMessage ?: "Event tidak ditemukan",
                        onRetry = {
                            viewModel.clearErrorMessage()
                            viewModel.getEventById(eventId)
                        },
                        onBack = {
                            viewModel.clearErrorMessage()
                            navController.popBackStack()
                        }
                    )
                }

                // Step 5: Success - Display Event Details
                selectedEvent != null -> {
                    EventDetailContent(
                        event = selectedEvent!!,
                        onEditClick = {
                            // Set selectedEvent untuk mode edit di CreateEventScreen
                            viewModel.setSelectedEventForEdit(selectedEvent!!)
                            navController.navigate(Screen.CreateEvent.route)
                        },
                        onDeleteClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Step 7: Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ErrorRed
                )
            },
            title = { Text("Hapus Event?") },
            text = {
                Text("Apakah Anda yakin ingin menghapus event ini? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEvent(eventId) {
                            // Navigate back on success
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Event Detail Content (Step 5 dari Activity Diagram)
 */
@Composable
fun EventDetailContent(
    event: Event,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card with Gradient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryPurple, SecondaryPurple)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusBadgeWhite(status = event.status)
                }
            }
        }

        // Date & Time Card
        DetailInfoCard(
            icon = Icons.Default.DateRange,
            title = "Tanggal & Waktu",
            content = "${event.date}\n${event.time}",
            iconColor = InfoBlue
        )

        // Location Card
        DetailInfoCard(
            icon = Icons.Default.Place,
            title = "Lokasi",
            content = event.location,
            iconColor = ErrorRed
        )

        // Description Card
        if (!event.description.isNullOrBlank()) {
            DetailInfoCard(
                icon = Icons.Default.Info,
                title = "Deskripsi",
                content = event.description,
                iconColor = WarningOrange
            )
        }

        // Capacity Card
        if (event.capacity != null) {
            DetailInfoCard(
                icon = Icons.Default.Person,
                title = "Kapasitas",
                content = "${event.capacity} orang",
                iconColor = SuccessGreen
            )
        }

        // Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Informasi Tambahan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Divider()
                if (event.createdAt != null) {
                    MetadataRow("Dibuat", event.createdAt)
                }
                if (event.updatedAt != null) {
                    MetadataRow("Diperbarui", event.updatedAt)
                }
                MetadataRow("ID Event", event.id ?: "-")
            }
        }

        // Step 6: Action Buttons
        ActionButtonsSection(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailInfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusBadgeWhite(status: String) {
    val (backgroundColor, label) = when (status) {
        "upcoming" -> Pair(Color.White.copy(alpha = 0.3f), "Akan Datang")
        "ongoing" -> Pair(Color.White.copy(alpha = 0.3f), "Berlangsung")
        "completed" -> Pair(Color.White.copy(alpha = 0.3f), "Selesai")
        "cancelled" -> Pair(Color.White.copy(alpha = 0.3f), "Dibatalkan")
        else -> Pair(Color.White.copy(alpha = 0.3f), status)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Action Buttons Section (Step 6 dari Activity Diagram)
 */
@Composable
fun ActionButtonsSection(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Tindakan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Edit Button (baru ditambahkan)
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update Event", fontWeight = FontWeight.Bold)
        }

        // Delete Button
        Button(
            onClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hapus Event", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Error State View (Step 4 dari Activity Diagram)
 */
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = ErrorRed
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Retry Button
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Coba Lagi")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Back Button
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
    }
}