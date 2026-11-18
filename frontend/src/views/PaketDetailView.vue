<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchRincianBiayaUmkm } from '../api/paketVarian'
import { formatRupiah, formatRentangDurasi } from '../utils/rupiahPaket'
import PaketGaleriPlaceholder from '../components/paket/PaketGaleriPlaceholder.vue'
import PaketRincianBiayaCard from '../components/paket/PaketRincianBiayaCard.vue'

const route = useRoute()
const router = useRouter()

const paketId = computed(() => route.params.id)

const loading = ref(true)
const errorState = ref(null) // { status, message }
const rincianBiaya = ref(null)

async function muatRincianPaket() {
  loading.value = true
  errorState.value = null

  try {
    const data = await fetchRincianBiayaUmkm(paketId.value)
    rincianBiaya.value = data
  } catch (err) {
    errorState.value = {
      status: err.status || 500,
      message: err.message || 'Terjadi kesalahan saat memuat rincian paket.',
    }
  } finally {
    loading.value = false
  }
}

function handleKembaliKeKatalog() {
  router.push('/katalog').catch(() => {
    router.push('/')
  })
}

onMounted(() => {
  muatRincianPaket()
})
</script>

<template>
  <div class="page-container">
    <!-- Navigasi Atas / Breadcrumb -->
    <nav class="top-nav" aria-label="Navigasi halaman">
      <button type="button" class="btn-back" @click="handleKembaliKeKatalog">← Katalog Jasa</button>

      <router-link
        :to="`/freelancer/paket/${paketId}/varian`"
        class="link-mode-freelancer"
        title="Buka panel kelola varian untuk freelancer"
      >
        ⚙️ Mode Freelancer: Kelola Varian
      </router-link>
    </nav>

    <!-- State 1: Memuat Data (Loading) -->
    <main v-if="loading" class="state-container loading-state" aria-busy="true">
      <div class="spinner" aria-hidden="true"></div>
      <p>Memuat rincian paket jasa...</p>
    </main>

    <!-- State 2: Paket Tidak Tersedia / Error (UC-03) -->
    <main v-else-if="errorState" class="state-container error-state" role="alert">
      <div class="error-card">
        <span class="error-icon" aria-hidden="true">⚠️</span>
        <!-- Pesan khusus jika 404 Paket Tidak Tersedia (UC-03) -->
        <h1 v-if="errorState.status === 404" class="error-title">Paket Tidak Tersedia</h1>
        <h1 v-else class="error-title">Gagal Memuat Paket</h1>

        <p class="error-message">
          {{ errorState.message }}
        </p>

        <div class="error-actions">
          <button type="button" class="btn-action-primary" @click="handleKembaliKeKatalog">
            Kembali ke Katalog
          </button>
          <button
            v-if="errorState.status !== 404"
            type="button"
            class="btn-action-secondary"
            @click="muatRincianPaket"
          >
            Coba Lagi
          </button>
        </div>
      </div>
    </main>

    <!-- State 3: Berhasil Memuat Rincian Paket (UC-03, FR-02) -->
    <main v-else-if="rincianBiaya" class="content-grid">
      <!-- Kolom Kiri: Galeri Hasil Kerja Placeholder -->
      <section class="kolom-kiri" aria-label="Galeri hasil kerja">
        <PaketGaleriPlaceholder />
      </section>

      <!-- Kolom Kanan: Rincian Info & Biaya Paket -->
      <section class="kolom-kanan" aria-label="Informasi dan rincian biaya paket">
        <!-- Judul & Kategori Paket -->
        <header class="paket-header">
          <div class="kategori-pill">
            <span>Kategori: Jasa Digital</span>
            <small class="badge-fase">(PBI-03)</small>
          </div>

          <h1 class="paket-title">Paket Jasa Digital #{{ rincianBiaya.packageId }}</h1>

          <!-- Harga Mulai Dari (Eksplisit Sesuai Mockup) -->
          <div class="harga-mulai-section">
            <span class="label-mulai">Mulai dari</span>
            <span class="harga-nominal">
              {{ formatRupiah(rincianBiaya.hargaMulai) }}
            </span>
          </div>
        </header>

        <!-- Kotak Ringkasan Revisi & Estimasi (2 Kolom) -->
        <div class="ringkasan-kotak-grid">
          <div class="kotak-info">
            <span class="kotak-label">Revisi</span>
            <strong class="kotak-nilai">3x Revisi</strong>
            <small class="kotak-sub">(Ketentuan PBI-20)</small>
          </div>

          <div class="kotak-info">
            <span class="kotak-label">Estimasi Pengerjaan</span>
            <strong class="kotak-nilai">
              {{
                formatRentangDurasi(
                  rincianBiaya.ringkasanEstimasi?.durasiTercepat,
                  rincianBiaya.ringkasanEstimasi?.durasiTerlama,
                )
              }}
            </strong>
            <small class="kotak-sub">(Berdasarkan varian aktif)</small>
          </div>
        </div>

        <!-- Syarat & Ketentuan Ringkas (Mockup Placeholder) -->
        <div class="syarat-ketentuan-box">
          <h2 class="section-heading">Syarat & Ketentuan Pengerjaan</h2>
          <ul class="syarat-list">
            <li>Pengerjaan dimulai segera setelah freelancer mengonfirmasi pesanan.</li>
            <li>Revisi mengikuti kuota paket dan hanya untuk konsep yang disepakati.</li>
            <li>File hasil kerja diserahkan dalam format digital standar platform.</li>
          </ul>
        </div>

        <!-- Baris Info Penyedia Jasa -->
        <div class="penyedia-row">
          <div class="penyedia-avatar" aria-hidden="true">👤</div>
          <div class="penyedia-info">
            <span class="penyedia-label">Penyedia Jasa:</span>
            <strong class="penyedia-nama">Freelancer Pemula / Mahasiswa</strong>
            <small class="penyedia-lokasi">Indonesia • Profil Terverifikasi</small>
          </div>
          <button
            type="button"
            class="btn-profil-disabled"
            disabled
            title="Modul profil freelancer belum tersedia"
          >
            Profil ›
          </button>
        </div>

        <!-- BAGIAN UTAMA PBI-04: Rincian Biaya Transparan & Varian -->
        <section class="section-rincian">
          <PaketRincianBiayaCard
            :varian="rincianBiaya.varian"
            :hargaMulai="rincianBiaya.hargaMulai"
          />
        </section>

        <!-- Tombol Aksi Transaksi (Placeholder Fase 2 Sesuai Mockup) -->
        <div class="transaksi-actions">
          <button
            type="button"
            class="btn-hubungi-disabled"
            disabled
            title="Fitur chat dan request jasa segera hadir pada fase berikutnya"
          >
            💬 Hubungi via Request (Segera Hadir)
          </button>

          <button
            type="button"
            class="btn-pesan-disabled"
            disabled
            title="Fitur transaksi dan pembayaran DP segera hadir pada fase berikutnya"
          >
            Pesan & Bayar DP 50% • Fase 2
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px 48px 16px;
  font-family: Arial, sans-serif;
  color: #1f2937;
}

