package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warga_list")
data class Warga(
    @PrimaryKey val nik: String, // 16-digit unique identifier
    val nama: String,
    val tempatLahir: String,
    val tanggalLahir: String, // format: YYYY-MM-DD
    val jenisKelamin: String, // "Laki-laki" / "Perempuan"
    val alamat: String,
    val rt: String, // Separate RT field
    val rw: String, // Separate RW field
    val agama: String,
    val statusKawin: String, // "Belum Kawin" / "Kawin" / "Cerai Hidup" / "Cerai Mati"
    val pekerjaan: String,
    val golDarah: String, // "A" / "B" / "AB" / "O" / "-"
    val noKk: String, // 16-digit Family Card Number (No. KK)
    val hubKeluarga: String, // Kepala Keluarga, Isteri, anak, cucu, Orang tua, Mertua, Famili lain
    val pendidikan: String, // Tidak Sekolah, SD sederajat, SMP sederajat, SMA /SMK sederajat, D2/D3, D4/S1, S2, S3
    val pekerjaanDetail: String = "", // Custom manual text if pekerjaan is "Lainnya"
    val noHp: String = "", // Optional phone number
    val keterangan: String, // Penduduk tetap, Domisili Sementara, Meninggal, Pindah domisili, Tidak Diketahui alamatnya
    val jumlahAnggotaKeluarga: Int = 0, // Number of family members input
    val inputtedBy: String, // username of user who inputted this record
    val timestamp: Long = System.currentTimeMillis()
) {
    // Helper to keep compatibility or easy display of RT/RW
    val rtRw: String
        get() = "$rt / $rw"
}
