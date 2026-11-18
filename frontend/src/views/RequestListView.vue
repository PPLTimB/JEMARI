<template>
  <div class="request-page">
    <div class="container">
      
      <!-- Header Halaman -->
      <div class="page-header">
        <div>
          <h1 class="page-title">Request Saya</h1>
          <p class="page-subtitle">Pantau perkembangan kebutuhan jasa dan kolaborasi dengan freelancer</p>
        </div>
        <router-link to="/request/buat" class="btn btn-primary">
          + Buat Request Baru
        </router-link>
      </div>

      <!-- Notifikasi Alert -->
      <div v-if="alertMessage" :class="['alert', alertType === 'error' ? 'alert-error' : 'alert-success']">
        <span>{{ alertMessage }}</span>
        <button class="alert-close" @click="alertMessage = ''">&times;</button>
      </div>

      <!-- Tab Filter Status (Mockup 4.5.5) -->
      <div class="status-tabs">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          :class="['tab-btn', { active: activeFilter === tab.value }]"
          @click="activeFilter = tab.value"
        >
          {{ tab.label }}
          <span class="tab-count">{{ countByStatus(tab.value) }}</span>
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>Memuat daftar request...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredRequests.length === 0" class="empty-state">
        <div class="empty-icon">&#128221;</div>
        <h3>Tidak ada request ditemukan</h3>
        <p>
          {{ activeFilter === 'ALL'
            ? 'Anda belum pernah membuat posting kebutuhan jasa. Klik tombol di bawah untuk membuat request pertama Anda.'
            : 'Tidak ada request pada kategori status ini.' }}
        </p>
        <router-link to="/request/buat" class="btn btn-primary">
          + Buat Request Sekarang
        </router-link>
      </div>

      <!-- Daftar Kartu Request (Mockup 4.5.5) -->
      <div v-else class="request-grid">
        <div v-for="req in filteredRequests" :key="req.id" class="request-card">
          
          <!-- Stepper Indikator 3 Tahap (Dibuka -> Diproses -> Selesai) -->
          <div class="stepper-track">
            <div :class="['step-node', isStepActive(req.status, 'OPEN') ? 'step-done' : '']">
              <span class="step-dot"></span>
              <span class="step-label">Dibuka</span>
            </div>
            <div class="step-line" :class="{ 'line-active': isLineActive(req.status, 2) }"></div>
            <div :class="['step-node', isStepActive(req.status, 'IN_PROGRESS') ? 'step-done' : '']">
              <span class="step-dot"></span>
              <span class="step-label">Diproses</span>
            </div>
            <div class="step-line" :class="{ 'line-active': isLineActive(req.status, 3) }"></div>
            <div :class="['step-node', isStepActive(req.status, 'COMPLETED') ? 'step-done' : (req.status === 'CLOSED' ? 'step-closed' : '')]">
              <span class="step-dot"></span>
              <span class="step-label">{{ req.status === 'CLOSED' ? 'Ditutup' : 'Selesai' }}</span>
            </div>
          </div>

          <!-- Informasi Kartu -->
          <div class="card-content">
            <div class="card-meta">
              <span class="category-badge">{{ req.kategori || req.category }}</span>
              <span class="deadline-text">&#128197; Tenggat: {{ formatDate(req.deadline) }}</span>
            </div>

            <h3 class="card-title">
              <router-link :to="`/request/${req.id}`" class="title-link">
                {{ req.judul || req.title }}
              </router-link>
            </h3>

            <p class="card-desc">{{ req.deskripsi || req.description }}</p>

            <div class="card-info-row">
              <div class="info-group">
                <span class="info-label">Anggaran:</span>
                <span class="info-value price-text">{{ formatRupiah(req.harga || req.budget) }}</span>
              </div>

              <!-- Keterangan Status / Pelamar -->
              <div class="info-group">
                <span class="info-label">Status Terkini:</span>
                <div v-if="req.status === 'OPEN'" class="applicant-info">
                  <span class="badge-applicant">&#128101; {{ req.pelamarCount || 0 }} pelamar</span>
                </div>
                <div v-else-if="req.status === 'IN_PROGRESS'" class="freelancer-info">
                  <span class="freelancer-name">&#9881; Dikerjakan: {{ req.freelancerName || 'Freelancer Terpilih' }}</span>
                </div>
                <div v-else-if="req.status === 'COMPLETED'" class="completed-info">
                  <span class="badge-completed">&#10003; Proyek Selesai</span>
                </div>
                <div v-else class="closed-info">
                  <span class="badge-closed">Request Ditutup</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Aksi Kartu (Sesuai Mockup 4.5.5) -->
          <div class="card-actions">
            <router-link :to="`/request/${req.id}`" class="btn btn-outline">
              Detail &raquo;
            </router-link>

            <!-- Aksi Jika Berstatus Dibuka: Edit & Tutup -->
            <template v-if="req.status === 'OPEN'">
              <router-link :to="`/request/edit/${req.id}`" class="btn btn-secondary">
                Ubah
              </router-link>
              <button class="btn btn-danger-outline" @click="konfirmasiTutup(req)">
                Tutup
              </button>
            </template>

            <!-- Aksi Jika Selesai: Beri Review (Fase 2) -->
            <template v-else-if="req.status === 'COMPLETED'">
              <button class="btn btn-fase2" @click="alertFase2">
                Beri Review (Fase 2)
              </button>
            </template>
          </div>

        </div>
      </div>

      <!-- Modal Konfirmasi Tutup Request -->
      <div v-if="modalTutup.show" class="modal-backdrop" @click.self="modalTutup.show = false">
        <div class="modal-card">
          <h3>Tutup Request Kebutuhan?</h3>
          <p>
            Apakah Anda yakin ingin menutup request <strong>"{{ modalTutup.item?.judul || modalTutup.item?.title }}"</strong>?
            Setelah ditutup, request tidak akan menerima lamaran baru dari freelancer.
          </p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="modalTutup.show = false">Batal</button>
            <button class="btn btn-danger" @click="eksekusiTutupRequest">Ya, Tutup Request</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { requestService } from '../services/requestService'

