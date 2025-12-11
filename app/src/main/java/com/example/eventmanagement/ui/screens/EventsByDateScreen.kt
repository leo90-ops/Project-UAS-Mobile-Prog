package com.example.eventmanagement.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.example.eventmanagement.Screen
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.ui.theme.*
import com.example.eventmanagement.ui.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.sortedBy
import kotlin.let
import kotlin.text.ifEmpty
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.to

/**
 * Events by Date Screen
 * Sesuai dengan:
 * - Class Diagram: EventsByDateScreen
 * - Use Case: Get Events by Date
 * - Activity Diagram: Get Events by Date
 *
 * Flow sesuai Activity Diagram:
 * 1. Show Date Picker → 2. User Select Date → 3. Format Date
 * 4. Show Loading → 5. Send GET Request → 6. Check Response
 * 7. Display List or Empty State → 8. User Actions (Click Event/Filter/Sort)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsByDateScreen(
    viewModel: EventViewModel,
    navController: NavController
) {
    // States
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Selected date state
    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Filter & Sort states
    var filterStatus by remember { mutableStateOf("all") }
    var sortBy by remember { mutableStateOf("time") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filtered and sorted events (Step 8 dari Activity Diagram)
    val filteredEvents = remember(events, filterStatus, sortBy) {
        var result = events

        // Apply status filter
        if (filterStatus != "all") {
            result = result.filter { it.status == filterStatus }
        }

        // Apply sorting
        result = when (sortBy) {
            "time" -> result.sortedBy { it.time }
            "location" -> result.sortedBy { it.location }
            "title" -> result.sortedBy { it.title }
            else -> result
        }

        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Cari Event dengan Kalender",
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedDate.isNotEmpty()) {
                            Text(
                                selectedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    // Filter & Sort Button
                    if (selectedDate.isNotEmpty() && events.isNotEmpty()) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Filter",
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Date Selection Card (Step 1 dari Activity Diagram)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pilih Tanggal",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                selectedDate.ifEmpty { "Tap untuk memilih tanggal" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }

                // Error Message (Step 6 dari Activity Diagram)
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearErrorMessage() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        // Step 4: Loading State
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // No date selected yet
                        selectedDate.isEmpty() -> {
                            InitialStateView(
                                onSelectDate = { showDatePicker = true },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Step 7: Empty State (No events found)
                        filteredEvents.isEmpty() && !isLoading -> {
                            EmptyEventsView(
                                date = selectedDate,
                                onSelectAnotherDate = { showDatePicker = true },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Step 7: Display Events List
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Event count and filter info
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${filteredEvents.size} Event ditemukan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Active filters badge
                                        if (filterStatus != "all" || sortBy != "time") {
                                            Surface(
                                                color = InfoBlue.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "Filter",
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = InfoBlue
                                                )
                                            }
                                        }
                                    }
                                }

                                // Events list (Step 8: Click Event)
                                items(filteredEvents) { event ->
                                    EventCard(
                                        event = event,
                                        onClick = {
                                            navController.navigate(
                                                Screen.EventDetail.createRoute(event.id ?: "")
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Step 1-2: Date Picker Dialog
    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date: String ->
                selectedDate = date
                // Step 3-5: Format Date & Send Request
                viewModel.getEventsByDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Step 8: Filter & Sort Dialog
    if (showFilterDialog) {
        FilterSortDialog(
            currentStatus = filterStatus,
            currentSort = sortBy,
            onApply = { status, sort ->
                filterStatus = status
                sortBy = sort
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

/**
 * Initial State View (Before date selected)
 */
@Composable
fun InitialStateView(
    onSelectDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = PrimaryPurple.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Cari Event dengan Kalender",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pilih tanggal untuk melihat Event yang tersedia",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSelectDate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih Tanggal")
        }
    }
}

/**
 * Empty Events View (Step 7 dari Activity Diagram)
 */
@Composable
fun EmptyEventsView(
    date: String,
    onSelectAnotherDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tidak ada Event",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tidak ada Event pada tanggal $date",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onSelectAnotherDate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih Tanggal Lain")
        }
    }
}

/**
 * Filter & Sort Dialog (Step 8 dari Activity Diagram)
 */
@Composable
fun FilterSortDialog(
    currentStatus: String,
    currentSort: String,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var selectedSort by remember { mutableStateOf(currentSort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Status Filter
                Text(
                    "Filter by Status:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Column {
                    listOf(
                        "all" to "Semua",
                        "upcoming" to "Akan Datang",
                        "ongoing" to "Berlangsung",
                        "completed" to "Selesai",
                        "cancelled" to "Dibatalkan"
                    ).forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedStatus == value,
                                onClick = { selectedStatus = value }
                            )
                            Text(
                                label,
                                modifier = Modifier.clickable { selectedStatus = value }
                            )
                        }
                    }
                }

                Divider()

                // Sort
                Text(
                    "Sort by:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Column {
                    listOf(
                        "time" to "Waktu",
                        "location" to "Lokasi",
                        "title" to "Nama Jadwal"
                    ).forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedSort == value,
                                onClick = { selectedSort = value }
                            )
                            Text(
                                label,
                                modifier = Modifier.clickable { selectedSort = value }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(selectedStatus, selectedSort)
            }) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * Simple Date Picker Dialog (Step 1-2 dari Activity Diagram)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    } ?: LocalDate.now()

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    onDateSelected(formatter.format(selectedDate))
                }
            ) {
                Text("Pilih")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}