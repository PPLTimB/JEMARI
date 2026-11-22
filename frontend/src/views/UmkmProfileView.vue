<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { umkmProfileService } from '../services/umkmProfileService'

const API_ORIGIN = (
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
).replace(/\/api\/?$/, '')

const tokenInput = ref(localStorage.getItem('jemari_token') || '')
const showTokenBar = ref(true)

const isLoading = ref(false)
const isSaving = ref(false)
const isDeactivating = ref(false)
const isUploadingLogo = ref(false)
const isDeletingLogo = ref(false)

const profileExists = ref(false)
const logoFileName = ref('')
const logoInput = ref(null)

const alertMessage = ref('')
const alertType = ref('info')

const profileMeta = reactive({
  id: null,
  active: true,
  createdAt: null,
  updatedAt: null,
})

const form = reactive({
  businessName: '',
  description: '',
  category: '',
  phoneNumber: '',
  contactEmail: '',
})

const categories = [
  'Makanan dan Minuman',
  'Fashion',
  'Kerajinan',
  'Kecantikan',
  'Pertanian',
  'Jasa',
  'Lainnya',
]

const initials = computed(() => {
  if (!form.businessName) return 'UM'

  return form.businessName
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase() || '')
    .join('')
})

const logoUrl = computed(() => {
  if (!logoFileName.value) return ''

  return `${API_ORIGIN}/uploads/${encodeURIComponent(logoFileName.value)}`
})

function showAlert(type, message) {
  alertType.value = type
  alertMessage.value = message
}

function saveTokenAndLoad() {
  tokenInput.value = localStorage.getItem('jemari_token') || ''

  if (!tokenInput.value) {
    showAlert('error', 'Token login tidak ditemukan. Silakan login kembali.')
    return
  }

  loadProfile()
}

async function loadProfile() {
  const token = localStorage.getItem('jemari_token')

  if (!token) {
    showAlert('error', 'Token login tidak ditemukan. Silakan login kembali.')
    return
  }

  isLoading.value = true
  alertMessage.value = ''

  try {
    const res = await umkmProfileService.getMyProfile()
    const data = res.data

    profileExists.value = true

    form.businessName = data.businessName || ''
    form.description = data.description || ''
    form.category = data.category || ''
    form.phoneNumber = data.phoneNumber || ''
    form.contactEmail = data.contactEmail || ''

    logoFileName.value = data.logoUrl || ''

    profileMeta.id = data.id
    profileMeta.active = data.active
    profileMeta.createdAt = data.createdAt
    profileMeta.updatedAt = data.updatedAt
  } catch (err) {
    if (err.status === 404) {
      profileExists.value = false
      logoFileName.value = ''
      profileMeta.id = null
      profileMeta.active = false

      showAlert('info', 'Profil usaha belum dibuat. Silakan lengkapi formulir.')
    } else if (err.status === 401 || err.status === 403) {
      showAlert('error', 'Token tidak valid atau bukan akun dengan role UMKM.')
    } else {
      showAlert('error', err.message || 'Gagal memuat profil usaha.')
    }
  } finally {
    isLoading.value = false
  }
}

async function handleSubmit() {
  isSaving.value = true
  alertMessage.value = ''

  const payload = {
    businessName: form.businessName,
    description: form.description,
    category: form.category,
    phoneNumber: form.phoneNumber,
    contactEmail: form.contactEmail,
  }

  try {
    const res = profileExists.value
      ? await umkmProfileService.updateMyProfile(payload)
      : await umkmProfileService.createMyProfile(payload)

    const data = res.data

    profileExists.value = true
    logoFileName.value = data.logoUrl || logoFileName.value

    profileMeta.id = data.id
    profileMeta.active = data.active
    profileMeta.createdAt = data.createdAt
    profileMeta.updatedAt = data.updatedAt

    showAlert('success', res.message || 'Profil usaha berhasil disimpan.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal menyimpan profil usaha.')
  } finally {
    isSaving.value = false
  }
}

