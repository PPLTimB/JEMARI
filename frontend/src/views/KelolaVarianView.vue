<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  fetchVarianFreelancer,
  createVarianFreelancer,
  updateVarianFreelancer,
  deleteVarianFreelancer,
} from '../api/paketVarian'
import { getPaketToken, setPaketToken, clearPaketToken } from '../api/httpPaket'
import VarianTable from '../components/paket/VarianTable.vue'
import VarianFormModal from '../components/paket/VarianFormModal.vue'
import KonfirmasiHapusModal from '../components/paket/KonfirmasiHapusModal.vue'

const route = useRoute()
const paketId = computed(() => route.params.id)

const loading = ref(true)
const actionLoading = ref(false)
const varianList = ref([])

// Notifikasi & Error State
const alertSuccess = ref('')
const alertError = ref('')
const serverErrors = ref([])
const serverGeneralError = ref('')

// Modal Form State (Tambah / Edit)
const isFormModalOpen = ref(false)
const isEditMode = ref(false)
const currentVarian = ref(null)

// Modal Konfirmasi Hapus State
const isDeleteModalOpen = ref(false)
const varianToDelete = ref(null)

// Helper Token Pengujian
const showTokenHelper = ref(false)
const inputToken = ref(getPaketToken() || '')

async function loadVarian() {
  loading.value = true
  alertError.value = ''

  try {
    const data = await fetchVarianFreelancer(paketId.value)
    varianList.value = data || []
  } catch (err) {
    alertError.value = err.message || 'Gagal memuat daftar varian paket.'
  } finally {
    loading.value = false
  }
}

function openTambahModal() {
  isEditMode.value = false
  currentVarian.value = null
  serverErrors.value = []
  serverGeneralError.value = ''
  isFormModalOpen.value = true
}

function openEditModal(item) {
  isEditMode.value = true
  currentVarian.value = { ...item }
  serverErrors.value = []
  serverGeneralError.value = ''
  isFormModalOpen.value = true
}

function openDeleteModal(item) {
  varianToDelete.value = item
  isDeleteModalOpen.value = true
}

async function handleFormSubmit(payload) {
  actionLoading.value = true
  serverErrors.value = []
  serverGeneralError.value = ''

  try {
    if (isEditMode.value && currentVarian.value) {
      await updateVarianFreelancer(paketId.value, currentVarian.value.id, payload)
      alertSuccess.value = `Varian "${payload.name}" berhasil diperbarui!`
    } else {
      await createVarianFreelancer(paketId.value, payload)
      alertSuccess.value = `Varian "${payload.name}" berhasil ditambahkan!`
    }

    isFormModalOpen.value = false
    await loadVarian()

    // Sembunyikan alert sukses setelah 5 detik
    setTimeout(() => {
      alertSuccess.value = ''
    }, 5000)
  } catch (err) {
    if (err.errors && Array.isArray(err.errors) && err.errors.length > 0) {
      serverErrors.value = err.errors
      serverGeneralError.value =
        'Terdapat kolom yang tidak valid. Periksa penanda merah pada formulir.'
    } else {
      serverGeneralError.value = err.message || 'Gagal menyimpan varian.'
    }
  } finally {
    actionLoading.value = false
  }
}

async function handleConfirmDelete() {
  if (!varianToDelete.value) return

  actionLoading.value = true
  try {
    await deleteVarianFreelancer(paketId.value, varianToDelete.value.id)
    alertSuccess.value = `Varian "${varianToDelete.value.name}" berhasil dinonaktifkan.`
    isDeleteModalOpen.value = false
    varianToDelete.value = null
    await loadVarian()

    setTimeout(() => {
      alertSuccess.value = ''
    }, 5000)
  } catch (err) {
    alertError.value = err.message || 'Gagal menghapus varian.'
    isDeleteModalOpen.value = false
  } finally {
    actionLoading.value = false
  }
}

function handleSaveToken() {
  if (inputToken.value.trim()) {
    setPaketToken(inputToken.value.trim())
    alertSuccess.value = 'Token autentikasi berhasil disimpan di localStorage!'
  } else {
    clearPaketToken()
    alertSuccess.value = 'Token autentikasi dihapus dari localStorage.'
  }
  showTokenHelper.value = false
  loadVarian()
}

onMounted(() => {
  loadVarian()
})
</script>

