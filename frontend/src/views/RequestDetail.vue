<template>
  <div class="detail-page">
    <div class="container">
      
      <!-- Navigasi Kembali -->
      <router-link to="/request-saya" class="back-link">
        &larr; Kembali ke Request Saya
      </router-link>

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Memuat detail request...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="alert alert-error">
        {{ errorMessage }}
      </div>

      <!-- Detail Request Card (Mockup 4.5.10) -->
      <div v-else-if="request" class="detail-card">
        
        <!-- Status Header & Stepper -->
        <div class="status-header">
          <div>
            <span class="badge-status" :class="`status-${request.status?.toLowerCase()}`">
              {{ request.statusLabel || (request.status === 'OPEN' ? 'Dibuka' : request.status) }}
            </span>
          </div>

          <div class="header-actions" v-if="request.status === 'OPEN'">
            <router-link :to="`/request/edit/${request.id}`" class="btn btn-secondary">
              Ubah Request
            </router-link>
            <button class="btn btn-danger-outline" @click="tutupRequest">
              Tutup Request
            </button>
          </div>
        </div>

        <!-- Stepper 3 Tahap Visual -->
        <div class="stepper-box">
          <div :class="['step-item', isDone('OPEN') ? 'active' : '']">
            <span class="step-num">1</span>
            <span class="step-text">Dibuka</span>
          </div>
          <div class="step-divider" :class="{ active: isLineDone(2) }"></div>
          <div :class="['step-item', isDone('IN_PROGRESS') ? 'active' : '']">
            <span class="step-num">2</span>
            <span class="step-text">Diproses</span>
          </div>
          <div class="step-divider" :class="{ active: isLineDone(3) }"></div>
          <div :class="['step-item', isDone('COMPLETED') ? 'active' : '']">
            <span class="step-num">3</span>
            <span class="step-text">{{ request.status === 'CLOSED' ? 'Ditutup' : 'Selesai' }}</span>
          </div>
        </div>

        <!-- Judul & Kategori -->
        <div class="main-info">
          <div class="meta-tags">
            <span class="tag-category">{{ request.kategori || request.category }}</span>
            <span class="meta-date">&#128197; Batas Waktu: <strong>{{ formatDate(request.deadline) }}</strong></span>
          </div>

          <h1 class="request-title">{{ request.judul || request.title }}</h1>
        </div>

        <!-- Highlight Anggaran -->
        <div class="budget-banner">
          <div class="budget-col">
            <span class="budget-label">Ekspektasi Anggaran UMKM</span>
            <span class="budget-value">{{ formatRupiah(request.harga || request.budget) }}</span>
          </div>
          <div class="budget-col" v-if="request.pelamarCount !== undefined">
            <span class="budget-label">Total Pelamar Freelancer</span>
            <span class="budget-value pelamar-text">&#128101; {{ request.pelamarCount }} Orang</span>
          </div>
        </div>

        <!-- Deskripsi Kebutuhan -->
        <div class="section-block">
          <h3 class="section-title">Deskripsi Kebutuhan:</h3>
          <div class="desc-content">
            {{ request.deskripsi || request.description }}
          </div>
        </div>

        <!-- Foto Referensi (PBI-12) -->
        <div class="section-block" v-if="referencePhotos.length > 0">
          <h3 class="section-title">Foto Referensi Produk:</h3>
          <div class="reference-photos-grid">
            <div v-for="photo in referencePhotos" :key="photo.id" class="reference-photo-item">
              <img :src="photo.photoUrl" :alt="photo.originalName || 'Foto Referensi'" class="reference-photo-img" />
              <p class="reference-photo-name">{{ photo.originalName }}</p>
            </div>
          </div>
        </div>

        <!-- Pelamar Section (Mockup 4.5.6 Preview) -->
        <div class="section-block applicant-section">
          <h3 class="section-title">
            Daftar Pelamar Freelancer ({{ pelamarList.length }})
          </h3>

          <div v-if="pelamarList.length === 0" class="empty-pelamar">
            <p>Belum ada freelancer yang mengajukan lamaran pada request ini.</p>
          </div>

          <div v-else class="pelamar-grid">
            <div v-for="pelamar in pelamarList" :key="pelamar.id" class="pelamar-card">
              <div class="pelamar-info">
                <strong>{{ pelamar.nama }}</strong>
                <span class="pelamar-bid">{{ formatRupiah(pelamar.tawaranHarga) }} &bull; {{ pelamar.durasi }}</span>
              </div>
              <button class="btn btn-primary btn-sm" @click="alertPilih(pelamar.nama)">
                Pilih Freelancer
              </button>
            </div>
          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requestService } from '../services/requestService'

