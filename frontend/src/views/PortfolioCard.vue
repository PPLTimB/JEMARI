<template>
  <div style="padding: 20px; font-family: sans-serif;">
    <h2>Portofolio Freelancer (Slicing Hi-Fi)</h2>

    <!-- Kartu Portofolio -->
    <div class="card-container">

      <!-- Area Gambar dengan styling object-fit -->
      <img src="https://via.placeholder.com/300x150" alt="Foto Portofolio" class="card-image" />

      <!-- Area Teks -->
      <div class="card-body">
        <h3 class="card-title">{{ portofolio.judul }}</h3>
        <p class="card-author">Dibuat oleh: <strong>{{ portofolio.namaFreelancer }}</strong></p>
        <p class="card-description">{{ portofolio.deskripsi }}</p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const portofolio = ref({
  judul: "Desain UI/UX Aplikasi Kopi",
  namaFreelancer: "Clavin",
  deskripsi: "Membuat desain antarmuka aplikasi pemesanan kopi yang modern, bersih, dan mudah digunakan oleh pelanggan."
})
</script>

<style scoped>
/* --- PEKERJAAN 13 NOVEMBER (Slicing CSS dari Hi-Fi) --- */
.card-container {
  width: 300px;
  border: 1px solid #E5E7EB; /* Warna garis pinggir abu-abu muda */
  border-radius: 12px; /* Membuat sudut membulat */
  overflow: hidden; /* Mencegah gambar keluar dari sudut membulat */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); /* Bayangan lembut */
  background-color: #FFFFFF;
}

.card-image {
  width: 100%;
  height: 150px;
  object-fit: cover; /* Mencegah gambar gepeng */
}

.card-body {
  padding: 16px; /* Jarak dari pinggir teks ke garis kartu */
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #111827; /* Warna hitam pekat untuk judul */
}

.card-author {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6B7280; /* Warna abu-abu untuk nama */
}

.card-description {
  margin: 0;
  font-size: 14px;
  color: #4B5563;
  line-height: 1.5; /* Jarak antar baris teks agar enak dibaca */
}
</style>