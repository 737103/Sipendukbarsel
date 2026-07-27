package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.UserAccount
import com.example.data.model.Warga
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

// Routes
object Screen {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val USER_DASHBOARD = "user_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
}

@Composable
fun MainAppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val dest = if (currentUser?.isAdmin == true) Screen.ADMIN_DASHBOARD else Screen.USER_DASHBOARD
            navController.navigate(dest) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.LOGIN,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.LOGIN) {
            LoginScreen(viewModel = viewModel, onNavigateToRegister = {
                navController.navigate(Screen.REGISTER)
            })
        }
        composable(Screen.REGISTER) {
            RegisterScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable(Screen.USER_DASHBOARD) {
            UserDashboardScreen(viewModel = viewModel)
        }
        composable(Screen.ADMIN_DASHBOARD) {
            AdminDashboardScreen(viewModel = viewModel)
        }
    }
}

// ----------------------------------------------------
// LOGIN SCREEN
// ----------------------------------------------------
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.clearLoginError()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual Banner / Logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Kelurahan Bara Baraya Selatan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SISTEM KEPENDUDUKAN",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Bara Baraya Selatan",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Silakan Masuk",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Masukkan kredensial Anda untuk mengakses portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = "Toggle Password Visibility")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp)
            )

            // Login error message
            if (loginError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = loginError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Login, contentDescription = "Masuk")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Masuk ke Sistem", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Belum punya akun?",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Daftar Akun Baru",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Copyright BarselApps 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// ----------------------------------------------------
// REGISTER SCREEN
// ----------------------------------------------------
@Composable
fun RegisterScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rt by remember { mutableStateOf("") }
    var rw by remember { mutableStateOf("") }

    val registerError by viewModel.registerError.collectAsStateWithLifecycle()
    val registerSuccess by viewModel.registerSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.clearRegisterError()
    }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            Toast.makeText(context, "Pendaftaran berhasil! Tunggu verifikasi admin.", Toast.LENGTH_LONG).show()
            viewModel.clearRegisterError()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "Daftar Akun",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = "Registration Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
            )

            Text(
                text = "Registrasi Anggota",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kirim permohonan akses pendaftaran kependudukan kepada admin kelurahan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap *") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Name Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rt,
                    onValueChange = { if (it.length <= 3) rt = it.filter { char -> char.isDigit() } },
                    label = { Text("RT (3 Digit) *") },
                    placeholder = { Text("Contoh: 001") },
                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = "RT Icon") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = rw,
                    onValueChange = { if (it.length <= 3) rw = it.filter { char -> char.isDigit() } },
                    label = { Text("RW (3 Digit) *") },
                    placeholder = { Text("Contoh: 002") },
                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = "RW Icon") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = "Toggle Password Visibility")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp)
            )

            if (registerError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = registerError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.register(username, password, fullName, rt, rw) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Kirim")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daftar & Verifikasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// USER DASHBOARD SCREEN
// ----------------------------------------------------
@Composable
fun UserDashboardScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val tabs = listOf(
        TabItem("Input Data", Icons.Default.AddBox),
        TabItem("Database", Icons.Default.Folder),
        TabItem("Statistik", Icons.Default.BarChart),
        TabItem("Ekspor/Impor", Icons.Default.SettingsBackupRestore)
    )

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "Portal Warga BBS",
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = currentUser?.fullName ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Petugas Warga",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log Out")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> UserInputWargaTab(viewModel = viewModel)
                1 -> UserDatabaseWargaTab(viewModel = viewModel)
                2 -> UserStatistikTab(viewModel = viewModel)
                3 -> BackupRestoreTab(viewModel = viewModel, isAdmin = false)
            }
        }
    }
}

