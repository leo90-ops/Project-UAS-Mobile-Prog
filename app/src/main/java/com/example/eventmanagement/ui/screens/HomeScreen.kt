package com.example.eventmanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eventmanagement.Screen
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.data.model.Statistics
import com.example.eventmanagement.ui.theme.*
import com.example.eventmanagement.ui.viewmodel.EventViewModel
import kotlin.math.min
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.slice
import kotlin.let
import kotlin.ranges.until
import kotlin.text.contains
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.to

/* ===================== HOME SCREEN ===================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EventViewModel,
    navController: NavController
) {
    val events by viewModel.events.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // State untuk pagination
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 5
    val totalPages = if (events.isNotEmpty()) (events.size + pageSize - 1) / pageSize else 0
    val startIndex = currentPage * pageSize
    val endIndex = min(startIndex + pageSize, events.size)

    // State untuk search
    var searchQuery by remember { mutableStateOf("") }

    // State untuk filter status (dropdown kecil di sebelah "Semua Event")
    var filterStatus by remember { mutableStateOf("all") }
    var expandedFilter by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        "all" to "Semua",
        "upcoming" to "Akan Datang",
        "ongoing" to "Berlangsung",
        "completed" to "Selesai",
        "cancelled" to "Dibatalkan"
    )

    // State untuk Navigation Drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Current route untuk highlight drawer item
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.Home.route

    LaunchedEffect(Unit) {
        viewModel.getAllEvents()
        viewModel.getStatistics()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Header drawer (opsional)
                Text(
                    text = "Menu Utama",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                // Items drawer
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = currentRoute == Screen.Home.route,
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Detail") },
                    selected = currentRoute == Screen.EventsByDate.route,
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        navController.navigate(Screen.EventsByDate.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    // Row pertama: Menu (garis tiga) di kiri, Profile icon di kanan (sejajar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryPurple)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        // Profile Avatar (sejajar dengan menu, clickable untuk navigasi Profil)
                        Surface(
                            shape = CircleShape,
                            color = PrimaryPurple.copy(alpha = 0.1f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { navController.navigate(Screen.Profile.route) }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Search Bar (kembali seperti awal, full width)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Event") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 12.dp),  // Ruang untuk tidak mepet
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    // Divider jika diperlukan
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateEvent.route) },
                    containerColor = PrimaryPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },

            ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item { QuickActionsSection(navController) }
                item { StatisticsSection(statistics) }

                errorMessage?.let {
                    item {
                        ErrorCard(message = it, onDismiss = { viewModel.clearErrorMessage() })
                    }
                }

                // Item untuk "Semua Event" dengan filter di sebelah kanan
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Semua Event (${events.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Filter Icon Kecil (hanya icon, expand dropdown saat ditekan, align ke kanan)
                        IconButton(
                            onClick = { expandedFilter = !expandedFilter },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter Status",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Dropdown untuk filter (muncul saat icon ditekan, align ke kanan atas)
                        DropdownMenu(
                            expanded = expandedFilter,
                            onDismissRequest = { expandedFilter = false },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            statusOptions.forEach { (status, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        filterStatus = status
                                        expandedFilter = false
                                    }
                                )
                            }
                        }
                    }
                }

                when {
                    isLoading && events.isEmpty() -> {
                        item { CircularProgressIndicator() }
                    }
                    events.isEmpty() -> {
                        item { EmptyStateCard() }
                    }
                    else -> {
                        // Filter events berdasarkan search query dan status
                        val filteredEvents = if (searchQuery.isEmpty() && filterStatus == "all") {
                            events
                        } else {
                            var tempEvents = events
                            if (searchQuery.isNotEmpty()) {
                                tempEvents = tempEvents.filter { it.title.contains(searchQuery, ignoreCase = true) }
                            }
                            if (filterStatus != "all") {
                                tempEvents = tempEvents.filter { it.status == filterStatus }
                            }
                            tempEvents
                        }
                        val filteredTotalPages = if (filteredEvents.isNotEmpty()) (filteredEvents.size + pageSize - 1) / pageSize else 0
                        val filteredStartIndex = currentPage * pageSize
                        val filteredEndIndex = min(filteredStartIndex + pageSize, filteredEvents.size)

                        // Tampilkan item paginated
                        items(filteredEvents.slice(filteredStartIndex until filteredEndIndex)) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    navController.navigate(
                                        Screen.EventDetail.createRoute(event.id ?: "")
                                    )
                                }
                            )
                        }

                        // Pagination controls (hanya jika filteredTotalPages > 1)
                        if (filteredTotalPages > 1) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tombol Previous
                                    IconButton(
                                        onClick = { if (currentPage > 0) currentPage-- },
                                        enabled = currentPage > 0
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowLeft,
                                            contentDescription = "Sebelumnya",
                                            tint = if (currentPage > 0) PrimaryPurple else Color.Gray
                                        )
                                    }

                                    // Indikator halaman
                                    Text(
                                        text = "Halaman ${currentPage + 1} dari $filteredTotalPages",
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    // Tombol Next
                                    IconButton(
                                        onClick = { if (currentPage < filteredTotalPages - 1) currentPage++ },
                                        enabled = currentPage < filteredTotalPages - 1
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Selanjutnya",
                                            tint = if (currentPage < filteredTotalPages - 1) PrimaryPurple else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ===================== QUICK ACTION ===================== */