const requests = ref([])
const isLoading = ref(true)
const activeFilter = ref('ALL')
const alertMessage = ref('')
const alertType = ref('success')

const modalTutup = ref({
  show: false,
  item: null
})

const filterTabs = [
  { label: 'Semua', value: 'ALL' },
  { label: 'Dibuka', value: 'OPEN' },
  { label: 'Diproses', value: 'IN_PROGRESS' },
  { label: 'Selesai', value: 'COMPLETED' }
]

async function loadData() {
  isLoading.value = true
  try {
    requests.value = await requestService.getMyRequests()
  } catch (err) {
    alertType.value = 'error'
    alertMessage.value = 'Gagal memuat data request: ' + err.message
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadData()
})

const filteredRequests = computed(() => {
  if (activeFilter.value === 'ALL') return requests.value
  if (activeFilter.value === 'COMPLETED') {
    return requests.value.filter((r) => r.status === 'COMPLETED' || r.status === 'CLOSED')
  }
  return requests.value.filter((r) => r.status === activeFilter.value)
})

function countByStatus(status) {
  if (status === 'ALL') return requests.value.length
  if (status === 'COMPLETED') {
    return requests.value.filter((r) => r.status === 'COMPLETED' || r.status === 'CLOSED').length
  }
  return requests.value.filter((r) => r.status === status).length
}

function isStepActive(currentStatus, targetStep) {
  if (currentStatus === 'OPEN') return targetStep === 'OPEN'
  if (currentStatus === 'IN_PROGRESS') return targetStep === 'OPEN' || targetStep === 'IN_PROGRESS'
  if (currentStatus === 'COMPLETED') return true
  if (currentStatus === 'CLOSED') return targetStep === 'OPEN' || targetStep === 'CLOSED'
  return false
}

function isLineActive(currentStatus, lineNumber) {
  if (lineNumber === 2) {
    return currentStatus === 'IN_PROGRESS' || currentStatus === 'COMPLETED'
  }
  if (lineNumber === 3) {
    return currentStatus === 'COMPLETED'
  }
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
    return d.toLocaleDateString('id-ID', { year: 'numeric', month: 'short', day: 'numeric' })
  } catch {
    return dateStr
  }
}

function konfirmasiTutup(req) {
  modalTutup.value = {
    show: true,
    item: req
  }
}