// ----------------------------------------------------
// USER TAB 0: INPUT WARGA
// ----------------------------------------------------
@Composable
fun UserInputWargaTab(viewModel: AppViewModel) {
    var nik by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var noKk by remember { mutableStateOf("") }
    var tempatLahir by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") } // Format: YYYY-MM-DD
    var jenisKelamin by remember { mutableStateOf("Laki-laki") }
    var alamat by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var rt by remember(currentUser) { mutableStateOf(currentUser?.rt ?: "") }
    var rw by remember(currentUser) { mutableStateOf(currentUser?.rw ?: "") }
    var hubKeluarga by remember { mutableStateOf("Kepala Keluarga") }
    var agama by remember { mutableStateOf("Islam") }
    var statusKawin by remember { mutableStateOf("Belum Kawin") }
    var pendidikan by remember { mutableStateOf("Tidak Sekolah") }
    var pekerjaan by remember { mutableStateOf("Belum Bekerja") }
    var pekerjaanDetail by remember { mutableStateOf("") }
    var golDarah by remember { mutableStateOf("O") }
    var noHp by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("Penduduk Tetap") }
    var jumlahAnggotaKeluargaText by remember { mutableStateOf("0") }
    val familyMembers = remember { mutableStateListOf<FamilyMemberInputState>() }

    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    val allWargaList by viewModel.allWarga.collectAsStateWithLifecycle()

    val inputError by viewModel.inputError.collectAsStateWithLifecycle()
    val inputSuccess by viewModel.inputSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(inputSuccess) {
        if (inputSuccess) {
            Toast.makeText(context, "Data kependudukan berhasil disimpan ke database!", Toast.LENGTH_SHORT).show()
            // Reset fields
            nik = ""
            nama = ""
            noKk = ""
            tempatLahir = ""
            tanggalLahir = ""
            alamat = ""
            rt = currentUser?.rt ?: ""
            rw = currentUser?.rw ?: ""
            hubKeluarga = "Kepala Keluarga"
            agama = "Islam"
            statusKawin = "Belum Kawin"
            pendidikan = "Tidak Sekolah"
            pekerjaan = "Belum Bekerja"
            pekerjaanDetail = ""
            golDarah = "O"
            noHp = ""
            keterangan = "Penduduk Tetap"
            jumlahAnggotaKeluargaText = "0"
            familyMembers.clear()
            hasAttemptedSubmit = false
            viewModel.clearInputSuccess()
            viewModel.clearInputError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pendataan Warga Baru",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Kelurahan Bara Baraya Selatan. NIK wajib unik seluruh sistem.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val isMainNikDuplicateDb = allWargaList.any { it.nik == nik.trim() }
        val isMainNikDuplicateForm = familyMembers.any { it.nik.trim() == nik.trim() && it.nik.isNotBlank() }
        val isMainNikInvalidFormat = nik.trim().length != 16 || !nik.trim().all { it.isDigit() }
        val isMainNikError = isMainNikDuplicateDb || isMainNikDuplicateForm || (hasAttemptedSubmit && isMainNikInvalidFormat)

        OutlinedTextField(
            value = nik,
            onValueChange = { if (it.length <= 16) nik = it.filter { char -> char.isDigit() } },
            label = { Text("NIK Kepala/Utama (16 Digit) *") },
            leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = "NIK") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isMainNikError,
            supportingText = {
                if (isMainNikDuplicateDb) {
                    Text("NIK sudah terdaftar di database", color = MaterialTheme.colorScheme.error)
                } else if (isMainNikDuplicateForm) {
                    Text("NIK duplikat di dalam form", color = MaterialTheme.colorScheme.error)
                } else if (hasAttemptedSubmit && isMainNikInvalidFormat) {
                    Text("NIK wajib 16 digit angka", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isNamaError = hasAttemptedSubmit && nama.trim().isBlank()

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nama") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isNamaError,
            supportingText = {
                if (isNamaError) {
                    Text("Nama lengkap wajib diisi", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isNoKkInvalid = noKk.trim().length != 16 || !noKk.trim().all { it.isDigit() }
        val isNoKkError = hasAttemptedSubmit && isNoKkInvalid

        OutlinedTextField(
            value = noKk,
            onValueChange = { if (it.length <= 16) noKk = it.filter { char -> char.isDigit() } },
            label = { Text("No. Kartu Keluarga (KK) (16 Digit) *") },
            leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = "No KK") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isNoKkError,
            supportingText = {
                if (isNoKkError) {
                    Text("No. KK wajib 16 digit angka", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isTempatLahirError = hasAttemptedSubmit && tempatLahir.trim().isBlank()
        val isTanggalLahirInvalid = tanggalLahir.trim().isBlank() || !tanggalLahir.trim().matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
        val isTanggalLahirError = hasAttemptedSubmit && isTanggalLahirInvalid

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = tempatLahir,
                onValueChange = { tempatLahir = it },
                label = { Text("Tempat Lahir *") },
                modifier = Modifier
                    .weight(1.2f)
                    .padding(end = 4.dp),
                singleLine = true,
                isError = isTempatLahirError,
                supportingText = {
                    if (isTempatLahirError) {
                        Text("Wajib diisi", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = tanggalLahir,
                onValueChange = { tanggalLahir = it },
                label = { Text("Tgl Lahir (YYYY-MM-DD) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .weight(1.8f)
                    .padding(start = 4.dp),
                singleLine = true,
                placeholder = { Text("e.g. 1995-12-30") },
                isError = isTanggalLahirError,
                supportingText = {
                    if (isTanggalLahirError) {
                        Text("Format YYYY-MM-DD wajib", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Jenis Kelamin Selector
        Text(
            text = "Jenis Kelamin *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { jenisKelamin = "Laki-laki" }
            ) {
                RadioButton(selected = jenisKelamin == "Laki-laki", onClick = { jenisKelamin = "Laki-laki" })
                Text("Laki-laki", style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { jenisKelamin = "Perempuan" }
            ) {
                RadioButton(selected = jenisKelamin == "Perempuan", onClick = { jenisKelamin = "Perempuan" })
                Text("Perempuan", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val isAlamatError = hasAttemptedSubmit && alamat.trim().isBlank()

        OutlinedTextField(
            value = alamat,
            onValueChange = { alamat = it },
            label = { Text("Alamat Tempat Tinggal *") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Alamat") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            isError = isAlamatError,
            supportingText = {
                if (isAlamatError) {
                    Text("Alamat tempat tinggal wajib diisi", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isRtError = hasAttemptedSubmit && (rt.trim().length != 3 || !rt.trim().all { it.isDigit() })
        val isRwError = hasAttemptedSubmit && (rw.trim().length != 3 || !rw.trim().all { it.isDigit() })

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rt,
                onValueChange = { if (it.length <= 3) rt = it.filter { char -> char.isDigit() } },
                label = { Text("RT (3 Digit) *") },
                placeholder = { Text("e.g. 001") },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = "RT") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                singleLine = true,
                isError = isRtError,
                supportingText = {
                    if (isRtError) {
                        Text("RT wajib 3 digit angka", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = rw,
                onValueChange = { if (it.length <= 3) rw = it.filter { char -> char.isDigit() } },
                label = { Text("RW (3 Digit) *") },
                placeholder = { Text("e.g. 002") },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = "RW") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                singleLine = true,
                isError = isRwError,
                supportingText = {
                    if (isRwError) {
                        Text("RW wajib 3 digit angka", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdowns & Selectors
        Row(modifier = Modifier.fillMaxWidth()) {
            // Hubungan Keluarga Dropdown
            var hubExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                OutlinedTextField(
                    value = hubKeluarga,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hubungan Kel. *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = hubExpanded, onDismissRequest = { hubExpanded = false }) {
                    val daftarHub = listOf("Kepala Keluarga", "Isteri", "Anak", "Cucu", "Orang Tua", "Mertua", "Famili Lain")
                    daftarHub.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                hubKeluarga = option
                                hubExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { hubExpanded = true }
                )
            }

            // Agama Dropdown
            var agamaExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(start = 4.dp)
            ) {
                OutlinedTextField(
                    value = agama,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Agama *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = agamaExpanded, onDismissRequest = { agamaExpanded = false }) {
                    val daftarAgama = listOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu")
                    daftarAgama.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                agama = option
                                agamaExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { agamaExpanded = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Status Kawin Dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                OutlinedTextField(
                    value = statusKawin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Kawin *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    val daftarStatus = listOf("Belum Kawin", "Kawin", "Cerai Hidup", "Cerai Mati")
                    daftarStatus.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                statusKawin = option
                                statusExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { statusExpanded = true }
                )
            }

            // Pendidikan Terakhir Dropdown
            var pendExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(start = 4.dp)
            ) {
                OutlinedTextField(
                    value = pendidikan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pendidikan *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = pendExpanded, onDismissRequest = { pendExpanded = false }) {
                    val daftarPend = listOf("Tidak Sekolah", "SD Sederajat", "SMP Sederajat", "SMA / SMK Sederajat", "D2/D3", "D4/S1", "S2", "S3")
                    daftarPend.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                pendidikan = option
                                pendExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { pendExpanded = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Pekerjaan Dropdown
            var pekExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .padding(end = 4.dp)
            ) {
                OutlinedTextField(
                    value = pekerjaan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pekerjaan *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = pekExpanded, onDismissRequest = { pekExpanded = false }) {
                    val daftarPek = listOf(
                        "Belum Bekerja", "Pelajar/Mahasiswa", "Buruh Harian Lepas", "Mengurus Rumah Tangga",
                        "Karyawan Swasta", "PNS", "PPPK", "Pedagang", "Polri/TNI", "Peg. BUMD/BUMN",
                        "Petani", "Nelayan", "Anggota DPRD", "Anggota DPR RI", "Pengacara", "Lainnya"
                    )
                    daftarPek.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                pekerjaan = option
                                pekExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { pekExpanded = true }
                )
            }

            // Gol Darah Selector
            var golExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(start = 4.dp)
            ) {
                OutlinedTextField(
                    value = golDarah,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gol. Darah *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = golExpanded, onDismissRequest = { golExpanded = false }) {
                    val daftarGol = listOf("A", "B", "AB", "O", "-")
                    daftarGol.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                golDarah = option
                                golExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { golExpanded = true }
                )
            }
        }

        if (pekerjaan == "Lainnya") {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = pekerjaanDetail,
                onValueChange = { pekerjaanDetail = it },
                label = { Text("Sebutkan Detail Pekerjaan Lainnya *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // No Telepon/HP (Optional)
        OutlinedTextField(
            value = noHp,
            onValueChange = { noHp = it },
            label = { Text("No. Telepon / HP") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Keterangan Dropdown
        var ketExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = keterangan,
                onValueChange = {},
                readOnly = true,
                label = { Text("Keterangan Kependudukan *") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            DropdownMenu(expanded = ketExpanded, onDismissRequest = { ketExpanded = false }) {
                val daftarKet = listOf("Penduduk Tetap", "Domisili Sementara", "Meninggal", "Pindah Domisili", "Tidak Diketahui Alamatnya")
                daftarKet.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            keterangan = option
                            ketExpanded = false
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { ketExpanded = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // JUMLAH ANGGOTA KELUARGA (DIISI MANUAL, DYNAMIC FORM GENERATION)
        OutlinedTextField(
            value = jumlahAnggotaKeluargaText,
            onValueChange = {
                val num = it.toIntOrNull() ?: 0
                if (num >= 0 && num <= 15) {
                    jumlahAnggotaKeluargaText = it
                    while (familyMembers.size < num) {
                        familyMembers.add(FamilyMemberInputState())
                    }
                    while (familyMembers.size > num) {
                        familyMembers.removeAt(familyMembers.size - 1)
                    }
                } else if (it.isBlank()) {
                    jumlahAnggotaKeluargaText = ""
                    familyMembers.clear()
                }
            },
            label = { Text("Jumlah Anggota Keluarga (Tambahan) *") },
            placeholder = { Text("e.g. 2") },
            leadingIcon = { Icon(Icons.Default.People, contentDescription = "Family Members") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // RENDER DYNAMIC FORMS FOR FAMILY MEMBERS
        familyMembers.forEachIndexed { idx, member ->
            FamilyMemberFormBlock(
                member = member,
                index = idx,
                allWargaList = allWargaList,
                mainNik = nik,
                familyMembers = familyMembers,
                hasAttemptedSubmit = hasAttemptedSubmit
            )
        }

        if (inputError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = inputError ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                hasAttemptedSubmit = true
                val mainWarga = Warga(
                    nik = nik.trim(),
                    nama = nama.trim(),
                    tempatLahir = tempatLahir.trim(),
                    tanggalLahir = tanggalLahir.trim(),
                    jenisKelamin = jenisKelamin,
                    alamat = alamat.trim(),
                    rt = rt.trim(),
                    rw = rw.trim(),
                    agama = agama,
                    statusKawin = statusKawin,
                    pekerjaan = pekerjaan,
                    pekerjaanDetail = pekerjaanDetail.trim(),
                    golDarah = golDarah,
                    noKk = noKk.trim(),
                    hubKeluarga = hubKeluarga,
                    pendidikan = pendidikan,
                    noHp = noHp.trim(),
                    keterangan = keterangan,
                    jumlahAnggotaKeluarga = familyMembers.size,
                    inputtedBy = "" // Filled by ViewModel
                )

                val memberWargas = familyMembers.map { member ->
                    Warga(
                        nik = member.nik.trim(),
                        nama = member.nama.trim(),
                        tempatLahir = member.tempatLahir.trim(),
                        tanggalLahir = member.tanggalLahir.trim(),
                        jenisKelamin = member.jenisKelamin,
                        alamat = alamat.trim(), // Inherited
                        rt = rt.trim(), // Inherited
                        rw = rw.trim(), // Inherited
                        agama = member.agama,
                        statusKawin = member.statusKawin,
                        pekerjaan = member.pekerjaan,
                        pekerjaanDetail = member.pekerjaanDetail.trim(),
                        golDarah = member.golDarah,
                        noKk = noKk.trim(), // Inherited No. KK
                        hubKeluarga = member.hubKeluarga,
                        pendidikan = member.pendidikan,
                        noHp = member.noHp.trim(),
                        keterangan = member.keterangan, // Custom per-member status
                        jumlahAnggotaKeluarga = familyMembers.size, // Inherited
                        inputtedBy = "" // Filled by ViewModel
                    )
                }

                viewModel.inputWarga(mainWarga, memberWargas)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = "Simpan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Keluarga & Warga Baru", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ----------------------------------------------------
// USER TAB 1: DATABASE WARGA YANG TELAH TERINPUT
// ----------------------------------------------------
@Composable
fun UserDatabaseWargaTab(viewModel: AppViewModel) {
    val myWargaList by viewModel.currentUserWarga.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var editTargetWarga by remember { mutableStateOf<Warga?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filteredList = remember(myWargaList, searchQuery) {
        myWargaList.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
                    it.nik.contains(searchQuery) ||
                    it.alamat.contains(searchQuery, ignoreCase = true)
        }
    }

    val exportXlsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.ms-excel")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val xlsString = viewModel.exportWargaToXls(filteredList) // exports current filtered warga list
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(xlsString.toByteArray(charset("UTF-8")))
                    }
                    Toast.makeText(context, "Data berhasil diekspor ke file Excel (.xls)!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ekspor gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (editTargetWarga != null) {
        EditWargaDialog(
            warga = editTargetWarga!!,
            onDismiss = { editTargetWarga = null },
            onConfirm = { updated ->
                viewModel.editWarga(updated)
                editTargetWarga = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Database Warga Anda",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Daftar penduduk Kelurahan Bara Baraya Selatan yang telah Anda input.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Cari NIK, Nama, atau Alamat...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Excel Download row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: ${filteredList.size} warga",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (filteredList.isNotEmpty()) {
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        exportXlsLauncher.launch("Data_Warga_Anda_$timestamp.xls")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Excel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unduh Excel (.xls)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "Empty list",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Hasil pencarian kosong" else "Anda belum menginput data warga",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.nik }) { warga ->
                    WargaCardItem(
                        warga = warga,
                        onEdit = { editTargetWarga = warga },
                        onDelete = { viewModel.deleteWarga(warga) },
                        isAdminMode = false
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// USER TAB 2: STATISTIK DATA WARGA YANG TERINPUT
// ----------------------------------------------------
@Composable
fun UserStatistikTab(viewModel: AppViewModel, isAdminGlobal: Boolean = false) {
    val wargaList by (if (isAdminGlobal) viewModel.allWarga else viewModel.currentUserWarga).collectAsStateWithLifecycle()

    val totalCount = wargaList.size
    val maleCount = wargaList.count { it.jenisKelamin.lowercase() == "laki-laki" }
    val femaleCount = wargaList.count { it.jenisKelamin.lowercase() == "perempuan" }

    // Grouping for RtRw and Agama
    val rtRwStats = wargaList.groupBy { it.rtRw }.mapValues { it.value.size }
    val agamaStats = wargaList.groupBy { it.agama }.mapValues { it.value.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (isAdminGlobal) "Statistik Keseluruhan" else "Statistik Input Anda",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isAdminGlobal) "Analisis data keseluruhan warga Bara Baraya Selatan." else "Analisis kependudukan hasil kontribusi data Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TOTAL WARGA TERDATA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$totalCount", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
                Text("Jiwa", style = MaterialTheme.typography.titleMedium)
            }
        }

        val pendudukTetapCount = wargaList.count { it.keterangan.trim().lowercase() == "penduduk tetap" }
        val domisiliSementaraCount = wargaList.count { it.keterangan.trim().lowercase() == "domisili sementara" }
        val meninggalCount = wargaList.count { it.keterangan.trim().lowercase() == "meninggal" }
        val pindahDomisiliCount = wargaList.count { it.keterangan.trim().lowercase() == "pindah domisili" }
        val tidakDiketahuiAlamatnyaCount = wargaList.count { it.keterangan.trim().lowercase() == "tidak diketahui alamatnya" }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Status Kependudukan (Keterangan)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusStatCard(
                    title = "Penduduk Tetap",
                    count = pendudukTetapCount,
                    icon = Icons.Default.Home,
                    color = Color(0xFF4CAF50), // Green
                    modifier = Modifier.weight(1f)
                )
                StatusStatCard(
                    title = "Domisili Sementara",
                    count = domisiliSementaraCount,
                    icon = Icons.Default.Schedule,
                    color = Color(0xFFFF9800), // Orange
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusStatCard(
                    title = "Meninggal",
                    count = meninggalCount,
                    icon = Icons.Default.Cancel,
                    color = Color(0xFFF44336), // Red
                    modifier = Modifier.weight(1f)
                )
                StatusStatCard(
                    title = "Pindah Domisili",
                    count = pindahDomisiliCount,
                    icon = Icons.Default.ArrowForward,
                    color = Color(0xFF9C27B0), // Purple
                    modifier = Modifier.weight(1f)
                )
            }

            StatusStatCard(
                title = "Tidak Diketahui Alamatnya",
                count = tidakDiketahuiAlamatnyaCount,
                icon = Icons.Default.Help,
                color = Color(0xFF795548), // Brown
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gender Chart Card
        Text("Demografi Jenis Kelamin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Laki-laki: $maleCount jiwa", style = MaterialTheme.typography.bodyMedium)
                    Text("Perempuan: $femaleCount jiwa", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom simple progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.4f))
                ) {
                    val ratio = if (totalCount > 0) maleCount.toFloat() / totalCount.toFloat() else 0.5f
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(ratio.coerceAtLeast(0.01f))
                                .background(Color(0xFF2196F3))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - ratio).coerceAtLeast(0.01f))
                                .background(Color(0xFFE91E63))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF2196F3)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Laki-laki (${if (totalCount > 0) (maleCount * 100 / totalCount) else 0}%)", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perempuan (${if (totalCount > 0) (femaleCount * 100 / totalCount) else 0}%)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RtRw stats
        Text("Distribusi per RT / RW", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (rtRwStats.isEmpty()) {
                    Text("Data RT/RW belum tersedia", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    val maxVal = rtRwStats.values.maxOrNull() ?: 1
                    rtRwStats.forEach { (rtRw, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RT/RW $rtRw",
                                modifier = Modifier.width(100.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                val barRatio = count.toFloat() / maxVal.toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(barRatio)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$count jiwa",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Agama stats
        Text("Distribusi per Agama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (agamaStats.isEmpty()) {
                    Text("Data Agama belum tersedia", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    val maxVal = agamaStats.values.maxOrNull() ?: 1
                    agamaStats.forEach { (agama, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = agama,
                                modifier = Modifier.width(100.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                val barRatio = count.toFloat() / maxVal.toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(barRatio)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.tertiary)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$count jiwa",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatusStatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count jiwa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ----------------------------------------------------
// USER/ADMIN TAB 3: BACKUP & RESTORE DATA (DOWNLOAD / RESTORE)
// ----------------------------------------------------
@Composable
fun BackupRestoreTab(viewModel: AppViewModel, isAdmin: Boolean, isScrollable: Boolean = true) {
    val backupRestoreMessage by viewModel.backupRestoreMessage.collectAsStateWithLifecycle()
    val syncing by viewModel.syncing.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val syncSuccess by viewModel.syncSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.clearBackupMessage()
        viewModel.clearSyncStatus()
    }

    // Android Storage Access Framework file pickers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonString = viewModel.exportWargaToJson()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Database berhasil diekspor ke file JSON!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ekspor gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val csvString = viewModel.exportWargaToCsv()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvString.toByteArray(charset("UTF-8")))
                    }
                    Toast.makeText(context, "Database berhasil diekspor ke file Excel/CSV!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ekspor Excel gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    viewModel.restoreWargaFromJson(content)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Impor gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val scrollModifier = if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .then(scrollModifier)
    ) {
        Text(
            text = "Download & Restore Data",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isAdmin) "Gunakan menu ini untuk memback-up data kependudukan kelurahan keseluruhan." else "Back-up dan pulihkan data warga yang telah Anda input secara lokal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Download / Export
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Download Data (Backup)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Simpan data sebagai file berkas JSON.", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val fileName = "Back-up_Warga_BBS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan File")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download File JSON", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy to Clipboard Alternative
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val jsonString = viewModel.exportWargaToJson()
                            if (jsonString.isNotBlank()) {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clipData = android.content.ClipData.newPlainText("Warga BBS Backup JSON", jsonString)
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(context, "Data JSON disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Tidak ada data untuk disalin", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salin JSON ke Clipboard", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Download / Export Excel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Excel download icon",
                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32), // Forest green for Excel
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Download Data Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isAdmin) "Simpan seluruh data warga kelurahan sebagai berkas Excel/CSV." else "Simpan data warga yang Anda input sebagai berkas Excel/CSV.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val fileName = "Data_Warga_BBS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
                        exportExcelLauncher.launch(fileName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan File Excel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download File Excel (CSV)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy to Clipboard Alternative for Excel/CSV
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val csvString = viewModel.exportWargaToCsv()
                            if (csvString.isNotBlank()) {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clipData = android.content.ClipData.newPlainText("Warga BBS CSV", csvString)
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(context, "Data Excel/CSV disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Tidak ada data untuk disalin", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salin format Excel (CSV) ke Clipboard", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Restore / Import
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = "Restore icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Restore Data (Pulihkan)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Impor data warga dari berkas JSON eksternal.", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/plain"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Buka File")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Berkas File JSON", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Paste from clipboard option
                var showPasteDialog by remember { mutableStateOf(false) }
                if (showPasteDialog) {
                    var pastedText by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showPasteDialog = false },
                        title = { Text("Pulihkan dari Teks Clipboard") },
                        text = {
                            Column {
                                Text("Tempel string backup JSON di sini:", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pastedText,
                                    onValueChange = { pastedText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    placeholder = { Text("[{...}]") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.restoreWargaFromJson(pastedText)
                                showPasteDialog = false
                            }) {
                                Text("Kirim Pulihkan")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPasteDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                OutlinedButton(
                    onClick = { showPasteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Tempel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tempel JSON dari Clipboard", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (backupRestoreMessage != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = backupRestoreMessage ?: "",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ----------------------------------------------------
// ADMIN DASHBOARD SCREEN
// ----------------------------------------------------
@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val tabs = listOf(
        TabItem("Kelola Akun", Icons.Default.ManageAccounts),
        TabItem("Database Warga", Icons.Default.Folder),
        TabItem("Statistik", Icons.Default.BarChart),
        TabItem("Reset & Pengaturan", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "Admin Panel BBS",
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Admin Kelurahan",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bara Baraya Selatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log Out")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AdminKelolaAkunTab(viewModel = viewModel)
                1 -> AdminDatabaseWargaTab(viewModel = viewModel)
                2 -> UserStatistikTab(viewModel = viewModel, isAdminGlobal = true)
                3 -> AdminResetPasswordTab(viewModel = viewModel)
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN TAB 0: KELOLA AKUN USER YANG MENDAFTAR (VERIFIKASI SETUJU/TOLAK)
// ----------------------------------------------------
@Composable
fun AdminKelolaAkunTab(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()

    val pendingUsers = remember(users) { users.filter { it.status == "PENDING" && !it.isAdmin } }
    val approvedUsers = remember(users) { users.filter { it.status == "APPROVED" && !it.isAdmin } }
    val rejectedUsers = remember(users) { users.filter { it.status == "REJECTED" && !it.isAdmin } }

    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Kelola Akun User",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Setujui atau tolak akun petugas warga kelurahan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row for sub tabs
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(selected = selectedSubTab == 0, onClick = { selectedSubTab = 0 }) {
                Text("Pending (${pendingUsers.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSubTab == 1, onClick = { selectedSubTab = 1 }) {
                Text("Disetujui (${approvedUsers.size})", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedSubTab == 2, onClick = { selectedSubTab = 2 }) {
                Text("Ditolak (${rejectedUsers.size})", modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = when (selectedSubTab) {
            0 -> pendingUsers
            1 -> approvedUsers
            else -> rejectedUsers
        }

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada akun dalam daftar ini", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList, key = { it.username }) { user ->
                    UserAccountCardItem(
                        user = user,
                        onApprove = { viewModel.approveUser(user.username) },
                        onReject = { viewModel.rejectUser(user.username) },
                        onDelete = { viewModel.deleteUserAccount(user.username) }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN TAB 1: DATABASE WARGA KESELURUHAN (DIINPUT OLEH SEMUA USER)
// ----------------------------------------------------
@Composable
fun AdminDatabaseWargaTab(viewModel: AppViewModel) {
    val allWargaList by viewModel.allWarga.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var editTargetWarga by remember { mutableStateOf<Warga?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filteredList = remember(allWargaList, searchQuery) {
        allWargaList.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
                    it.nik.contains(searchQuery) ||
                    it.alamat.contains(searchQuery, ignoreCase = true) ||
                    it.inputtedBy.contains(searchQuery, ignoreCase = true)
        }
    }

    val exportXlsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.ms-excel")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val xlsString = viewModel.exportWargaToXls(filteredList) // exports current filtered warga list
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(xlsString.toByteArray(charset("UTF-8")))
                    }
                    Toast.makeText(context, "Data berhasil diekspor ke file Excel (.xls)!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Ekspor gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (editTargetWarga != null) {
        EditWargaDialog(
            warga = editTargetWarga!!,
            onDismiss = { editTargetWarga = null },
            onConfirm = { updated ->
                viewModel.editWarga(updated)
                editTargetWarga = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Database Keseluruhan Warga",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Seluruh data kependudukan Kelurahan Bara Baraya Selatan yang diinput oleh semua user.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Cari NIK, Nama, Alamat, atau Penginput...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Excel Download row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: ${filteredList.size} warga",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            if (filteredList.isNotEmpty()) {
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        exportXlsLauncher.launch("Data_Warga_BBS_Keseluruhan_$timestamp.xls")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Excel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unduh Excel (.xls)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Hasil pencarian kosong" else "Database warga kosong",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.nik }) { warga ->
                    WargaCardItem(
                        warga = warga,
                        onEdit = { editTargetWarga = warga },
                        onDelete = { viewModel.deleteWarga(warga) },
                        isAdminMode = true
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN TAB 3: RESET PASSWORD USER DAN PASSWORD ADMIN
// ----------------------------------------------------
@Composable
fun AdminResetPasswordTab(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val nonAdminUsers = remember(users) { users.filter { !it.isAdmin } }

    var adminNewPassword by remember { mutableStateOf("") }
    var selectedUserToReset by remember { mutableStateOf<UserAccount?>(null) }
    var userNewPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Reset Password & Pengaturan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Kelola kata sandi admin dan atur ulang sandi petugas kelurahan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Change Admin Password
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ubah Password Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = adminNewPassword,
                    onValueChange = { adminNewPassword = it },
                    label = { Text("Password Baru Admin") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (adminNewPassword.isNotBlank()) {
                            viewModel.changeAdminPassword(adminNewPassword.trim())
                            adminNewPassword = ""
                            Toast.makeText(context, "Password admin berhasil diubah!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Isi password baru terlebih dahulu", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ubah Sandi")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Reset User Password
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reset Password User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Selector
                var userExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedUserToReset?.username ?: "Pilih akun user...",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = "person") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(expanded = userExpanded, onDismissRequest = { userExpanded = false }) {
                        nonAdminUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.fullName} (${user.username})") },
                                onClick = {
                                    selectedUserToReset = user
                                    userExpanded = false
                                }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { userExpanded = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userNewPassword,
                    onValueChange = { userNewPassword = it },
                    label = { Text("Password Baru User") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = "Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedUserToReset != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val user = selectedUserToReset
                        if (user != null && userNewPassword.isNotBlank()) {
                            viewModel.resetPasswordByAdmin(user.username, userNewPassword.trim())
                            Toast.makeText(context, "Password user ${user.username} diset ulang!", Toast.LENGTH_SHORT).show()
                            selectedUserToReset = null
                            userNewPassword = ""
                        } else {
                            Toast.makeText(context, "Pilih user dan masukkan password baru", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = selectedUserToReset != null && userNewPassword.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Sandi User")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Backup Menu for Admin (so admin also has export/restore capability of the entire db!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kelola Database Eksternal (Admin)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ekspor atau pulihkan keseluruhan data kependudukan Bara Baraya Selatan.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                BackupRestoreTab(viewModel = viewModel, isAdmin = true, isScrollable = false)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Danger zone
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Zona Bahaya", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tindakan di bawah ini bersifat permanen dan tidak dapat dibatalkan.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                var showClearDialog by remember { mutableStateOf(false) }
                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text("Hapus Semua Data Warga?") },
                        text = { Text("Apakah Anda yakin ingin menghapus seluruh database warga dari sistem? Tindakan ini tidak bisa dibatalkan.") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.clearAllWarga()
                                Toast.makeText(context, "Seluruh database warga berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                showClearDialog = false
                            }) {
                                Text("Hapus Semua", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                Button(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Hapus")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Seluruh Database Warga", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ----------------------------------------------------
// SHARED UI: USER ACCOUNT CARD ITEM
// ----------------------------------------------------
@Composable
fun UserAccountCardItem(
    user: UserAccount,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (user.isAdmin) "Semua Wilayah" else "RT ${user.rt} / RW ${user.rw}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Badge
                val (color, text) = when (user.status) {
                    "PENDING" -> MaterialTheme.colorScheme.secondaryContainer to "Tertunda"
                    "APPROVED" -> MaterialTheme.colorScheme.primaryContainer to "Disetujui"
                    else -> MaterialTheme.colorScheme.errorContainer to "Ditolak"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (user.status == "PENDING") {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tolak", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak")
                    }
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Setuju", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setujui")
                    }
                } else {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Akun", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus User")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SHARED UI: WARGA CARD ITEM
// ----------------------------------------------------
@Composable
fun WargaCardItem(
    warga: Warga,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isAdminMode: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Data Penduduk?") },
            text = { Text("Apakah Anda yakin ingin menghapus data kependudukan NIK ${warga.nik} (${warga.nama})?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = warga.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = "NIK: ${warga.nik}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RT/RW ${warga.rtRw}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand details"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alamat: ${warga.alamat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isAdminMode) {
                    Text(
                        text = "by: @${warga.inputtedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanded details block
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 12.dp))

                    DetailRow(label = "No. KK", value = warga.noKk)
                    DetailRow(label = "Tempat, Tanggal Lahir", value = "${warga.tempatLahir}, ${warga.tanggalLahir}")
                    DetailRow(label = "Jenis Kelamin", value = warga.jenisKelamin)
                    DetailRow(label = "Hubungan Keluarga", value = warga.hubKeluarga)
                    DetailRow(label = "Agama", value = warga.agama)
                    DetailRow(label = "Status Pernikahan", value = warga.statusKawin)
                    DetailRow(label = "Pendidikan", value = warga.pendidikan)
                    DetailRow(label = "Pekerjaan", value = if (warga.pekerjaan == "Lainnya") "Lainnya: ${warga.pekerjaanDetail}" else warga.pekerjaan)
                    DetailRow(label = "Golongan Darah", value = warga.golDarah)
                    DetailRow(label = "No. Telepon/HP", value = warga.noHp.ifBlank { "-" })
                    DetailRow(label = "Keterangan", value = warga.keterangan)
                    if (warga.jumlahAnggotaKeluarga > 0) {
                        DetailRow(label = "Jml Anggota Kel. Tambahan", value = "${warga.jumlahAnggotaKeluarga}")
                    }
                    DetailRow(label = "Petugas Penginput", value = "@${warga.inputtedBy}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}

// ----------------------------------------------------
// EDIT WARGA DIALOG
// ----------------------------------------------------
@Composable
fun EditWargaDialog(
    warga: Warga,
    onDismiss: () -> Unit,
    onConfirm: (Warga) -> Unit
) {
    var nama by remember { mutableStateOf(warga.nama) }
    var noKk by remember { mutableStateOf(warga.noKk) }
    var tempatLahir by remember { mutableStateOf(warga.tempatLahir) }
    var tanggalLahir by remember { mutableStateOf(warga.tanggalLahir) }
    var jenisKelamin by remember { mutableStateOf(warga.jenisKelamin) }
    var alamat by remember { mutableStateOf(warga.alamat) }
    var rt by remember { mutableStateOf(warga.rt) }
    var rw by remember { mutableStateOf(warga.rw) }
    var hubKeluarga by remember { mutableStateOf(warga.hubKeluarga) }
    var agama by remember { mutableStateOf(warga.agama) }
    var statusKawin by remember { mutableStateOf(warga.statusKawin) }
    var pendidikan by remember { mutableStateOf(warga.pendidikan) }
    var pekerjaan by remember { mutableStateOf(warga.pekerjaan) }
    var pekerjaanDetail by remember { mutableStateOf(warga.pekerjaanDetail) }
    var golDarah by remember { mutableStateOf(warga.golDarah) }
    var noHp by remember { mutableStateOf(warga.noHp) }
    var keterangan by remember { mutableStateOf(warga.keterangan) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Data Penduduk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("NIK: ${warga.nik} (Tidak dapat diubah)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap *") })
                
                OutlinedTextField(
                    value = noKk,
                    onValueChange = { if (it.length <= 16) noKk = it.filter { char -> char.isDigit() } },
                    label = { Text("No. KK *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(value = tempatLahir, onValueChange = { tempatLahir = it }, label = { Text("Tempat Lahir *") })
                OutlinedTextField(value = tanggalLahir, onValueChange = { tanggalLahir = it }, label = { Text("Tgl Lahir (YYYY-MM-DD) *") })
                
                // Jenis Kelamin Selector
                Text("Jenis Kelamin *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { jenisKelamin = "Laki-laki" }) {
                        RadioButton(selected = jenisKelamin == "Laki-laki", onClick = { jenisKelamin = "Laki-laki" })
                        Text("Laki-laki")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { jenisKelamin = "Perempuan" }) {
                        RadioButton(selected = jenisKelamin == "Perempuan", onClick = { jenisKelamin = "Perempuan" })
                        Text("Perempuan")
                    }
                }

                OutlinedTextField(value = alamat, onValueChange = { alamat = it }, label = { Text("Alamat *") })
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rt,
                        onValueChange = { if (it.length <= 3) rt = it.filter { char -> char.isDigit() } },
                        label = { Text("RT (3 Digit) *") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = rw,
                        onValueChange = { if (it.length <= 3) rw = it.filter { char -> char.isDigit() } },
                        label = { Text("RW (3 Digit) *") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Hubungan Keluarga Dropdown
                var hubExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = hubKeluarga,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hubungan Keluarga *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = hubExpanded, onDismissRequest = { hubExpanded = false }) {
                        val daftarHub = listOf("Kepala Keluarga", "Isteri", "Anak", "Cucu", "Orang Tua", "Mertua", "Famili Lain")
                        daftarHub.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { hubKeluarga = option; hubExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { hubExpanded = true })
                }

                // Agama Selector
                var agamaExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = agama,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Agama *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = agamaExpanded, onDismissRequest = { agamaExpanded = false }) {
                        val daftarAgama = listOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu")
                        daftarAgama.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { agama = option; agamaExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { agamaExpanded = true })
                }

                // Status Pernikahan
                var statusExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = statusKawin,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Pernikahan *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        val daftarStatus = listOf("Belum Kawin", "Kawin", "Cerai Hidup", "Cerai Mati")
                        daftarStatus.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { statusKawin = option; statusExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { statusExpanded = true })
                }

                // Pendidikan Terakhir
                var pendExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = pendidikan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pendidikan Terakhir *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = pendExpanded, onDismissRequest = { pendExpanded = false }) {
                        val daftarPend = listOf("Tidak Sekolah", "SD Sederajat", "SMP Sederajat", "SMA / SMK Sederajat", "D2/D3", "D4/S1", "S2", "S3")
                        daftarPend.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { pendidikan = option; pendExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { pendExpanded = true })
                }

                // Pekerjaan Dropdown
                var pekExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = pekerjaan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pekerjaan *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = pekExpanded, onDismissRequest = { pekExpanded = false }) {
                        val daftarPek = listOf(
                            "Belum Bekerja", "Pelajar/Mahasiswa", "Buruh Harian Lepas", "Mengurus Rumah Tangga",
                            "Karyawan Swasta", "PNS", "PPPK", "Pedagang", "Polri/TNI", "Peg. BUMD/BUMN",
                            "Petani", "Nelayan", "Anggota DPRD", "Anggota DPR RI", "Pengacara", "Lainnya"
                        )
                        daftarPek.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { pekerjaan = option; pekExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { pekExpanded = true })
                }

                if (pekerjaan == "Lainnya") {
                    OutlinedTextField(
                        value = pekerjaanDetail,
                        onValueChange = { pekerjaanDetail = it },
                        label = { Text("Sebutkan Pekerjaan Detail *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Golongan Darah
                var golExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = golDarah,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Golongan Darah *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = golExpanded, onDismissRequest = { golExpanded = false }) {
                        val daftarGol = listOf("A", "B", "AB", "O", "-")
                        daftarGol.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { golDarah = option; golExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { golExpanded = true })
                }

                // No Telepon (Optional)
                OutlinedTextField(
                    value = noHp,
                    onValueChange = { noHp = it },
                    label = { Text("No. Telepon / HP") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Keterangan Dropdown
                var ketExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Keterangan *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = ketExpanded, onDismissRequest = { ketExpanded = false }) {
                        val daftarKet = listOf("Penduduk Tetap", "Domisili Sementara", "Meninggal", "Pindah Domisili", "Tidak Diketahui Alamatnya")
                        daftarKet.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { keterangan = option; ketExpanded = false })
                        }
                    }
                    Box(modifier = Modifier.matchParentSize().clickable { ketExpanded = true })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    warga.copy(
                        nama = nama.trim(),
                        noKk = noKk.trim(),
                        tempatLahir = tempatLahir.trim(),
                        tanggalLahir = tanggalLahir.trim(),
                        jenisKelamin = jenisKelamin,
                        alamat = alamat.trim(),
                        rt = rt.trim(),
                        rw = rw.trim(),
                        hubKeluarga = hubKeluarga,
                        agama = agama.trim(),
                        statusKawin = statusKawin.trim(),
                        pendidikan = pendidikan,
                        pekerjaan = pekerjaan.trim(),
                        pekerjaanDetail = pekerjaanDetail.trim(),
                        golDarah = golDarah.trim(),
                        noHp = noHp.trim(),
                        keterangan = keterangan
                    )
                )
            }) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Support Classes
data class TabItem(val title: String, val icon: ImageVector)

// Top App Bar helper to avoid Opt-In warning blocks
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptInTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

// Scroll state remember Helper for lazy compilation
@Composable
fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()

// ----------------------------------------------------
// DYNAMIC FAMILY REGISTRATION SUPPORT
// ----------------------------------------------------
class FamilyMemberInputState {
    var nik by mutableStateOf("")
    var nama by mutableStateOf("")
    var tempatLahir by mutableStateOf("")
    var tanggalLahir by mutableStateOf("")
    var jenisKelamin by mutableStateOf("Laki-laki")
    var agama by mutableStateOf("Islam")
    var statusKawin by mutableStateOf("Belum Kawin")
    var pendidikan by mutableStateOf("Tidak Sekolah")
    var pekerjaan by mutableStateOf("Belum Bekerja")
    var pekerjaanDetail by mutableStateOf("")
    var golDarah by mutableStateOf("O")
    var hubKeluarga by mutableStateOf("Anak")
    var noHp by mutableStateOf("")
    var keterangan by mutableStateOf("Penduduk Tetap")
}

@Composable
fun FamilyMemberFormBlock(
    member: FamilyMemberInputState,
    index: Int,
    allWargaList: List<Warga>,
    mainNik: String,
    familyMembers: List<FamilyMemberInputState>,
    hasAttemptedSubmit: Boolean
) {
    val trimNik = member.nik.trim()
    val isNikDuplicateDb = allWargaList.any { it.nik == trimNik && trimNik.isNotEmpty() }
    val isNikDuplicateForm = (trimNik == mainNik.trim() && trimNik.isNotEmpty()) || 
        familyMembers.filterIndexed { idx, _ -> idx != index }.any { it.nik.trim() == trimNik && trimNik.isNotEmpty() }
    val isNikInvalidFormat = trimNik.length != 16 || !trimNik.all { it.isDigit() }
    val isNikError = isNikDuplicateDb || isNikDuplicateForm || (hasAttemptedSubmit && isNikInvalidFormat)

    val isNamaError = hasAttemptedSubmit && member.nama.trim().isBlank()
    val isTempatLahirError = hasAttemptedSubmit && member.tempatLahir.trim().isBlank()
    val isTanggalLahirInvalid = member.tanggalLahir.trim().isBlank() || !member.tanggalLahir.trim().matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    val isTanggalLahirError = hasAttemptedSubmit && isTanggalLahirInvalid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Anggota Keluarga #${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Menggunakan No. KK, Alamat, dan RT/RW yang sama dengan Kepala Keluarga.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = member.nik,
                onValueChange = { if (it.length <= 16) member.nik = it.filter { char -> char.isDigit() } },
                label = { Text("NIK Anggota (16 Digit) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isNikError,
                supportingText = {
                    if (isNikDuplicateDb) {
                        Text("NIK sudah terdaftar di database", color = MaterialTheme.colorScheme.error)
                    } else if (isNikDuplicateForm) {
                        Text("NIK duplikat di dalam form", color = MaterialTheme.colorScheme.error)
                    } else if (hasAttemptedSubmit && isNikInvalidFormat) {
                        Text("NIK wajib 16 digit angka", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = member.nama,
                onValueChange = { member.nama = it },
                label = { Text("Nama Lengkap Anggota *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isNamaError,
                supportingText = {
                    if (isNamaError) {
                        Text("Nama lengkap wajib diisi", color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.tempatLahir,
                    onValueChange = { member.tempatLahir = it },
                    label = { Text("Tempat Lahir *") },
                    modifier = Modifier
                        .weight(1.3f)
                        .padding(end = 4.dp),
                    singleLine = true,
                    isError = isTempatLahirError,
                    supportingText = {
                        if (isTempatLahirError) {
                            Text("Wajib diisi", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = member.tanggalLahir,
                    onValueChange = { member.tanggalLahir = it },
                    label = { Text("Tgl Lahir (YYYY-MM-DD) *") },
                    modifier = Modifier
                        .weight(1.7f)
                        .padding(start = 4.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. 1995-12-30") },
                    isError = isTanggalLahirError,
                    supportingText = {
                        if (isTanggalLahirError) {
                            Text("Format YYYY-MM-DD wajib", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Jenis Kelamin *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { member.jenisKelamin = "Laki-laki" }
                ) {
                    RadioButton(selected = member.jenisKelamin == "Laki-laki", onClick = { member.jenisKelamin = "Laki-laki" })
                    Text("Laki-laki", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { member.jenisKelamin = "Perempuan" }
                ) {
                    RadioButton(selected = member.jenisKelamin == "Perempuan", onClick = { member.jenisKelamin = "Perempuan" })
                    Text("Perempuan", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hubungan Keluarga Dropdown
            var hubExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.hubKeluarga,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hubungan Keluarga *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = hubExpanded, onDismissRequest = { hubExpanded = false }) {
                    val daftarHub = listOf("Kepala Keluarga", "Isteri", "Anak", "Cucu", "Orang Tua", "Mertua", "Famili Lain")
                    daftarHub.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.hubKeluarga = option
                                hubExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { hubExpanded = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Agama Dropdown
            var agamaExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.agama,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Agama *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = agamaExpanded, onDismissRequest = { agamaExpanded = false }) {
                    val daftarAgama = listOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu")
                    daftarAgama.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.agama = option
                                agamaExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { agamaExpanded = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Pernikahan Dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.statusKawin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Perkawinan *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    val daftarStatus = listOf("Belum Kawin", "Kawin", "Cerai Hidup", "Cerai Mati")
                    daftarStatus.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.statusKawin = option
                                statusExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { statusExpanded = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pendidikan Terakhir Dropdown
            var pendExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.pendidikan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pendidikan Terakhir *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = pendExpanded, onDismissRequest = { pendExpanded = false }) {
                    val daftarPend = listOf("Tidak Sekolah", "SD Sederajat", "SMP Sederajat", "SMA / SMK Sederajat", "D2/D3", "D4/S1", "S2", "S3")
                    daftarPend.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.pendidikan = option
                                pendExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { pendExpanded = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pekerjaan Dropdown
            var pekExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.pekerjaan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pekerjaan *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = pekExpanded, onDismissRequest = { pekExpanded = false }) {
                    val daftarPek = listOf(
                        "Belum Bekerja", "Pelajar/Mahasiswa", "Buruh Harian Lepas", "Mengurus Rumah Tangga",
                        "Karyawan Swasta", "PNS", "PPPK", "Pedagang", "Polri/TNI", "Peg. BUMD/BUMN",
                        "Petani", "Nelayan", "Anggota DPRD", "Anggota DPR RI", "Pengacara", "Lainnya"
                    )
                    daftarPek.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.pekerjaan = option
                                pekExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { pekExpanded = true }
                )
            }

            if (member.pekerjaan == "Lainnya") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = member.pekerjaanDetail,
                    onValueChange = { member.pekerjaanDetail = it },
                    label = { Text("Sebutkan Detail Pekerjaan *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Golongan Darah Dropdown
            var golExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.golDarah,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Golongan Darah *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = golExpanded, onDismissRequest = { golExpanded = false }) {
                    val daftarGol = listOf("A", "B", "AB", "O", "-")
                    daftarGol.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.golDarah = option
                                golExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { golExpanded = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // No Telepon/HP (Optional)
            OutlinedTextField(
                value = member.noHp,
                onValueChange = { member.noHp = it },
                label = { Text("No. Telepon / HP (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Keterangan Kependudukan Dropdown
            var ketExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = member.keterangan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Keterangan Kependudukan *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = ketExpanded, onDismissRequest = { ketExpanded = false }) {
                    val daftarKet = listOf("Penduduk Tetap", "Domisili Sementara", "Meninggal", "Pindah Domisili", "Tidak Diketahui Alamatnya")
                    daftarKet.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                member.keterangan = option
                                ketExpanded = false
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { ketExpanded = true }
                )
            }
        }
    }
}
