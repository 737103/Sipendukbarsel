package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.UserAccount
import com.example.data.model.Warga
import com.example.data.repository.AppRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.flatMapLatest

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.userDao(), database.wargaDao())
        
        // Ensure admin user is seeded at app launch
        viewModelScope.launch {
            repository.ensureAdminExists()
        }
    }

    // Auth State
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWarga: StateFlow<List<Warga>> = repository.allWargaFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Warga filtered by current user's RT and RW to separate/group data
    val currentUserWarga: StateFlow<List<Warga>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                if (user.isAdmin) {
                    repository.allWargaFlow
                } else {
                    repository.allWargaFlow.map { list ->
                        list.filter { it.rt == user.rt && it.rw == user.rw }
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback messages
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError = _registerError.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    private val _inputError = MutableStateFlow<String?>(null)
    val inputError = _inputError.asStateFlow()

    private val _inputSuccess = MutableStateFlow(false)
    val inputSuccess = _inputSuccess.asStateFlow()

    private val _backupRestoreMessage = MutableStateFlow<String?>(null)
    val backupRestoreMessage = _backupRestoreMessage.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val wargaListType = Types.newParameterizedType(List::class.java, Warga::class.java)
    private val jsonAdapter = moshi.adapter<List<Warga>>(wargaListType)

    fun clearLoginError() { _loginError.value = null }
    fun clearRegisterError() { _registerError.value = null; _registerSuccess.value = false }
    fun clearInputSuccess() { _inputSuccess.value = false }
    fun clearInputError() { _inputError.value = null }
    fun clearBackupMessage() { _backupRestoreMessage.value = null }

    // Authentication
    fun login(username: String, passwordPlain: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (username.isBlank() || passwordPlain.isBlank()) {
                _loginError.value = "Username dan password tidak boleh kosong"
                return@launch
            }

            val user = repository.getUserByUsername(username.trim())
            if (user == null) {
                _loginError.value = "Username tidak ditemukan"
            } else if (user.passwordHash != passwordPlain) {
                _loginError.value = "Password salah"
            } else {
                when (user.status) {
                    "PENDING" -> _loginError.value = "Akun Anda belum disetujui oleh admin kelurahan"
                    "REJECTED" -> _loginError.value = "Pendaftaran akun Anda ditolak oleh admin kelurahan"
                    "APPROVED" -> {
                        _currentUser.value = user
                    }
                }
            }
        }
    }

    fun register(username: String, passwordPlain: String, fullName: String, rt: String, rw: String) {
        viewModelScope.launch {
            _registerError.value = null
            _registerSuccess.value = false

            if (username.isBlank() || passwordPlain.isBlank() || fullName.isBlank() || rt.isBlank() || rw.isBlank()) {
                _registerError.value = "Semua field termasuk RT dan RW harus diisi"
                return@launch
            }

            if (rt.trim().length != 3 || !rt.trim().all { it.isDigit() } || rw.trim().length != 3 || !rw.trim().all { it.isDigit() }) {
                _registerError.value = "RT dan RW masing-masing harus berupa angka 3 digit (contoh: 001)"
                return@launch
            }

            if (username.trim().lowercase() == "admin") {
                _registerError.value = "Username 'admin' sudah digunakan"
                return@launch
            }

            val existing = repository.getUserByUsername(username.trim())
            if (existing != null) {
                _registerError.value = "Username sudah terdaftar"
            } else {
                val newUser = UserAccount(
                    username = username.trim(),
                    passwordHash = passwordPlain,
                    fullName = fullName.trim(),
                    status = "PENDING",
                    isAdmin = false,
                    rt = rt.trim(),
                    rw = rw.trim()
                )
                repository.registerUser(newUser)
                _registerSuccess.value = true
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Citizen management (Input Warga)
    fun inputWarga(mainWarga: Warga, familyMembers: List<Warga>) {
        viewModelScope.launch {
            _inputError.value = null
            _inputSuccess.value = false

            val currentUsername = _currentUser.value?.username ?: return@launch

            // Validate main citizen
            val mainErr = validateWargaSingle(mainWarga)
            if (mainErr != null) {
                _inputError.value = "Data Utama: $mainErr"
                return@launch
            }

            // Validate all family members
            for (i in familyMembers.indices) {
                val member = familyMembers[i]
                val err = validateWargaSingle(member)
                if (err != null) {
                    _inputError.value = "Anggota Keluarga #${i + 1} (${member.nama.ifBlank { "Tanpa Nama" }}): $err"
                    return@launch
                }
            }

            // Check for duplicate NIKs in the form
            val allNiks = mutableListOf(mainWarga.nik)
            allNiks.addAll(familyMembers.map { it.nik })
            if (allNiks.size != allNiks.distinct().size) {
                _inputError.value = "Ditemukan duplikasi NIK di dalam form pengisian"
                return@launch
            }

            // Check for duplicate NIKs in the database
            for (nik in allNiks) {
                val existing = repository.getWargaByNik(nik)
                if (existing != null) {
                    _inputError.value = "NIK $nik sudah terdaftar di sistem (oleh: ${existing.inputtedBy})"
                    return@launch
                }
            }

            try {
                // Insert main citizen
                repository.insertWarga(mainWarga.copy(inputtedBy = currentUsername))
                // Insert family members
                for (member in familyMembers) {
                    repository.insertWarga(member.copy(inputtedBy = currentUsername))
                }
                _inputSuccess.value = true
            } catch (e: Exception) {
                _inputError.value = "Gagal menyimpan data: ${e.localizedMessage}"
            }
        }
    }

    private fun validateWargaSingle(warga: Warga): String? {
        if (warga.nik.length != 16 || !warga.nik.all { it.isDigit() }) {
            return "NIK harus terdiri dari 16 digit angka"
        }
        if (warga.nama.isBlank()) {
            return "Nama lengkap harus diisi"
        }
        if (warga.noKk.length != 16 || !warga.noKk.all { it.isDigit() }) {
            return "No. KK harus terdiri dari 16 digit angka"
        }
        if (warga.tempatLahir.isBlank()) {
            return "Tempat lahir harus diisi"
        }
        if (warga.tanggalLahir.isBlank()) {
            return "Tanggal lahir harus diisi (Format: YYYY-MM-DD)"
        }
        if (warga.alamat.isBlank()) {
            return "Alamat harus diisi"
        }
        if (warga.rt.length != 3 || !warga.rt.all { it.isDigit() }) {
            return "RT harus terdiri dari 3 digit angka (contoh: 001)"
        }
        if (warga.rw.length != 3 || !warga.rw.all { it.isDigit() }) {
            return "RW harus terdiri dari 3 digit angka (contoh: 002)"
        }
        if (warga.pekerjaan.isBlank()) {
            return "Pekerjaan harus diisi"
        }
        if (warga.pekerjaan == "Lainnya" && warga.pekerjaanDetail.isBlank()) {
            return "Sebutkan pekerjaan detail lainnya"
        }
        if (warga.keterangan.isBlank()) {
            return "Keterangan kependudukan harus diisi"
        }
        return null
    }

    fun editWarga(warga: Warga) {
        viewModelScope.launch {
            _inputError.value = null
            _inputSuccess.value = false

            val err = validateWargaSingle(warga)
            if (err != null) {
                _inputError.value = "Gagal mengubah data: $err"
                return@launch
            }

            try {
                repository.updateWarga(warga)
                _inputSuccess.value = true
            } catch (e: Exception) {
                _inputError.value = "Gagal mengubah data: ${e.localizedMessage}"
            }
        }
    }

    fun deleteWarga(warga: Warga) {
        viewModelScope.launch {
            try {
                repository.deleteWarga(warga)
            } catch (e: Exception) {
                _inputError.value = "Gagal menghapus data: ${e.localizedMessage}"
            }
        }
    }

    // Admin Operations
    fun approveUser(username: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                repository.updateUser(user.copy(status = "APPROVED"))
            }
        }
    }

    fun rejectUser(username: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                repository.updateUser(user.copy(status = "REJECTED"))
            }
        }
    }

    fun deleteUserAccount(username: String) {
        viewModelScope.launch {
            repository.deleteUser(username)
        }
    }

    fun resetPasswordByAdmin(username: String, newPasswordPlain: String) {
        viewModelScope.launch {
            repository.resetUserPassword(username, newPasswordPlain)
        }
    }

    fun changeAdminPassword(newPasswordPlain: String) {
        viewModelScope.launch {
            repository.resetUserPassword("admin", newPasswordPlain)
            // Update current user cached copy if admin is currently logged in
            val current = _currentUser.value
            if (current != null && current.username == "admin") {
                _currentUser.value = current.copy(passwordHash = newPasswordPlain)
            }
        }
    }

    fun clearAllWarga() {
        viewModelScope.launch {
            repository.clearAllWarga()
        }
    }

    // Backup & Restore
    suspend fun exportWargaToJson(): String {
        return try {
            val isUserAdmin = _currentUser.value?.isAdmin == true
            val dataToExport = if (isUserAdmin) {
                repository.getAllWarga()
            } else {
                val user = _currentUser.value?.username ?: ""
                repository.getWargaByInputter(user)
            }
            jsonAdapter.toJson(dataToExport)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun exportWargaToCsv(): String {
        return try {
            val isUserAdmin = _currentUser.value?.isAdmin == true
            val dataToExport = if (isUserAdmin) {
                repository.getAllWarga()
            } else {
                val user = _currentUser.value?.username ?: ""
                repository.getWargaByInputter(user)
            }
            val sb = java.lang.StringBuilder()
            // Excel directive for separator
            sb.append("sep=,\n")
            // Headers
            sb.append("NIK,No. KK,Nama Lengkap,Tempat Lahir,Tanggal Lahir,Jenis Kelamin,Alamat,RT,RW,Agama,Status Kawin,Pekerjaan,Detail Pekerjaan,No. HP,Hubungan Keluarga,Pendidikan,Keterangan,Jumlah Anggota Keluarga,Diinput Oleh\n")
            for (w in dataToExport) {
                val row = listOf(
                    escapeCsv(w.nik),
                    escapeCsv(w.noKk),
                    escapeCsv(w.nama),
                    escapeCsv(w.tempatLahir),
                    escapeCsv(w.tanggalLahir),
                    escapeCsv(w.jenisKelamin),
                    escapeCsv(w.alamat),
                    escapeCsv(w.rt),
                    escapeCsv(w.rw),
                    escapeCsv(w.agama),
                    escapeCsv(w.statusKawin),
                    escapeCsv(w.pekerjaan),
                    escapeCsv(w.pekerjaanDetail),
                    escapeCsv(w.noHp),
                    escapeCsv(w.hubKeluarga),
                    escapeCsv(w.pendidikan),
                    escapeCsv(w.keterangan),
                    escapeCsv(w.jumlahAnggotaKeluarga.toString()),
                    escapeCsv(w.inputtedBy)
                )
                sb.append(row.joinToString(",")).append("\n")
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\r", "").replace("\n", " ")
        if (clean.contains(",") || clean.contains("\"")) {
            return "\"" + clean.replace("\"", "\"\"") + "\""
        }
        return clean
    }

    suspend fun exportWargaToXls(customList: List<Warga>? = null): String {
        return try {
            val dataToExport = if (customList != null) {
                customList
            } else {
                val isUserAdmin = _currentUser.value?.isAdmin == true
                if (isUserAdmin) {
                    repository.getAllWarga()
                } else {
                    val user = _currentUser.value?.username ?: ""
                    repository.getWargaByInputter(user)
                }
            }
            val sb = java.lang.StringBuilder()
            sb.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">\n")
            sb.append("<head>\n")
            sb.append("<meta http-equiv=\"content-type\" content=\"application/vnd.ms-excel; charset=UTF-8\">\n")
            sb.append("<!--[if gte mso 9]><xml>\n")
            sb.append("<x:ExcelWorkbook>\n")
            sb.append("<x:ExcelWorksheets>\n")
            sb.append("<x:ExcelWorksheet>\n")
            sb.append("<x:Name>Data Warga BBS</x:Name>\n")
            sb.append("<x:WorksheetOptions>\n")
            sb.append("<x:DisplayGridlines/>\n")
            sb.append("</x:WorksheetOptions>\n")
            sb.append("</x:ExcelWorksheet>\n")
            sb.append("</x:ExcelWorksheets>\n")
            sb.append("</x:ExcelWorkbook>\n")
            sb.append("</xml><![endif]-->\n")
            sb.append("<style>\n")
            sb.append("table { border-collapse: collapse; width: 100%; }\n")
            sb.append("th { background-color: #2E7D32; color: white; font-weight: bold; padding: 8px; border: 1px solid #dddddd; }\n")
            sb.append("td { padding: 8px; border: 1px solid #dddddd; mso-number-format:\"\\@\"; }\n")
            sb.append("</style>\n")
            sb.append("</head>\n")
            sb.append("<body>\n")
            sb.append("<h2>Data Warga Kelurahan Bara Baraya Selatan</h2>\n")
            sb.append("<table border=\"1\">\n")
            sb.append("<thead>\n")
            sb.append("<tr>\n")
            val headers = listOf(
                "NIK", "No. KK", "Nama Lengkap", "Tempat Lahir", "Tanggal Lahir", "Jenis Kelamin",
                "Alamat", "RT", "RW", "Agama", "Status Kawin", "Pekerjaan", "Detail Pekerjaan",
                "No. HP", "Hubungan Keluarga", "Pendidikan", "Keterangan", "Jumlah Anggota Keluarga", "Diinput Oleh"
            )
            for (header in headers) {
                sb.append("  <th>").append(escapeHtml(header)).append("</th>\n")
            }
            sb.append("</tr>\n")
            sb.append("</thead>\n")
            sb.append("<tbody>\n")
            for (w in dataToExport) {
                sb.append("<tr>\n")
                val fields = listOf(
                    w.nik, w.noKk, w.nama, w.tempatLahir, w.tanggalLahir, w.jenisKelamin,
                    w.alamat, w.rt, w.rw, w.agama, w.statusKawin, w.pekerjaan, w.pekerjaanDetail,
                    w.noHp, w.hubKeluarga, w.pendidikan, w.keterangan, w.jumlahAnggotaKeluarga.toString(), w.inputtedBy
                )
                for (field in fields) {
                    sb.append("  <td>").append(escapeHtml(field)).append("</td>\n")
                }
                sb.append("</tr>\n")
            }
            sb.append("</tbody>\n")
            sb.append("</table>\n")
            sb.append("</body>\n")
            sb.append("</html>\n")
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun escapeHtml(value: String): String {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    fun restoreWargaFromJson(jsonString: String) {
        viewModelScope.launch {
            _backupRestoreMessage.value = null
            if (jsonString.isBlank()) {
                _backupRestoreMessage.value = "Format JSON kosong atau tidak valid"
                return@launch
            }

            try {
                val list = jsonAdapter.fromJson(jsonString)
                if (list.isNullOrEmpty()) {
                    _backupRestoreMessage.value = "Data warga tidak ditemukan dalam berkas"
                    return@launch
                }

                var successCount = 0
                var duplicateCount = 0
                val currentUserVal = _currentUser.value ?: return@launch

                for (warga in list) {
                    val existing = repository.getWargaByNik(warga.nik)
                    if (existing != null) {
                        duplicateCount++
                    } else {
                        // For normal user, force their username as inputter for imported data
                        // For admin, preserve original or update to admin
                        val updatedWarga = if (currentUserVal.isAdmin) {
                            warga
                        } else {
                            warga.copy(inputtedBy = currentUserVal.username)
                        }
                        repository.insertWarga(updatedWarga)
                        successCount++
                    }
                }

                _backupRestoreMessage.value = "Berhasil memulihkan $successCount data. Terlewati $duplicateCount data (NIK ganda)."
            } catch (e: Exception) {
                _backupRestoreMessage.value = "Gagal memulihkan data: ${e.localizedMessage}"
            }
        }
    }
}