function openLogoPicker() {
  if (!profileExists.value) {
    showAlert('info', 'Simpan profil usaha terlebih dahulu sebelum mengunggah logo.')
    return
  }

  logoInput.value?.click()
}

async function handleLogoChange(event) {
  const file = event.target.files?.[0]

  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  const maxSize = 5 * 1024 * 1024

  if (!allowedTypes.includes(file.type)) {
    showAlert('error', 'Format logo harus JPG, PNG, atau WEBP.')
    event.target.value = ''
    return
  }

  if (file.size > maxSize) {
    showAlert('error', 'Ukuran logo maksimal 5 MB.')
    event.target.value = ''
    return
  }

  isUploadingLogo.value = true
  alertMessage.value = ''

  try {
    const res = await umkmProfileService.uploadLogo(file)

    logoFileName.value = res.data.logoUrl || ''

    profileMeta.updatedAt = res.data.updatedAt

    showAlert('success', res.message || 'Logo berhasil diunggah.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal mengunggah logo.')
  } finally {
    isUploadingLogo.value = false
    event.target.value = ''
  }
}

async function handleDeleteLogo() {
  const confirmed = window.confirm(
    'Apakah Anda yakin ingin menghapus logo usaha ini?',
  )

  if (!confirmed) return

  isDeletingLogo.value = true
  alertMessage.value = ''

  try {
    const res = await umkmProfileService.deleteLogo()

    logoFileName.value = ''
    profileMeta.updatedAt = res.data.updatedAt

    showAlert('success', res.message || 'Logo berhasil dihapus.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal menghapus logo.')
  } finally {
    isDeletingLogo.value = false
  }
}

async function handleDeactivate() {
  const confirmed = window.confirm(
    'Apakah Anda yakin ingin menonaktifkan profil usaha ini?',
  )

  if (!confirmed) return

  isDeactivating.value = true
  alertMessage.value = ''

  try {
    const res = await umkmProfileService.deactivateMyProfile()

    profileMeta.active = false
    profileExists.value = false
    logoFileName.value = ''

    showAlert('success', res.message || 'Profil usaha berhasil dinonaktifkan.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal menonaktifkan profil usaha.')
  } finally {
    isDeactivating.value = false
  }
}