const route = useRoute()
const router = useRouter()
const requestId = route.params.id

const request = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')

// Reference Photos (PBI-12)
const referencePhotos = ref([])
const isLoadingPhotos = ref(false)

// Dummy Pelamar sesuai Mockup 4.5.6 (Daftar Pelamar)
const pelamarList = ref([
  { id: 1, nama: 'Rina D.', tawaranHarga: 350000, durasi: '3 hari' },
  { id: 2, nama: 'Budi A.', tawaranHarga: 300000, durasi: '5 hari' },
  { id: 3, nama: 'Sari P.', tawaranHarga: 280000, durasi: '4 hari' }
])

onMounted(async () => {
  isLoading.value = true
  try {
    request.value = await requestService.getRequestById(requestId)
    // Load reference photos
    const photos = await requestService.getReferencePhotos(requestId)
    referencePhotos.value = photos || []
  } catch (err) {
    errorMessage.value = err.message
  } finally {
    isLoading.value = false
  }
})

function isDone(step) {
  if (!request.value) return false
  const s = request.value.status
  if (s === 'OPEN') return step === 'OPEN'
  if (s === 'IN_PROGRESS') return step === 'OPEN' || step === 'IN_PROGRESS'
  if (s === 'COMPLETED') return true
  if (s === 'CLOSED') return step === 'OPEN' || step === 'CLOSED'
  return false
}

function isLineDone(line) {
  if (!request.value) return false
  const s = request.value.status
  if (line === 2) return s === 'IN_PROGRESS' || s === 'COMPLETED'
  if (line === 3) return s === 'COMPLETED'
  return false
}