.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.btn-back {
  background: transparent;
  border: none;
  color: #7c3aed;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  padding: 8px 0;
  display: inline-flex;
  align-items: center;
  min-height: 44px;
}

.btn-back:hover {
  text-decoration: underline;
}

.link-mode-freelancer {
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  text-decoration: none;
  background-color: #f3f4f6;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  display: inline-flex;
  align-items: center;
  min-height: 44px;
}

.link-mode-freelancer:hover {
  background-color: #e5e7eb;
  color: #111827;
}

.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  text-align: center;
}

.spinner {
  width: 44px;
  height: 44px;
  border: 4px solid #e5e7eb;
  border-top-color: #7c3aed;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-card {
  max-width: 480px;
  padding: 36px 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.error-icon {
  font-size: 44px;
  display: block;
  margin-bottom: 12px;
}

.error-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
}

.error-message {
  font-size: 14px;
  color: #4b5563;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-action-primary {
  padding: 12px 20px;
  background-color: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-action-primary:hover {
  background-color: #6d28d9;
}

.btn-action-secondary {
  padding: 12px 20px;
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-action-secondary:hover {
  background-color: #e5e7eb;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 36px;
  align-items: start;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.kolom-kanan {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.paket-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kategori-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #ede9fe;
  color: #6d28d9;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 9999px;
  width: fit-content;
}

.badge-fase {
  font-size: 11px;
  opacity: 0.8;
}

.paket-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
}

.harga-mulai-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 4px;
}

.label-mulai {
  font-size: 14px;
  color: #6b7280;
  font-weight: 600;
}

.harga-nominal {
  font-size: 24px;
  font-weight: 800;
  color: #7c3aed;
}

.ringkasan-kotak-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.kotak-info {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.kotak-label {
  font-size: 12px;
  color: #6b7280;
}

.kotak-nilai {
  font-size: 16px;
  color: #111827;
}

.kotak-sub {
  font-size: 11px;
  color: #9ca3af;
}

.syarat-ketentuan-box {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}

.section-heading {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 700;
  color: #374151;
}

.syarat-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

.penyedia-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.penyedia-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.penyedia-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.penyedia-label {
  font-size: 11px;
  color: #6b7280;
}

.penyedia-nama {
  font-size: 14px;
  color: #111827;
}

.penyedia-lokasi {
  font-size: 12px;
  color: #6b7280;
}

.btn-profil-disabled {
  background: transparent;
  border: none;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 600;
  cursor: not-allowed;
  padding: 8px 12px;
  min-height: 44px;
}

.section-rincian {
  width: 100%;
}

.transaksi-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.btn-hubungi-disabled {
  width: 100%;
  padding: 14px;
  background-color: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: not-allowed;
  min-height: 48px;
}

.btn-pesan-disabled {
  width: 100%;
  padding: 14px;
  background-color: #faf5ff;
  color: #7c3aed;
  border: 2px dashed #c4b5fd;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 800;
  cursor: not-allowed;
  min-height: 48px;
}
</style>