@Composable
fun QuickActionsSection(navController: NavController) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚡ Aksi Cepat", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Buat",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(Screen.CreateEvent.route)
                }

                QuickActionButton(
                    icon = Icons.Default.DateRange,
                    label = "Lihat",
                    color = InfoBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(Screen.EventsByDate.route)
                }
            }
        }
    }
}

/* ===================== STATISTICS ===================== */

@Composable
fun StatisticsSection(statistics: Statistics?) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📊 Statistik Event", fontWeight = FontWeight.Bold)

            if (statistics == null) {
                CircularProgressIndicator()
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Total", statistics.total, PrimaryPurple, Modifier.weight(1f))
                    StatCard("Akan Datang", statistics.upcoming, StatusUpcoming, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Berlangsung", statistics.ongoing, StatusOngoing, Modifier.weight(1f))
                    StatCard("Selesai", statistics.completed, StatusCompleted, Modifier.weight(1f))
                }
            }
        }
    }
}

/* ===================== EVENT CARD ===================== */

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold)
                Text("${event.date} • ${event.time}")
                Text(event.location)
                Spacer(modifier = Modifier.height(6.dp))
                StatusBadge(event.status)
            }
            Icon(Icons.Default.KeyboardArrowRight, null)
        }
    }
}

/* ===================== STATUS BADGE ===================== */

@Composable
fun StatusBadge(status: String) {
    val (bg, text, label) = when (status) {
        "upcoming" -> Triple(StatusUpcoming.copy(alpha = 0.2f), StatusUpcoming, "Akan Datang")
        "ongoing" -> Triple(StatusOngoing.copy(alpha = 0.2f), StatusOngoing, "Berlangsung")
        "completed" -> Triple(StatusCompleted.copy(alpha = 0.2f), StatusCompleted, "Selesai")
        "cancelled" -> Triple(StatusCancelled.copy(alpha = 0.2f), StatusCancelled, "Dibatalkan")
        else -> Triple(Color.Gray.copy(alpha = 0.2f), Color.Gray, status)
    }

    Surface(color = bg, shape = RoundedCornerShape(12.dp)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = text,
            fontWeight = FontWeight.Medium
        )
    }
}


/* ===================== SUPPORT UI ===================== */

@Composable
fun EmptyStateCard() {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Belum ada event")
        }
    }
}

@Composable
fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = ErrorRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, modifier = Modifier.weight(1f), color = ErrorRed)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label)
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color, color.copy(alpha = 0.7f))
                )
            )
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(value.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}