function formatRupiah(num) {
  if (!num) return 'Rp 0'
  return 'Rp ' + Number(num).toLocaleString('id-ID')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('id-ID', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch {
    return dateStr
  }
}

async function tutupRequest() {
  if (confirm('Tutup lowongan request ini? Freelancer tidak akan dapat melamar lagi.')) {
    try {
      await requestService.closeRequest(requestId)
      request.value = await requestService.getRequestById(requestId)
      alert('Request berhasil ditutup!')
    } catch (err) {
      alert('Gagal menutup request: ' + err.message)
    }
  }
}

function alertPilih(nama) {
  alert(`Memilih ${nama}. Pembuatan proyek resmi & pembayaran DP Escrow 50% akan diproses pada Fase 2 (Sprint 2 / UC-12).`)
}
</script>

<style scoped>
.detail-page {
  padding: 32px 16px 64px 16px;
  background-color: #f9fafb;
  min-height: calc(100vh - 65px);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.back-link {
  display: inline-block;
  margin-bottom: 16px;
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.back-link:hover {
  color: #1d4ed8;
}

.detail-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.badge-status {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 700;
}

.status-open {
  background-color: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.status-in_progress {
  background-color: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-completed {
  background-color: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-closed {
  background-color: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* Stepper Box */
.stepper-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  padding: 14px 20px;
  margin-bottom: 28px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.step-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #e2e8f0;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.step-item.active {
  color: #1d4ed8;
  font-weight: 700;
}

.step-item.active .step-num {
  background-color: #2563eb;
  color: #ffffff;
}

.step-divider {
  flex: 1;
  height: 2px;
  background-color: #e2e8f0;
  margin: 0 16px;
}

.step-divider.active {
  background-color: #2563eb;
}

/* Main Info */
.main-info {
  margin-bottom: 24px;
}

.meta-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.tag-category {
  background-color: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.meta-date {
  font-size: 13px;
  color: #6b7280;
}

.request-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
}

/* Budget Banner */
.budget-banner {
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.budget-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.budget-label {
  font-size: 12px;
  text-transform: uppercase;
  color: #1e40af;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.budget-value {
  font-size: 22px;
  font-weight: 800;
  color: #1e3a8a;
}

.pelamar-text {
  color: #0369a1;
}

/* Section Blocks */
.section-block {
  margin-bottom: 28px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 10px 0;
}

.desc-content {
  background-color: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  padding: 16px;
  color: #374151;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

/* Pelamar Section */
.applicant-section {
  border-top: 1px solid #f3f4f6;
  padding-top: 24px;
}

.pelamar-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pelamar-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
}

.pelamar-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pelamar-bid {
  font-size: 12px;
  color: #6b7280;
}

.empty-pelamar {
  padding: 16px;
  text-align: center;
  background-color: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
}

/* Tombol */
.btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background-color: #2563eb;
  color: #ffffff;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
}

.btn-danger-outline {
  background-color: #ffffff;
  color: #dc2626;
  border-color: #fecaca;
}

.btn-danger-outline:hover {
  background-color: #fee2e2;
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 16px;
  border-radius: 8px;
}

.loading-state {
  text-align: center;
  padding: 48px 0;
  color: #6b7280;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Reference Photos (PBI-12) */
.reference-photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.reference-photo-item {
  background-color: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.reference-photo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reference-photo-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background-color: #e5e7eb;
}

.reference-photo-name {
  padding: 8px 12px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>


<style scoped>
.detail-page {
  padding: 32px 16px 64px 16px;
  background-color: #f9fafb;
  min-height: calc(100vh - 65px);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.back-link {
  display: inline-block;
  margin-bottom: 16px;
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.back-link:hover {
  color: #1d4ed8;
}

.detail-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.badge-status {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 700;
}

.status-open {
  background-color: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.status-in_progress {
  background-color: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-completed {
  background-color: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-closed {
  background-color: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* Stepper Box */
.stepper-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  padding: 14px 20px;
  margin-bottom: 28px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.step-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #e2e8f0;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.step-item.active {
  color: #1d4ed8;
  font-weight: 700;
}

.step-item.active .step-num {
  background-color: #2563eb;
  color: #ffffff;
}

.step-divider {
  flex: 1;
  height: 2px;
  background-color: #e2e8f0;
  margin: 0 16px;
}

.step-divider.active {
  background-color: #2563eb;
}

/* Main Info */
.main-info {
  margin-bottom: 24px;
}

.meta-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.tag-category {
  background-color: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.meta-date {
  font-size: 13px;
  color: #6b7280;
}

.request-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
}

/* Budget Banner */
.budget-banner {
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.budget-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.budget-label {
  font-size: 12px;
  text-transform: uppercase;
  color: #1e40af;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.budget-value {
  font-size: 22px;
  font-weight: 800;
  color: #1e3a8a;
}

.pelamar-text {
  color: #0369a1;
}

/* Section Blocks */
.section-block {
  margin-bottom: 28px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 10px 0;
}

.desc-content {
  background-color: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  padding: 16px;
  color: #374151;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

/* Pelamar Section */
.applicant-section {
  border-top: 1px solid #f3f4f6;
  padding-top: 24px;
}

.pelamar-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pelamar-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
}

.pelamar-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pelamar-bid {
  font-size: 12px;
  color: #6b7280;
}

.empty-pelamar {
  padding: 16px;
  text-align: center;
  background-color: #f9fafb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
}

/* Tombol */
.btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background-color: #2563eb;
  color: #ffffff;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
}

.btn-danger-outline {
  background-color: #ffffff;
  color: #dc2626;
  border-color: #fecaca;
}

.btn-danger-outline:hover {
  background-color: #fee2e2;
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 16px;
  border-radius: 8px;
}

.loading-state {
  text-align: center;
  padding: 48px 0;
  color: #6b7280;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>