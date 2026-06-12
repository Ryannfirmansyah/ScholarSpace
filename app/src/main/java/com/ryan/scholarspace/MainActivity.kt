package com.ryan.scholarspace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.ryan.scholarspace.data.database.UserEntity
import com.ryan.scholarspace.data.model.Course
import com.ryan.scholarspace.data.model.Scholarship
import com.ryan.scholarspace.ui.theme.ColorClosed
import com.ryan.scholarspace.ui.theme.ColorOpen
import com.ryan.scholarspace.ui.theme.ScholarSpaceTheme
import com.ryan.scholarspace.ui.theme.GoldStar
import com.ryan.scholarspace.ui.viewmodel.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ScholarSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            ScholarSpaceTheme(darkTheme = isDarkMode) {
                EduSearchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EduSearchApp(viewModel: ScholarSpaceViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        AuthFlow(viewModel = viewModel)
        return
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    
    // Bottom Sheet Detail states
    val activeScholDetail by viewModel.activeDetailScholarship.collectAsStateWithLifecycle()
    val activeCourseDetail by viewModel.activeDetailCourse.collectAsStateWithLifecycle()

    // Dialog state controllers
    val showAddScholarship by viewModel.showAddScholarshipDialog.collectAsStateWithLifecycle()
    val showAddCourse by viewModel.showAddCourseDialog.collectAsStateWithLifecycle()

    // Screen Intercept Back Handlers for Detail Screens
    BackHandler(enabled = activeScholDetail != null || activeCourseDetail != null) {
        if (activeScholDetail != null) viewModel.activeDetailScholarship.value = null
        if (activeCourseDetail != null) viewModel.activeDetailCourse.value = null
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!isWideScreen) {
                    EduSearchBottomBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { viewModel.currentScreen.value = it }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    EduSearchNavigationRail(
                        currentScreen = currentScreen,
                        onScreenSelected = { viewModel.currentScreen.value = it }
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Main screen view content with slide transitions
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "ScreenTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppScreen.SAVED -> SavedScreen(viewModel = viewModel)
                            AppScreen.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                            AppScreen.NEWS -> NewsScreen(viewModel = viewModel)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // --- Detail Overlay Windows ---
        activeScholDetail?.let { scholarship ->
            ScholarshipDetailOverlay(
                scholarship = scholarship,
                onClose = { viewModel.activeDetailScholarship.value = null },
                onToggleFavorite = { viewModel.toggleFavoriteScholarship(scholarship) },
                onDelete = {
                    viewModel.deleteCustomScholarship(scholarship.id)
                }
            )
        }

        activeCourseDetail?.let { course ->
            CourseDetailOverlay(
                course = course,
                onClose = { viewModel.activeDetailCourse.value = null },
                onToggleFavorite = { viewModel.toggleFavoriteCourse(course) },
                onDelete = {
                    viewModel.deleteCustomCourse(course.id)
                }
            )
        }

        // --- Dialog Add Listings Forms ---
        if (showAddScholarship) {
            AddScholarshipDialog(
                onDismiss = { viewModel.showAddScholarshipDialog.value = false },
                onSave = { title, provider, benefits, description, deadline, category, reqs, link ->
                    viewModel.addNewScholarship(title, provider, benefits, description, deadline, category, reqs, link)
                }
            )
        }

        if (showAddCourse) {
            AddCourseDialog(
                onDismiss = { viewModel.showAddCourseDialog.value = false },
                onSave = { title, instr, plat, price, rating, category, desc, link ->
                    viewModel.addNewCourse(title, instr, plat, price, rating, category, desc, link)
                }
            )
        }
    }
}

// --- Responsive Navigation Components ---

@Composable
fun EduSearchBottomBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.shadow(1.dp),
        tonalElevation = 1.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onScreenSelected(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.School, contentDescription = "Beranda") },
            label = { Text("Eksplor", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_eks_button")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SAVED,
            onClick = { onScreenSelected(AppScreen.SAVED) },
            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Tersimpan") },
            label = { Text("Simpan", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_saved_button")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ASSISTANT,
            onClick = { onScreenSelected(AppScreen.ASSISTANT) },
            icon = { Icon(Icons.Default.Chat, contentDescription = "Asisten AI") },
            label = { Text("AI Asisten", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_ai_button")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.NEWS,
            onClick = { onScreenSelected(AppScreen.NEWS) },
            icon = { Icon(Icons.Default.Newspaper, contentDescription = "Berita") },
            label = { Text("Berita", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onScreenSelected(AppScreen.PROFILE) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_set_button")
        )
    }
}

@Composable
fun EduSearchNavigationRail(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationRail(
        header = {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(vertical = 8.dp)
            )
        },
        modifier = Modifier.fillMaxHeight()
    ) {
        NavigationRailItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onScreenSelected(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.School, contentDescription = null) },
            label = { Text("Eksplor") }
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.SAVED,
            onClick = { onScreenSelected(AppScreen.SAVED) },
            icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
            label = { Text("Tersimpan") }
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.ASSISTANT,
            onClick = { onScreenSelected(AppScreen.ASSISTANT) },
            icon = { Icon(Icons.Default.Chat, contentDescription = null) },
            label = { Text("Asisten AI") }
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onScreenSelected(AppScreen.PROFILE) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Profil") }
        )
    }
}

// --- 1. Dashboard Screen ---

@Composable
fun DashboardScreen(viewModel: ScholarSpaceViewModel) {
    val currentTab by viewModel.currentItemTab.collectAsStateWithLifecycle()
    val scholarships by viewModel.scholarships.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeScholCategory by viewModel.selectedScholarshipCategory.collectAsStateWithLifecycle()
    val activeCourseCategory by viewModel.selectedCourseCategory.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Header — search bar terintegrasi di dalam gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val firstName = currentUser?.fullName?.trim()?.split(" ")?.firstOrNull()
                        if (firstName != null) {
                            Text("Halo, $firstName", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(0.82f))
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text("ScholarSpace", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Temukan beasiswa & kursus terbaik.", fontSize = 12.sp, color = Color.White.copy(0.72f))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.18f))
                            .border(2.dp, Color.White.copy(0.4f), CircleShape)
                            .clickable { viewModel.currentScreen.value = AppScreen.PROFILE },
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUri = profilePhotoUri
                        if (photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(if (photoUri.startsWith("/")) File(photoUri.substringBefore("?v=")) else Uri.parse(photoUri))
                                    .memoryCacheKey(photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = currentUser?.fullName?.trim()?.split(" ")
                                    ?.take(2)?.joinToString("") { it.take(1).uppercase() } ?: "?",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                TextField(
                    value = query,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier.fillMaxWidth().testTag("dashboard_search_input"),
                    placeholder = { Text("Cari beasiswa, kursus, platform...", fontSize = 14.sp, color = Color.White.copy(0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(0.75f), modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White.copy(0.75f), modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(0.22f),
                        unfocusedContainerColor = Color.White.copy(0.17f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(0.6f),
                        unfocusedPlaceholderColor = Color.White.copy(0.6f),
                        focusedLeadingIconColor = Color.White.copy(0.75f),
                        unfocusedLeadingIconColor = Color.White.copy(0.75f),
                        focusedTrailingIconColor = Color.White.copy(0.75f),
                        unfocusedTrailingIconColor = Color.White.copy(0.75f)
                    )
                )
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickStatChip("${scholarships.size}", "Beasiswa", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            QuickStatChip("${courses.size}", "Kursus", Color(0xFF7B1FA2), Modifier.weight(1f))
            QuickStatChip("${scholarships.count { it.status == "Buka" }}", "Terbuka", ColorOpen, Modifier.weight(1f))
        }

        // Type Segment Tab Switcher (Beasiswa / Kursus)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.currentItemTab.value = ItemTab.SCHOLARSHIPS },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentTab == ItemTab.SCHOLARSHIPS) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (currentTab == ItemTab.SCHOLARSHIPS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_scholarship"),
                elevation = null
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Beasiswa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.currentItemTab.value = ItemTab.COURSES },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentTab == ItemTab.COURSES) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (currentTab == ItemTab.COURSES) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_course"),
                elevation = null
            ) {
                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kursus", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Category Filter Chips & Add Listing trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kategori:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 8.dp)
            )

            // Dynamic Categories Flow Cards based on current active Segment
            Box(modifier = Modifier.weight(1f)) {
                if (currentTab == ItemTab.SCHOLARSHIPS) {
                    val scholarlyCats = listOf("Semua", "Dalam Negeri", "Luar Negeri", "Pemerintah", "Swasta")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        scholarlyCats.forEach { cat ->
                            CategoryFilterChip(
                                label = cat,
                                isSelected = activeScholCategory == cat,
                                onClick = { viewModel.selectedScholarshipCategory.value = cat }
                            )
                        }
                    }
                } else {
                    val courseCats = listOf("Semua", "Teknologi", "Desain", "Bisnis", "Bahasa")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        courseCats.forEach { cat ->
                            CategoryFilterChip(
                                label = cat,
                                isSelected = activeCourseCategory == cat,
                                onClick = { viewModel.selectedCourseCategory.value = cat }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Plus Manual Input FAB-Style Trigger
            FilledIconButton(
                onClick = {
                    if (currentTab == ItemTab.SCHOLARSHIPS) {
                        viewModel.showAddScholarshipDialog.value = true
                    } else {
                        viewModel.showAddCourseDialog.value = true
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .shadow(2.dp, CircleShape)
                    .testTag("add_item_fab"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Manual", modifier = Modifier.size(20.dp))
            }
        }

        // Dynamic Main List View (Room Reactive state Flow)
        if (currentTab == ItemTab.SCHOLARSHIPS) {
            if (scholarships.isEmpty()) {
                EmptyStateView(
                    message = "Beasiswa tidak ditemukan",
                    sub = "Coba ubah filter kategori atau kata kunci cari Anda."
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scholarships, key = { it.id }) { item ->
                        ScholarshipListItemCard(
                            scholarship = item,
                            onClick = { viewModel.activeDetailScholarship.value = item },
                            onToggleFavorite = { viewModel.toggleFavoriteScholarship(item) }
                        )
                    }
                }
            }
        } else {
            if (courses.isEmpty()) {
                EmptyStateView(
                    message = "Kursus tidak ditemukan",
                    sub = "Coba ubah filter kategori atau kata pencarian Anda."
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses, key = { it.id }) { item ->
                        CourseListItemCard(
                            course = item,
                            onClick = { viewModel.activeDetailCourse.value = item },
                            onToggleFavorite = { viewModel.toggleFavoriteCourse(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalSearchSection(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(2.dp, RoundedCornerShape(28.dp))
            .testTag("dashboard_search_input"),
        placeholder = { Text("Cari beasiswa, kursus, teknologi...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp), // rounded-full
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// --- 2. Saved (Favorites) View Screen ---

@Composable
fun SavedScreen(viewModel: ScholarSpaceViewModel) {
    var favoriteTab by remember { mutableStateOf(ItemTab.SCHOLARSHIPS) }
    val savedScholarships by viewModel.savedScholarships.collectAsStateWithLifecycle()
    val savedCourses by viewModel.savedCourses.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradient Header - Saved Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Materi Tersimpan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Beasiswa dan kursus yang kamu simpan ada di sini.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        // Saved list segment control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (favoriteTab == ItemTab.SCHOLARSHIPS) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { favoriteTab = ItemTab.SCHOLARSHIPS }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Beasiswa (${savedScholarships.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (favoriteTab == ItemTab.SCHOLARSHIPS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (favoriteTab == ItemTab.COURSES) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { favoriteTab = ItemTab.COURSES }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Kursus (${savedCourses.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (favoriteTab == ItemTab.COURSES) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (favoriteTab == ItemTab.SCHOLARSHIPS) {
            if (savedScholarships.isEmpty()) {
                EmptyStateView(
                    message = "Belum ada beasiswa tersimpan",
                    sub = "Bookmark beasiswa favorit Anda dari halaman Eksplor untuk membiasakan membacanya secara offline."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedScholarships) { item ->
                        ScholarshipListItemCard(
                            scholarship = item,
                            onClick = { viewModel.activeDetailScholarship.value = item },
                            onToggleFavorite = { viewModel.toggleFavoriteScholarship(item) }
                        )
                    }
                }
            }
        } else {
            if (savedCourses.isEmpty()) {
                EmptyStateView(
                    message = "Belum ada kursus tersimpan",
                    sub = "Bookmark kursus pelatihan favorit Anda untuk meluaskan wawasan kapan saja."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedCourses) { item ->
                        CourseListItemCard(
                            course = item,
                            onClick = { viewModel.activeDetailCourse.value = item },
                            onToggleFavorite = { viewModel.toggleFavoriteCourse(item) }
                        )
                    }
                }
            }
        }
    }
}

// --- 3. Assistant (Gemini AI Companion) Screen ---

@Composable
fun AssistantScreen(viewModel: ScholarSpaceViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.aiIsLoading.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Auto scroll down on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    // Scroll to bottom when keyboard opens
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0 && messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ScholarSpace AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tanyakan beasiswa, kursus, atau saran studi",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.testTag("btn_clear_chat")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Hapus Riwayat Chat", modifier = Modifier.size(22.dp))
                }
            }
        }

        // Chat message bubbles stream list
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ScholarSpace AI sedang berpikir...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Bottom dialog ask/send messaging bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("ai_chat_input"),
                    placeholder = { Text("Tanyakan sesuatu ke ScholarSpace AI...", fontSize = 13.sp) },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                FilledIconButton(
                    onClick = {
                        val messageToSend = textInput
                        textInput = ""
                        viewModel.sendChatMessage(messageToSend)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_send_button"),
                    enabled = textInput.isNotBlank() && !isLoading,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Kirim",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == MessageSender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Android,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- 4. Settings & Student Bio Screen ---

@Composable
fun SettingsScreen(viewModel: ScholarSpaceViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient Header - Settings Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Profil & Pengaturan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Identitas pengembang dan konfigurasi tema aplikasi.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        // Student Bio Card with Gradient Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            // Gradient card header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RF",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Ryan Firmansyah",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "NIM. H071241082",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Universitas Hasanuddin (Unhas)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Stats section
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Angkatan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "2024 (Aktif 2026)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Tugas / Program",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Final Lab Mobile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Appearance Adjustments
        Text(
            text = "PREFERENSI TAMPILAN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Mode Gelap", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Turunkan stres mata saat malam hari.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        }

        // SQLite Local caching explanation card for exam evaluation
        Text(
            text = "EVALUASI DATABASE (ROOM & SQLITE)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .padding(bottom = 60.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Informasi Caching SQLite",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Aplikasi ScholarSpace mengintegrasikan pustaka Room Database di atas mesin relasional SQLite asli Android.\n\n" +
                            "💡 Penjelasan Alur:\n" +
                            "- Sesi Beranda memuat beasiswa & kursus berstandar tinggi.\n" +
                            "- Mengetuk tombol Bookmark (Favorit) akan memasukkan model kelas yang diserialisasi ke dalam database lokal.\n" +
                            "- Menambahkan materi secara manual melalui tombol (+) akan memasukkan data baru ke database dengan metadata 'isCustom = true'.\n" +
                            "- Saat gawai offline, daftar Favorit dan materi buatan mandiri tetap termuat secara utuh dari kueri SQLite Room Flow.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// --- Helper functions for category/platform colors ---

fun scholarshipCategoryColor(category: String): Color = when (category.lowercase()) {
    "pemerintah" -> Color(0xFF1565C0)
    "luar negeri" -> Color(0xFF7B1FA2)
    "dalam negeri" -> Color(0xFF2E7D32)
    "swasta" -> Color(0xFFE64A19)
    else -> Color(0xFF546E7A)
}

fun scholarshipCategoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "pemerintah" -> Icons.Default.AccountBalance
    "luar negeri" -> Icons.Default.Flight
    "dalam negeri" -> Icons.Default.HomeWork
    "swasta" -> Icons.Default.Business
    else -> Icons.Default.School
}

fun coursePlatformColor(platform: String): Color = when (platform.lowercase()) {
    "dicoding" -> Color(0xFF00897B)
    "udemy" -> Color(0xFF7C3AED)
    "coursera" -> Color(0xFF1D4ED8)
    else -> Color(0xFF546E7A)
}

// --- Quick Stat Chip ---

@Composable
fun QuickStatChip(count: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(0.85f), fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Card Item Lists Layouts ---

@Composable
fun ScholarshipListItemCard(
    scholarship: Scholarship,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val catColor = scholarshipCategoryColor(scholarship.category)
    val catIcon = scholarshipCategoryIcon(scholarship.category)
    val statusColor = if (scholarship.status == "Buka") ColorOpen else ColorClosed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column {
            // Colored category header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(catColor.copy(0.12f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(26.dp).background(catColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(catIcon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(scholarship.category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = catColor)
                        if (scholarship.isCustom) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text("Buatan Anda", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(0.15f),
                        contentColor = statusColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(5.dp).background(statusColor, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(scholarship.status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Main content
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scholarship.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(scholarship.provider, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f), modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(scholarship.deadline, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
                    }
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("fav_schol_${scholarship.id}")) {
                    Icon(
                        if (scholarship.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        "Simpan",
                        tint = if (scholarship.isFavorite) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CourseListItemCard(
    course: Course,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val platformColor = coursePlatformColor(course.platform)
    val isFree = course.price.lowercase() == "gratis" || course.price.lowercase().contains("audit")
    val priceColor = if (isFree) ColorOpen else MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column {
            // Colored platform header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(platformColor.copy(0.12f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(26.dp).background(platformColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(course.platform, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = platformColor)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = platformColor.copy(0.15f),
                            contentColor = platformColor
                        ) {
                            Text(course.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = priceColor.copy(0.15f),
                        contentColor = priceColor
                    ) {
                        Text(course.price, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            // Main content
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        course.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(course.instructor, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                if (i < course.rating.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                                null, tint = GoldStar, modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("${course.rating}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(0.65f))
                    }
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("fav_course_${course.id}")) {
                    Icon(
                        if (course.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        "Simpan",
                        tint = if (course.isFavorite) platformColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, sub: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = sub,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// --- 5. Detail HUD overlay windows (Scholarships & Courses) ---

@Composable
fun ScholarshipDetailOverlay(
    scholarship: Scholarship,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    // Material 3 Dialog styled as a beautiful details sheet
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        scholarship.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (scholarship.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Simpan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scholarship Title
                Text(
                    text = scholarship.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Sponsor
                Text(
                    text = "Penyedia: ${scholarship.provider}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "DESKRIPSI BEASISWA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scholarship.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Requirements
                Text(
                    text = "PERSYARATAN UTAMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scholarship.requirements,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Benefits
                Text(
                    text = "MANFAAT & CAKUPAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scholarship.benefits,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Batas: ${scholarship.deadline}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scholarship.link))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Buka Situs resmi", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                // If added by user, show a delete button
                if (scholarship.isCustom) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onClose()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Lowongan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailOverlay(
    course: Course,
    onClose: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        course.platform + " • " + course.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (course.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Simpan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Course Title
                Text(
                    text = course.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Instructor & Platform
                Text(
                    text = "Dipublikasikan oleh: ${course.instructor}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "DESKRIPSI KURSUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Price & Evaluation metrics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BIAYA KURSUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                        Text(course.price, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("RATING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${course.rating} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "URL tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kunjungi URL Kelas")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                // If added by user, show a delete button
                if (course.isCustom) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onClose()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Kursus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 6. Forms Modal Adding (Scholarship & Course Dialogs) ---

@Composable
fun AddScholarshipDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var benefits by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dalam Negeri") }

    val categories = listOf("Dalam Negeri", "Luar Negeri", "Pemerintah", "Swasta")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Tambah Informasi Beasiswa 🎓",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Beasiswa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_schol_title")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Instansi / Penyedia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Batas Pendaftaran (e.g., 12 Des 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori Beasiswa:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { category = cat }
                        ) {
                            RadioButton(
                                selected = category == cat,
                                onClick = { category = cat }
                            )
                            Text(cat, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text("Kriteria / Syarat Utama") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = benefits,
                    onValueChange = { benefits = it },
                    label = { Text("Manfaat / Cakupan Dana") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("URL Link Pendaftaran") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, provider, benefits, description, deadline, category, requirements, if (link.isBlank()) "https://google.com" else link)
                            }
                        },
                        enabled = title.isNotBlank() && provider.isNotBlank(),
                        modifier = Modifier.testTag("save_schol_btn")
                    ) {
                        Text("Simpan Beasiswa")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var ratingString by remember { mutableStateOf("4.8") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Teknologi") }

    val categories = listOf("Teknologi", "Desain", "Bisnis", "Bahasa")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Tambah Informasi Kursus 📚",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Kursus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_course_title")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Nama Pengajar / Instansi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = { platform = it },
                        label = { Text("Platform (e.g. Udemy)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Biaya / Harga") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ratingString,
                        onValueChange = { ratingString = it },
                        label = { Text("Rating (1.0 - 5.0)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori Kursus:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { category = cat }
                        ) {
                            RadioButton(
                                selected = category == cat,
                                onClick = { category = cat }
                            )
                            Text(cat, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Modul / Materi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("URL Link Pembelian / Akses") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val ratingDouble = ratingString.toDoubleOrNull() ?: 4.8
                                onSave(title, instructor, platform, if (price.isBlank()) "Gratis" else price, ratingDouble, category, description, if (link.isBlank()) "https://google.com" else link)
                            }
                        },
                        enabled = title.isNotBlank() && instructor.isNotBlank() && platform.isNotBlank(),
                        modifier = Modifier.testTag("save_course_btn")
                    ) {
                        Text("Simpan Kursus")
                    }
                }
            }
        }
    }
}

// ===================== AUTH FLOW =====================

@Composable
fun AuthFlow(viewModel: ScholarSpaceViewModel) {
    var showRegister by remember { mutableStateOf(false) }
    if (showRegister) {
        RegisterScreen(viewModel = viewModel, onGoLogin = { showRegister = false })
    } else {
        LoginScreen(viewModel = viewModel, onGoRegister = { showRegister = true })
    }
}

@Composable
fun LoginScreen(viewModel: ScholarSpaceViewModel, onGoRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF4F46E5)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("ScholarSpace", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("Masuk ke akun Anda", fontSize = 13.sp, color = Color.White.copy(alpha = 0.72f))
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; errorMsg = "" },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(20.dp))
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg, color = Color(0xFFDC2626), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) { errorMsg = "Isi semua kolom"; return@Button }
                            isLoading = true
                            viewModel.login(email, password, onSuccess = { isLoading = false }, onError = { isLoading = false; errorMsg = it })
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                        Text("Belum punya akun? Daftar sekarang", fontSize = 13.sp, color = Color(0xFF4F46E5))
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: ScholarSpaceViewModel, onGoLogin: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF4F46E5))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Buat Akun Baru", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("Daftar untuk mulai menggunakan ScholarSpace", fontSize = 13.sp, color = Color.White.copy(alpha = 0.72f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    @Composable fun Field(value: String, onChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text) {
                        OutlinedTextField(value = value, onValueChange = { onChange(it); errorMsg = "" },
                            label = { Text(label) }, leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(10.dp))
                    }
                    Field(fullName, { fullName = it }, "Nama Lengkap", Icons.Default.Person)
                    Field(email, { email = it }, "Email", Icons.Default.Email, KeyboardType.Email)
                    Field(username, { username = it }, "Username", Icons.Default.AccountCircle)
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(20.dp)) } },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; errorMsg = "" },
                        label = { Text("Konfirmasi Password") }, leadingIcon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Field(university, { university = it }, "Universitas", Icons.Default.School)
                    Field(major, { major = it }, "Jurusan / Program Studi", Icons.Default.MenuBook)
                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank() -> errorMsg = "Isi semua kolom wajib"
                                password != confirmPassword -> errorMsg = "Password tidak cocok"
                                password.length < 6 -> errorMsg = "Password minimal 6 karakter"
                                else -> {
                                    isLoading = true
                                    viewModel.register(fullName, email, username, password, university, major,
                                        onSuccess = { isLoading = false },
                                        onError = { isLoading = false; errorMsg = it })
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Daftar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                        Text("Sudah punya akun? Masuk", fontSize = 13.sp, color = Color(0xFF4F46E5))
                    }
                }
            }
        }
    }
}

// ===================== PROFILE SCREEN =====================

@Composable
fun ProfileScreen(viewModel: ScholarSpaceViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val savedScholarships by viewModel.savedScholarships.collectAsStateWithLifecycle()
    val savedCourses by viewModel.savedCourses.collectAsStateWithLifecycle()
    val scholarships by viewModel.scholarships.collectAsStateWithLifecycle()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editUniversity by remember { mutableStateOf("") }
    var editMajor by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column {
                Text("Profil Saya", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Informasi akun dan statistik penggunaan", fontSize = 11.sp, color = Color.White.copy(0.75f))
            }
        }

        // Avatar + Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(80.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.2f))
                                .border(2.dp, Color.White.copy(0.5f), CircleShape)
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUri = profilePhotoUri
                            if (photoUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(if (photoUri.startsWith("/")) File(photoUri.substringBefore("?v=")) else Uri.parse(photoUri))
                                        .memoryCacheKey(photoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Profil",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = user?.fullName?.trim()?.split(" ")
                                        ?.take(2)?.joinToString("") { it.take(1).uppercase() }
                                        ?: "?",
                                    fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.White
                                )
                            }
                        }
                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Ganti Foto",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(user?.fullName ?: "-", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("@${user?.username ?: "-"}", fontSize = 13.sp, color = Color.White.copy(0.85f))
                        Text(user?.email ?: "-", fontSize = 12.sp, color = Color.White.copy(0.7f))
                    }
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                if (!isEditing) {
                    ProfileInfoRow(Icons.Default.School, "Universitas", user?.university?.ifBlank { "-" } ?: "-")
                    ProfileInfoRow(Icons.Default.MenuBook, "Jurusan", user?.major?.ifBlank { "-" } ?: "-")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = {
                        editName = user?.fullName ?: ""
                        editUniversity = user?.university ?: ""
                        editMajor = user?.major ?: ""
                        isEditing = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Profil")
                    }
                } else {
                    OutlinedTextField(editName, { editName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(editUniversity, { editUniversity = it }, label = { Text("Universitas") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(editMajor, { editMajor = it }, label = { Text("Jurusan") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Batal") }
                        Button(onClick = { viewModel.updateProfile(editName, editUniversity, editMajor); isEditing = false }, modifier = Modifier.weight(1f)) { Text("Simpan") }
                    }
                }
            }
        }

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Statistik Penggunaan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(count = savedScholarships.size.toString(), label = "Beasiswa\nDisimpan", color = MaterialTheme.colorScheme.primary)
                    StatItem(count = savedCourses.size.toString(), label = "Kursus\nDisimpan", color = MaterialTheme.colorScheme.secondary)
                    StatItem(count = scholarships.size.toString(), label = "Total\nBeasiswa", color = Color(0xFF388E3C))
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Cache Database SQLite", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("${savedScholarships.size + savedCourses.size} item tersimpan lokal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Pengaturan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Mode Gelap", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(if (isDarkMode) "Aktif" else "Nonaktif", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode() })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Keluar / Logout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}