onMounted(() => {
  if (tokenInput.value) {
    loadProfile()
  }
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="topbar-inner">
        <a href="/" class="brand-logo">JEMARI</a>
        <span class="header-label">Profil Usaha UMKM</span>
      </div>
    </header>

    <div v-if="showTokenBar" class="dev-banner">
      <div class="dev-banner-inner">
        <div>
          <strong class="dev-title">Koneksi API Backend • Role UMKM</strong>
          <p class="dev-desc">
            Masukkan token JWT akun UMKM untuk menghubungkan profil dengan database.
          </p>
        </div>

        <div class="dev-controls">
          <input
            v-model="tokenInput"
            type="password"
            class="token-input"
            placeholder="Masukkan token JWT"
          />
          <button
            type="button"
            class="btn-primary"
            :disabled="isLoading"
            @click="saveTokenAndLoad"
          >
            {{ isLoading ? 'Memuat...' : 'Muat Profil' }}
          </button>
        </div>
      </div>
    </div>

    <main class="main-container">
      <div class="page-heading">
        <div>
          <h1>Profil Usaha Saya</h1>
          <p>Kelola informasi usaha UMKM Anda di platform JEMARI.</p>
        </div>

        <span
          :class="['status-badge', profileMeta.active ? 'active' : 'inactive']"
        >
          {{ profileMeta.active ? 'Aktif' : 'Belum Aktif' }}
        </span>
      </div>

      <div v-if="alertMessage" :class="['alert-box', `alert-${alertType}`]">
        {{ alertMessage }}
      </div>

      <section class="profile-card">
        <div class="profile-header">
          <div class="logo-section">
            <img
              v-if="logoUrl"
              :src="logoUrl"
              alt="Logo usaha"
              class="business-avatar logo-image"
            />

            <div v-else class="business-avatar">
              {{ initials }}
            </div>

            <div class="logo-actions">
              <input
                ref="logoInput"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                class="hidden-file-input"
                @change="handleLogoChange"
              />

              <button
                type="button"
                class="btn-logo"
                :disabled="!profileExists || isUploadingLogo || isDeletingLogo"
                @click="openLogoPicker"
              >
                {{
                  isUploadingLogo
                    ? 'Mengunggah...'
                    : logoFileName
                      ? 'Ganti Logo'
                      : 'Upload Logo'
                }}
              </button>

              <button
                v-if="logoFileName"
                type="button"
                class="btn-remove-logo"
                :disabled="isDeletingLogo || isUploadingLogo"
                @click="handleDeleteLogo"
              >
                {{ isDeletingLogo ? 'Menghapus...' : 'Hapus Logo' }}
              </button>

              <small>JPG, PNG, atau WEBP. Maksimal 5 MB.</small>
            </div>
          </div>

          <div class="business-info">
            <h2>{{ form.businessName || 'Nama Usaha UMKM' }}</h2>
            <p>{{ form.category || 'Kategori usaha belum dipilih' }}</p>
            <small v-if="profileMeta.id">ID Profil #{{ profileMeta.id }}</small>
          </div>
        </div>

        <form class="profile-form" @submit.prevent="handleSubmit">
          <div class="field-group">
            <label for="businessName">Nama Usaha <span>*</span></label>
            <input
              id="businessName"
              v-model="form.businessName"
              type="text"
              maxlength="150"
              required
              placeholder="Contoh: Kedai Kopi Nusantara"
            />
          </div>

          <div class="field-group">
            <label for="description">Deskripsi Usaha <span>*</span></label>
            <textarea
              id="description"
              v-model="form.description"
              rows="4"
              maxlength="2000"
              required
              placeholder="Ceritakan tentang usaha Anda..."
            ></textarea>
          </div>

          <div class="field-group">
            <label for="category">Kategori Usaha <span>*</span></label>
            <select id="category" v-model="form.category" required>
              <option value="" disabled>Pilih kategori usaha</option>
              <option v-for="item in categories" :key="item" :value="item">
                {{ item }}
              </option>
            </select>
          </div>

          <div class="two-col">
            <div class="field-group">
              <label for="phoneNumber">Nomor Telepon <span>*</span></label>
              <input
                id="phoneNumber"
                v-model="form.phoneNumber"
                type="tel"
                maxlength="20"
                required
                placeholder="081234567890"
              />
            </div>

            <div class="field-group">
              <label for="contactEmail">Email Kontak <span>*</span></label>
              <input
                id="contactEmail"
                v-model="form.contactEmail"
                type="email"
                maxlength="150"
                required
                placeholder="usaha@example.com"
              />
            </div>
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="btn-primary"
              :disabled="isSaving || isLoading || isDeactivating"
            >
              {{
                isSaving
                  ? 'Menyimpan...'
                  : profileExists
                    ? 'Simpan Perubahan'
                    : 'Buat Profil Usaha'
              }}
            </button>

            <button
              v-if="profileExists && profileMeta.active"
              type="button"
              class="btn-danger"
              :disabled="isDeactivating || isSaving"
              @click="handleDeactivate"
            >
              {{ isDeactivating ? 'Memproses...' : 'Nonaktifkan Profil' }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f8fafb;
  color: #111827;
  font-family: 'Inter', system-ui, sans-serif;
}

.topbar {
  background: white;
  border-bottom: 1px solid #eef0f3;
}

.topbar-inner {
  max-width: 1100px;
  margin: auto;
  padding: 1rem 1.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand-logo {
  color: #166534;
  font-size: 1.35rem;
  font-weight: 800;
  text-decoration: none;
}

.header-label {
  color: #6b7280;
  font-size: 0.9rem;
}

.dev-banner {
  background: #edf7f1;
  border-bottom: 1px solid #d1e7da;
}

.dev-banner-inner {
  max-width: 1100px;
  margin: auto;
  padding: 1rem 1.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.dev-title {
  color: #14532d;
  font-size: 0.9rem;
}

.dev-desc {
  margin: 0.3rem 0 0;
  color: #6b7280;
  font-size: 0.8rem;
}

.dev-controls {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  max-width: 550px;
}

.token-input {
  flex: 1;
  min-width: 0;
  padding: 0.65rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
}

.main-container {
  max-width: 1000px;
  margin: 2rem auto;
  padding: 0 1.5rem;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.page-heading h1 {
  margin: 0 0 0.4rem;
  font-size: 1.6rem;
  font-weight: 800;
}

.page-heading p {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}

.status-badge {
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.profile-card {
  padding: 1.75rem;
  background: white;
  border: 1px solid #eef0f3;
  border-radius: 14px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.75rem;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  flex-shrink: 0;
}

.business-avatar {
  width: 65px;
  height: 65px;
  flex-shrink: 0;
  border-radius: 14px;
  background: #166534;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.3rem;
  font-weight: 800;
}

.logo-image {
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.logo-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.35rem;
}

.logo-actions small {
  color: #6b7280;
  font-size: 0.72rem;
}

.hidden-file-input {
  display: none;
}

.btn-logo,
.btn-remove-logo {
  padding: 0.45rem 0.7rem;
  border-radius: 7px;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
}

.btn-logo {
  background: #edf7f1;
  border: 1px solid #bbdec9;
  color: #166534;
}

.btn-remove-logo {
  background: #fff1f2;
  border: 1px solid #fecdd3;
  color: #be123c;
}

.btn-logo:disabled,
.btn-remove-logo:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.business-info h2 {
  margin: 0 0 0.3rem;
  font-size: 1.15rem;
}

.business-info p,
.business-info small {
  margin: 0;
  color: #6b7280;
  font-size: 0.85rem;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-group label {
  font-size: 0.86rem;
  font-weight: 600;
}

.field-group label span {
  color: #dc2626;
}

.field-group input,
.field-group textarea,
.field-group select {
  width: 100%;
  box-sizing: border-box;
  padding: 0.7rem 0.85rem;
  border: 1px solid #d1d5db;
  border-radius: 9px;
  font: inherit;
  font-size: 0.9rem;
  background: white;
}

.field-group input:focus,
.field-group textarea:focus,
.field-group select:focus {
  outline: none;
  border-color: #166534;
  box-shadow: 0 0 0 3px rgba(22, 101, 52, 0.12);
}

.field-group textarea {
  resize: vertical;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.btn-primary,
.btn-danger {
  padding: 0.7rem 1.2rem;
  border: none;
  border-radius: 9px;
  font-size: 0.88rem;
  font-weight: 700;
  cursor: pointer;
}

.btn-primary {
  background: #166534;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #14532d;
}

.btn-danger {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert-box {
  padding: 0.85rem 1rem;
  margin-bottom: 1rem;
  border-radius: 9px;
  font-size: 0.88rem;
}

.alert-success {
  background: #dcfce7;
  color: #166534;
}

.alert-error {
  background: #fee2e2;
  color: #991b1b;
}

.alert-info {
  background: #fef3c7;
  color: #92400e;
}

@media (max-width: 640px) {
  .two-col {
    grid-template-columns: 1fr;
  }

  .page-heading {
    align-items: flex-start;
  }

  .dev-controls {
    max-width: 100%;
  }

  .profile-card {
    padding: 1.1rem;
  }

  .profile-header {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .logo-section {
    flex-wrap: wrap;
  }
}
</style>