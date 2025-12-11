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
import com.example.eventmanagement.ui.theme.PrimaryPurple
import com.example.eventmanagement.ui.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create/Edit Event Screen
 * Support mode create baru atau update event existing
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

    // State form
    var title by remember { mutableStateOf(editEvent?.title ?: "") }
    var date by remember { mutableStateOf(editEvent?.date ?: getCurrentDate()) }
    var time by remember { mutableStateOf(editEvent?.time ?: getCurrentTime()) }
    var location by remember { mutableStateOf(editEvent?.location ?: "") }
    var description by remember { mutableStateOf(editEvent?.description ?: "") }
    var capacityText by remember { mutableStateOf(editEvent?.capacity?.toString() ?: "") }
    var expandedStatus by remember { mutableStateOf(false) }
    val statusOptions = listOf("upcoming", "ongoing", "completed", "cancelled")
    var selectedStatus by remember { mutableStateOf(editEvent?.status ?: "upcoming") }

    // State untuk error judul
    var showTitleError by remember { mutableStateOf(false) }

    // Reset error saat user mengetik
    LaunchedEffect(title) {
        if (title.isNotBlank()) showTitleError = false
    }

    // Handle success → kembali otomatis
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onNavigateBack()
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Update Event" else "Buat Event Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditEvent()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error dari server
            errorMessage?.let {
                ErrorCard(message = it, onDismiss = { viewModel.clearErrorMessage() })
            }

            // Judul Event dengan Icon Title (bukan huruf T)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Event") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,  // Icon judul yang proper
                        contentDescription = "Judul"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = showTitleError,
                supportingText = if (showTitleError) {
                    { Text("Judul event wajib diisi") }
                } else null
            )

            // Tanggal
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Tanggal (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Tanggal") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Waktu
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Waktu (HH:MM)") },
                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Waktu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Lokasi
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokasi") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Lokasi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Deskripsi
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )

            // Kapasitas
            OutlinedTextField(
                value = capacityText,
                onValueChange = { capacityText = it.filter { char -> char.isDigit() } },
                label = { Text("Kapasitas (opsional)") },
                leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = "Kapasitas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Dropdown Status
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = it }
            ) {
                OutlinedTextField(
                    value = getStatusLabel(selectedStatus),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Status") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Status") },
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

            // Tombol Submit
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        showTitleError = true
                        return@Button
                    }

                    val capacity = capacityText.toIntOrNull()?.takeIf { it > 0 }
                    val newEvent = Event(
                        id = editEvent?.id,
                        title = title.trim(),
                        date = date.trim(),
                        time = time.trim(),
                        location = location.trim(),
                        description = description.takeIf { it.isNotBlank() }?.trim(),
                        capacity = capacity,
                        status = selectedStatus
                    )

                    if (isEditMode) {
                        viewModel.updateEvent(newEvent)
                    } else {
                        viewModel.createEvent(newEvent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Helper functions
private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun getStatusLabel(status: String): String {
    return when (status) {
        "upcoming" -> "Akan Datang"
        "ongoing" -> "Berlangsung"
        "completed" -> "Selesai"
        "cancelled" -> "Dibatalkan"
        else -> status.replaceFirstChar { it.uppercase() }
    }
}