# Panduan Pengujian & Dokumentasi Frontend PBI-04 (JRI-17)

Dokumentasi ini menjelaskan implementasi frontend untuk **PBI-04 (JRI-17: "Menampilkan Detail Lengkap Paket Jasa")** yang mencakup:
- **UC-03:** UMKM melihat rincian biaya paket jasa ("Mulai dari Rp X", estimasi durasi, dan daftar varian transparan tanpa biaya tersembunyi).
- **UC-18:** Freelancer mengelola varian harga dan estimasi durasi paket (tambah, lihat, ubah, dan hapus/soft delete varian) dengan penandaan error validasi per kolom.
- **FR-02:** Rincian biaya ditampilkan transparan tanpa biaya tersembunyi.

---

## 1. Konfigurasi Environment & Base URL
- Modul HTTP client membaca base URL backend dari variabel lingkungan:
  ```env
  VITE_API_BASE_URL=http://localhost:8080
  ```
- Jika variabel ini tidak disetel, sistem secara otomatis default ke `http://localhost:8080`.

---

## 2. Rute Halaman (Routes)
1. **Halaman Detail Paket (UMKM - UC-03)**
   - Path: `/paket/:id`
   - Nama Route: `paket-detail`
   - Contoh URL: `http://localhost:5173/paket/1`
2. **Halaman Kelola Varian (Freelancer - UC-18)**
   - Path: `/freelancer/paket/:id/varian`
   - Nama Route: `kelola-varian`
   - Contoh URL: `http://localhost:5173/freelancer/paket/1/varian`

---

## 3. Autentikasi & Cara Menempelkan Token Uji Tanpa Halaman Login
Modul PBI-04 membaca token JWT dari SATU key localStorage:
```javascript
localStorage.getItem('jemari_auth_token')
```

### Cara 1: Menggunakan Tombol Helper di Halaman Freelancer
1. Buka halaman `/freelancer/paket/1/varian` di browser.
2. Di pojok kanan atas, klik tombol **"🔑 Pasang Token Uji"**.
3. Tempelkan token JWT Bearer (tanpa kata "Bearer "), lalu klik **"Simpan"**.
4. Token akan otomatis tersimpan di `localStorage.jemari_auth_token` dan dipakai pada setiap panggilan API berikutnya.

### Cara 2: Melalui Developer Tools Console (F12)
Buka tab **Console** di browser dan jalankan:
```javascript
// Untuk role FREELANCER (mengelola varian):
localStorage.setItem('jemari_auth_token', '<TOKEN_JWT_FREELANCER_ANDA>');

// Untuk role UMKM (melihat rincian biaya):
localStorage.setItem('jemari_auth_token', '<TOKEN_JWT_UMKM_ANDA>');

// Untuk menghapus token:
localStorage.removeItem('jemari_auth_token');
```

---

## 4. Endpoint Backend yang Digunakan

### A. Sisi UMKM (Role `UMKM`)
* `GET /api/paket/{paketId}/rincian-biaya`
  - Respons sukses:
    ```json
    {
      "success": true,
      "message": "Berhasil mengambil rincian biaya paket",
      "data": {
        "packageId": 1,
        "hargaMulai": 50000.0,
        "ringkasanEstimasi": {
          "durasiTercepat": 2,
          "durasiTerlama": 5
        },
        "varian": [
          {
            "id": 10,
            "name": "Basic",
            "description": "Desain 1 konsep logo",
            "price": 50000.0,
            "estimatedDays": 2
          }
        ]
      }
    }
    ```
  - Jika paket tidak aktif atau tidak ditemukan: status `404` dengan pesan `"Paket tidak tersedia"`.

### B. Sisi Freelancer (Role `FREELANCER`)
* `GET /api/freelancer/paket/{paketId}/varian` $\rightarrow$ Daftar varian aktif
* `POST /api/freelancer/paket/{paketId}/varian` $\rightarrow$ Tambah varian baru
* `PUT /api/freelancer/paket/{paketId}/varian/{varianId}` $\rightarrow$ Perbarui varian
* `DELETE /api/freelancer/paket/{paketId}/varian/{varianId}` $\rightarrow$ Hapus varian (soft delete)

---

## 5. Mockup Proposal vs Integrasi Data Nyata
Sesuai rancangan proposal 4.5.12:
- **Data Nyata (dari PBI-04):**
  - "Mulai dari Rp X" (diambil dari varian termurah).
  - Estimasi pengerjaan (diambil dari rentang durasi varian aktif, mis. "2–5 hari").
  - Daftar pilihan varian dan rincian biaya transparan (nama varian, deskripsi, harga, durasi).
- **Placeholder Netral (karena modul PBI lain belum ada di branch ini):**
  - Galeri hasil kerja $\rightarrow$ area placeholder diagonal pattern sesuai mockup (milik PBI-02).
  - Judul paket $\rightarrow$ `Paket Jasa Digital #<id>` (milik PBI-02).
  - Kuota revisi $\rightarrow$ `3x Revisi (Ketentuan PBI-20)`.
  - Profil penyedia $\rightarrow$ `Freelancer JEMARI (PBI-01)`.
  - Tombol aksi $\rightarrow$ `Hubungi via Request` dan `Pesan & Bayar DP 50%` dinonaktifkan dengan tanda Fase 2.
