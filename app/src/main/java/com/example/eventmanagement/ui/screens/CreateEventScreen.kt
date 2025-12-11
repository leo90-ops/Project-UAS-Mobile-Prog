package com.example.eventmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.ui.theme.*
import com.example.eventmanagement.ui.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.forEach
import kotlin.let
import kotlin.takeIf
import kotlin.text.isNotBlank
import kotlin.text.toIntOrNull

/**
 * Create/Edit Event Screen
 * Support mode create baru atau update event existing
 * Dipanggil dari: HomeScreen (FAB), EventDetailScreen (Edit Button)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val editEvent by viewModel.editEvent.collectAsState()
    val isEditMode = editEvent != null
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // State form (pre-fill jika mode edit)
    var title by remember { mutableStateOf(editEvent?.title ?: "") }
    var date by remember { mutableStateOf(editEvent?.date ?: getCurrentDate()) }
    var time by remember { mutableStateOf(editEvent?.time ?: getCurrentTime()) }
    var location by remember { mutableStateOf(editEvent?.location ?: "") }
    var description by remember { mutableStateOf(editEvent?.description ?: "") }
    var capacityText by remember { mutableStateOf(editEvent?.capacity?.toString() ?: "") }
    var expandedStatus by remember { mutableStateOf(false) }
    val statusOptions = listOf("upcoming", "ongoing", "completed", "cancelled")
    var selectedStatus by remember { mutableStateOf(editEvent?.status ?: "upcoming") }

    // Handle success (nav back otomatis)
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onNavigateBack()
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Update Event" else "Buat Event Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditEvent()  // Clear jika batal
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())  // Tambahkan scroll
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message jika ada
            errorMessage?.let {
                ErrorCard(message = it, onDismiss = { viewModel.clearErrorMessage() })
            }

            // Form Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Event") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Tanggal (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.DateRange, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Waktu (HH:MM)") },
                leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokasi") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = capacityText,
                onValueChange = { capacityText = it },
                label = { Text("Kapasitas (opsional)") },
                leadingIcon = { Icon(Icons.Default.PersonAdd, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = getStatusLabel(selectedStatus),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Status") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(getStatusLabel(status)) },
                            onClick = {
                                selectedStatus = status
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            // Tombol Buat/Update Event
            Button(
                onClick = {
                    val capacity = capacityText.toIntOrNull()?.takeIf { it > 0 }
                    val newEvent = Event(
                        id = editEvent?.id,  // Keep ID untuk update
                        title = title,
                        date = date,
                        time = time,
                        location = location,
                        description = description.takeIf { it.isNotBlank() },
                        capacity = capacity,
                        status = selectedStatus  // Gunakan selectedStatus dari dropdown
                    )

                    if (isEditMode) {
                        viewModel.updateEvent(newEvent) { onNavigateBack() }
                    } else {
                        viewModel.createEvent(newEvent) { onNavigateBack() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditMode) "Update Event" else "Buat Event",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Extra space di bottom agar tombol tidak terpotong
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper functions untuk default values
private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

// Helper untuk label status
private fun getStatusLabel(status: String): String {
    return when (status) {
        "upcoming" -> "Akan Datang"
        "ongoing" -> "Berlangsung"
        "completed" -> "Selesai"
        "cancelled" -> "Dibatalkan"
        else -> status
    }
}