async function eksekusiTutupRequest() {
  if (!modalTutup.value.item) return
  const id = modalTutup.value.item.id
  try {
    await requestService.closeRequest(id)
    alertType.value = 'success'
    alertMessage.value = 'Request berhasil ditutup dan tidak lagi menerima pelamar.'
    modalTutup.value.show = false
    await loadData()
  } catch (err) {
    alertType.value = 'error'
    alertMessage.value = 'Gagal menutup request: ' + err.message
  }
}

function alertFase2() {
  alert('Fitur Ulasan dan Rating terverifikasi akan aktif pada Fase 2 (Sprint 2 / UC-16).')
}
</script>

<style scoped>
.request-page {
  padding: 32px 16px 64px 16px;
  background-color: #f9fafb;
  min-height: calc(100vh - 65px);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* Tombol */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  border: 1px solid transparent;
  transition: all 0.2s;
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

.btn-outline {
  background-color: #ffffff;
  color: #2563eb;
  border-color: #bfdbfe;
}

.btn-outline:hover {
  background-color: #eff6ff;
}

.btn-danger-outline {
  background-color: #ffffff;
  color: #dc2626;
  border-color: #fecaca;
}

.btn-danger-outline:hover {
  background-color: #fee2e2;
}

.btn-danger {
  background-color: #dc2626;
  color: #ffffff;
}

.btn-danger:hover {
  background-color: #b91c1c;
}

.btn-fase2 {
  background-color: #f3f4f6;
  color: #9ca3af;
  border: 1px dashed #d1d5db;
  cursor: not-allowed;
}

/* Tabs Status */
.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
  overflow-x: auto;
}

.tab-btn {
  background: none;
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-btn:hover {
  background-color: #f3f4f6;
  color: #111827;
}

.tab-btn.active {
  background-color: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.tab-count {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 9999px;
  background-color: #e5e7eb;
  color: #374151;
}

.tab-btn.active .tab-count {
  background-color: #bfdbfe;
  color: #1d4ed8;
}

/* Alert */
.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.alert-success {
  background-color: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.alert-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: inherit;
}

/* Request Grid & Card */
.request-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.request-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.request-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* Stepper 3 Tahap */
.stepper-track {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f9fafb;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #f3f4f6;
}

.step-node {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #d1d5db;
}

.step-node.step-done {
  color: #1d4ed8;
  font-weight: 600;
}

.step-node.step-done .step-dot {
  background-color: #2563eb;
  box-shadow: 0 0 0 3px #dbeafe;
}

.step-node.step-closed {
  color: #6b7280;
}

.step-node.step-closed .step-dot {
  background-color: #9ca3af;
}

.step-line {
  flex: 1;
  height: 2px;
  background-color: #e5e7eb;
  margin: 0 12px;
}

.step-line.line-active {
  background-color: #2563eb;
}

/* Card Content */
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.category-badge {
  background-color: #eff6ff;
  color: #1d4ed8;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.deadline-text {
  font-size: 13px;
  color: #6b7280;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
}

.title-link {
  color: #111827;
  text-decoration: none;
}

.title-link:hover {
  color: #2563eb;
}

.card-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-info-row {
  display: flex;
  justify-content: space-between;
  background-color: #f8fafc;
  padding: 12px 14px;
  border-radius: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  text-transform: uppercase;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.price-text {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.badge-applicant {
  font-size: 13px;
  font-weight: 600;
  color: #0369a1;
  background-color: #e0f2fe;
  padding: 3px 8px;
  border-radius: 4px;
}

.freelancer-name {
  font-size: 13px;
  font-weight: 600;
  color: #b45309;
}

.badge-completed {
  font-size: 13px;
  font-weight: 600;
  color: #15803d;
}

.badge-closed {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

/* Card Actions */
.card-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
  flex-wrap: wrap;
}

/* Empty State */
.empty-state {
  text-align: center;
  background-color: #ffffff;
  border: 1px dashed #d1d5db;
  border-radius: 12px;
  padding: 48px 24px;
  margin-top: 24px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state h3 {
  font-size: 18px;
  color: #111827;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #6b7280;
  font-size: 14px;
  max-width: 440px;
  margin: 0 auto 20px auto;
  line-height: 1.5;
}

/* Loading Spinner */
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

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 100;
}

.modal-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 24px;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-card h3 {
  margin: 0 0 12px 0;
  color: #111827;
  font-size: 18px;
}

.modal-card p {
  color: #4b5563;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 20px 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