<template>
  <div class="kelola-container">
    <!-- Navigasi & Helper Atas -->
    <header class="page-header">
      <div class="breadcrumb-row">
        <router-link :to="`/paket/${paketId}`" class="link-back">
          ← Lihat Halaman Detail Paket (UMKM)
        </router-link>

        <button type="button" class="btn-token-toggle" @click="showTokenHelper = !showTokenHelper">
          🔑 {{ getPaketToken() ? 'Token Terpasang' : 'Pasang Token Uji' }}
        </button>
      </div>

      <!-- Panel Pasang Token Uji (Bila backend memerlukan token JWT Freelancer) -->
      <div v-if="showTokenHelper" class="token-panel">
        <label for="token-input" class="token-label">
          Token JWT Freelancer (disimpan di key localStorage <code>jemari_auth_token</code>):
        </label>
        <div class="token-input-row">
          <input
            id="token-input"
            v-model="inputToken"
            type="text"
            placeholder="Tempel token Bearer JWT di sini..."
          />
          <button type="button" class="btn-token-save" @click="handleSaveToken">Simpan</button>
        </div>
        <small class="token-hint">
          Digunakan untuk pengujian endpoint <code>/api/freelancer/**</code> yang memerlukan role
          FREELANCER.
        </small>
      </div>

      <div class="header-main">
        <div>
          <p class="subtitle">JEMARI • FREELANCER (UC-18)</p>
          <h1 class="page-title">Kelola Varian Paket #{{ paketId }}</h1>
          <p class="page-desc">
            Atur pilihan varian harga dan estimasi durasi pengerjaan agar calon pembeli UMKM
            memahami rincian biaya.
          </p>
        </div>

        <button type="button" class="btn-tambah" @click="openTambahModal">
          + Tambah Varian Baru
        </button>
      </div>
    </header>

    <!-- Notifikasi Sukses -->
    <div v-if="alertSuccess" class="alert alert-success" role="status">✓ {{ alertSuccess }}</div>

    <!-- Notifikasi Error Utama -->
    <div v-if="alertError" class="alert alert-danger" role="alert">
      <div class="alert-content">
        <span>⚠️ {{ alertError }}</span>
        <button type="button" class="btn-retry" @click="loadVarian">Coba Lagi</button>
      </div>
    </div>

    <!-- Konten Utama: Loading atau Tabel Varian -->
    <main class="card-main">
      <div class="card-header">
        <h2 class="card-title">Daftar Varian Aktif</h2>
        <span class="badge-count">{{ varianList.length }} varian aktif</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="spinner" aria-hidden="true"></div>
        <p>Memuat daftar varian...</p>
      </div>

      <div v-else>
        <VarianTable :varianList="varianList" @edit="openEditModal" @hapus="openDeleteModal" />
      </div>

      <footer class="card-footer-note">
        <span class="icon-info" aria-hidden="true">💡</span>
        <p>
          <strong>Aturan Bisnis (UC-18):</strong> Perubahan harga atau durasi hanya berlaku untuk
          pemesanan baru. Pesanan yang sedang berjalan tetap menggunakan ketentuan awal saat pesanan
          dibuat.
        </p>
      </footer>
    </main>

    <!-- Modal Form Tambah / Edit Varian -->
    <VarianFormModal
      :isOpen="isFormModalOpen"
      :isEdit="isEditMode"
      :initialData="currentVarian"
      :serverErrors="serverErrors"
      :serverGeneralError="serverGeneralError"
      :isLoading="actionLoading"
      @close="isFormModalOpen = false"
      @submit="handleFormSubmit"
    />

    <!-- Modal Konfirmasi Hapus Varian -->
    <KonfirmasiHapusModal
      :isOpen="isDeleteModalOpen"
      :varianItem="varianToDelete"
      :isLoading="actionLoading"
      @close="isDeleteModalOpen = false"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<style scoped>
.kelola-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 16px 64px 16px;
  font-family: Arial, sans-serif;
  color: #1f2937;
}

.page-header {
  margin-bottom: 24px;
}

.breadcrumb-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.link-back {
  color: #7c3aed;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  min-height: 44px;
}

.link-back:hover {
  text-decoration: underline;
}

.btn-token-toggle {
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  min-height: 44px;
}

.btn-token-toggle:hover {
  background-color: #e5e7eb;
}

.token-panel {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.token-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.token-input-row {
  display: flex;
  gap: 8px;
}

.token-input-row input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  min-height: 44px;
}

.btn-token-save {
  padding: 10px 18px;
  background-color: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-token-save:hover {
  background-color: #6d28d9;
}

.token-hint {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-top: 6px;
}

.header-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.subtitle {
  color: #7c3aed;
  font-weight: bold;
  font-size: 13px;
  letter-spacing: 1px;
  margin: 0 0 4px 0;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 26px;
  font-weight: 800;
  color: #111827;
}

.page-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
  max-width: 600px;
  line-height: 1.4;
}

.btn-tambah {
  padding: 12px 20px;
  background-color: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  transition: background-color 0.15s ease;
}

.btn-tambah:hover {
  background-color: #6d28d9;
}

.alert {
  padding: 14px 18px;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 20px;
}

.alert-success {
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #15803d;
  font-weight: 600;
}

.alert-danger {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
}

.alert-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-retry {
  padding: 6px 12px;
  background-color: #dc2626;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  min-height: 36px;
}

.card-main {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.badge-count {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 4px 10px;
  border-radius: 9999px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  color: #6b7280;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e5e7eb;
  border-top-color: #7c3aed;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.card-footer-note {
  margin-top: 24px;
  padding: 14px 16px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.card-footer-note .icon-info {
  font-size: 16px;
  flex-shrink: 0;
}

.card-footer-note p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.4;
}
</style>
