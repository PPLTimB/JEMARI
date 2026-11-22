<template>
  <div class="portfolio-page">
    <!-- Navbar Sesuai Mockup 4.5.7 (JEMARI | Profil | Cari Request | Paket Saya) -->
    <header class="app-header">
      <div class="header-container">
        <div class="brand-group">
          <router-link to="/portfolio" class="brand-title">JEMARI</router-link>
          <span class="brand-sub">Platform Kolaborasi UMKM &amp; Freelancer</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/portfolio" class="nav-link active">Profil &amp; Portofolio</router-link>
          <router-link to="/freelancer/profile" class="nav-link">Pengaturan Profil</router-link>
          <router-link to="/freelancer/packages" class="nav-link">Paket Saya</router-link>
        </nav>

        <div class="user-pill">
          <span class="pill-dot"></span>
          <span class="pill-label">Penyedia Jasa (Freelancer)</span>
        </div>
      </div>
    </header>

    <main class="page-content">
      <div class="container">

        <!-- Notifikasi Banner Alert -->
        <transition name="fade">
          <div v-if="alertMessage" :class="['alert-banner', alertType === 'error' ? 'alert-danger' : 'alert-success']">
            <span class="alert-icon">{{ alertType === 'error' ? '⚠️' : '✅' }}</span>
            <span class="alert-text">{{ alertMessage }}</span>
            <button class="alert-close-btn" @click="alertMessage = ''">&times;</button>
          </div>
        </transition>

        <!-- 1. Header Profil Freelancer (Sesuai Mockup 4.5.7: Foto, Nama, Badge "Baru", Bio, Kategori, Edit Profil) -->
        <section class="profile-header-card">
          <div class="profile-main-info">
            <div class="avatar-container">
              <img
                v-if="freelancer.avatarUrl"
                :src="freelancer.avatarUrl"
                :alt="freelancer.nama"
                class="avatar-image"
                @error="freelancer.avatarUrl = ''"
              />
              <div v-else class="avatar-initials">
                {{ getInitials(freelancer.nama) }}
              </div>
            </div>

            <div class="profile-details">
              <div class="name-badge-row">
                <h1 class="freelancer-name">{{ freelancer.nama }}</h1>
                <!-- Badge "Baru" Sesuai Mockup 4.5.7 Halaman 78 -->
                <span class="badge-baru" title="Talenta Baru JEMARI">Baru</span>
              </div>

              <div class="category-tag">
                <span class="category-icon">🎨</span>
                <span>{{ freelancer.kategori }}</span>
              </div>

              <p class="freelancer-bio">
                {{ freelancer.bio }}
              </p>
            </div>
          </div>

          <div class="profile-action-box">
            <button class="btn-edit-profile" @click="bukaModalEditProfil">
              <span>✎</span> Edit Profil
            </button>
          </div>
        </section>

        <!-- 2. Seksi Portofolio (Sesuai Mockup 4.5.7: Judul Portofolio & Tombol "+ Unggah") -->
        <section class="portfolio-section">
          <div class="section-top-bar">
            <div>
              <h2 class="section-heading">Portofolio</h2>
              <p class="section-subheading">
                Etalase digital resmi untuk memamerkan keahlian dan bukti kualitas kerja nyata kepada calon klien UMKM (FR-06 &amp; FR-03).
              </p>
            </div>

            <!-- Tombol "+ Unggah" Sesuai Mockup 4.5.7 -->
            <button class="btn-primary-upload" @click="bukaModalTambah">
              <span class="plus-sign">+</span> Unggah Karya
            </button>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="state-container">
            <div class="spinner"></div>
            <p>Memuat portofolio karya...</p>
          </div>

          <!-- Empty State (Sesuai Skenario UC-04: "belum ada portofolio") -->
          <div v-else-if="portfolios.length === 0" class="empty-portfolio-card">
            <div class="empty-icon-box">🖼️</div>
            <h3 class="empty-title">Belum ada portofolio</h3>
            <p class="empty-description">
              Unggah karya desain, foto produk, atau hasil kreasi terbaik Anda agar pelaku UMKM dapat memvalidasi kompetensi Anda.
            </p>
            <button class="btn-primary-upload mt-3" @click="bukaModalTambah">
              + Unggah Karya Pertama
            </button>
          </div>

          <!-- 3. Grid Galeri Portofolio Responsif -->
          <div v-else class="portfolio-grid">
            <article
              v-for="item in portfolios"
              :key="item.id"
              class="portfolio-card"
            >
              <!-- Area Gambar dengan Fitur Lightbox Preview (FR-03) -->
              <div class="card-media" @click="bukaLightbox(item)">
                <img
                  :src="item.imageUrl || item.gambarUrl"
                  :alt="item.title || item.judul"
                  class="card-img"
                  loading="lazy"
                />
                <div class="media-overlay" title="Klik untuk memperbesar gambar">
                  <span class="zoom-pill">🔍 Perbesar Gambar</span>
                </div>
              </div>

              <!-- Body Kartu -->
              <div class="card-content">
                <h3 class="card-title" :title="item.title || item.judul">
                  {{ item.title || item.judul }}
                </h3>
                <p class="card-desc">
                  {{ item.description || item.deskripsi || 'Tidak ada deskripsi.' }}
                </p>

                <!-- Tombol Aksi Kelola (Ubah & Hapus) Sesuai UC-17 -->
                <div class="card-footer">
                  <span class="card-date">{{ formatDate(item.createdAt) }}</span>
                  <div class="card-action-btns">
                    <button
                      class="btn-action btn-edit"
                      title="Ubah karya portofolio"
                      @click="bukaModalUbah(item)"
                    >
                      ✎ Ubah
                    </button>
                    <button
                      class="btn-action btn-delete"
                      title="Hapus karya portofolio"
                      @click="konfirmasiHapus(item)"
                    >
                      🗑 Hapus
                    </button>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </section>

      </div>
    </main>

    <!-- ======================================================== -->
    <!-- MODAL 1: FORM TAMBAH / UBAH KARYA (Skenario UC-17 & FR-06) -->
    <!-- ======================================================== -->
    <div v-if="modalKarya.isOpen" class="modal-backdrop" @click.self="tutupModalKarya">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3 class="modal-title">
            {{ modalKarya.isEdit ? 'Ubah Item Karya Portofolio' : 'Unggah Item Karya Portofolio' }}
          </h3>
          <button class="modal-close" @click="tutupModalKarya">&times;</button>
        </div>

        <form class="modal-body" @submit.prevent="simpanKarya">
          <!-- Pesan Error Validasi Form -->
          <div v-if="modalKarya.errorMessage" class="form-error-box">
            {{ modalKarya.errorMessage }}
          </div>

          <!-- Input Judul Karya (Wajib, Max 150 Karakter) -->
          <div class="form-group">
            <div class="label-row">
              <label for="karya-title" class="form-label">
                Judul Karya <span class="required-star">*</span>
              </label>
              <span class="char-counter" :class="{ 'char-limit': modalKarya.title.length > 150 }">
                {{ modalKarya.title.length }}/150
              </span>
            </div>
            <input
              id="karya-title"
              v-model="modalKarya.title"
              type="text"
              maxlength="150"
              required
              class="form-input"
              placeholder="Contoh: Desain Branding & Logo Kopi Nusantara"
            />
          </div>

          <!-- Input Deskripsi Karya (Opsional/Ringkas) -->
          <div class="form-group">
            <label for="karya-desc" class="form-label">Deskripsi Karya</label>
            <textarea
              id="karya-desc"
              v-model="modalKarya.description"
              rows="3"
              class="form-textarea"
              placeholder="Jelaskan konsep, proses kerja, atau hasil yang dicapai untuk UMKM..."
            ></textarea>
          </div>

          <!-- Input Berkas Media Gambar (Format JPG/PNG/WebP, Maks 5 MB) -->
          <div class="form-group">
            <label class="form-label">
              Gambar Hasil Karya <span class="required-star">*</span>
              <span class="file-note">(JPG, PNG, atau WebP &bull; Maks. 5 MB)</span>
            </label>

            <!-- Mode Pilihan Berkas: Unggah File / Masukkan URL -->
            <div class="file-mode-switch">
              <button
                type="button"
                :class="['switch-btn', modalKarya.imageMode === 'upload' ? 'active' : '']"
                @click="modalKarya.imageMode = 'upload'"
              >
                📁 Unggah Berkas
              </button>
              <button
                type="button"
                :class="['switch-btn', modalKarya.imageMode === 'url' ? 'active' : '']"
                @click="modalKarya.imageMode = 'url'"
              >
                🔗 Tautan Gambar
              </button>
            </div>

            <!-- Mode Unggah Berkas Dari Komputer -->
            <div v-if="modalKarya.imageMode === 'upload'" class="upload-dropzone">
              <input
                id="file-input"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                class="file-hidden-input"
                @change="handleFileChange"
              />
              <label for="file-input" class="dropzone-label">
                <span class="dropzone-icon">📷</span>
                <span class="dropzone-text">Pilih berkas gambar atau seret ke sini</span>
                <span class="dropzone-sub">Maksimal 5 MB per berkas</span>
              </label>
            </div>

            <!-- Mode Masukkan Tautan URL -->
            <div v-else class="url-input-box">
              <input
                v-model="modalKarya.imageUrl"
                type="url"
                class="form-input"
                placeholder="https://example.com/foto-karya.jpg"
              />
            </div>

            <!-- Pratinjau Gambar Real-Time -->
            <div v-if="modalKarya.imageUrl" class="image-preview-wrapper">
              <p class="preview-label">Pratinjau Gambar:</p>
              <div class="preview-box">
                <img :src="modalKarya.imageUrl" alt="Preview" class="preview-img" />
                <button type="button" class="btn-remove-preview" @click="hapusPreview">
                  Hapus
                </button>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-secondary" @click="tutupModalKarya">
              Batal
            </button>
            <button type="submit" class="btn-primary" :disabled="modalKarya.isSubmitting">
              <span v-if="modalKarya.isSubmitting" class="button-spinner"></span>
              {{ modalKarya.isSubmitting ? 'Menyimpan...' : (modalKarya.isEdit ? 'Simpan Perubahan' : 'Simpan Karya') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ======================================================== -->
    <!-- MODAL 2: PRATINJAU GAMBAR PENUH / LIGHTBOX (FR-03)        -->
    <!-- ======================================================== -->
    <div v-if="lightbox.isOpen" class="lightbox-backdrop" @click.self="tutupLightbox">
      <div class="lightbox-dialog">
        <button class="lightbox-close" title="Tutup pratinjau" @click="tutupLightbox">&times;</button>
        <div class="lightbox-media-container">
          <img :src="lightbox.imageUrl" :alt="lightbox.title" class="lightbox-img" />
        </div>
        <div class="lightbox-info">
          <h4 class="lightbox-title">{{ lightbox.title }}</h4>
          <p v-if="lightbox.description" class="lightbox-desc">{{ lightbox.description }}</p>
        </div>
      </div>
    </div>

    <!-- ======================================================== -->
    <!-- MODAL 3: KONFIRMASI HAPUS KARYA (UC-17)                  -->
    <!-- ======================================================== -->
    <div v-if="modalHapus.isOpen" class="modal-backdrop" @click.self="modalHapus.isOpen = false">
      <div class="modal-dialog modal-dialog-sm">
        <div class="modal-header">
          <h3 class="modal-title text-danger">Konfirmasi Hapus Karya</h3>
          <button class="modal-close" @click="modalHapus.isOpen = false">&times;</button>
        </div>
        <div class="modal-body">
          <p>
            Apakah Anda yakin ingin menghapus karya <strong>"{{ modalHapus.title }}"</strong> dari etalase portofolio publik Anda?
          </p>
          <p class="delete-warning">Tindakan ini akan menonaktifkan karya dari etalase JEMARI.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn-secondary" @click="modalHapus.isOpen = false">
            Batal
          </button>
          <button
            type="button"
            class="btn-danger"
            :disabled="modalHapus.isSubmitting"
            @click="eksekusiHapus"
          >
            {{ modalHapus.isSubmitting ? 'Menghapus...' : 'Ya, Hapus Karya' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ======================================================== -->
    <!-- MODAL 4: EDIT PROFIL FREELANCER RINGKAS                  -->
    <!-- ======================================================== -->
    <div v-if="modalEditProfil.isOpen" class="modal-backdrop" @click.self="modalEditProfil.isOpen = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3 class="modal-title">Edit Profil Freelancer</h3>
          <button class="modal-close" @click="modalEditProfil.isOpen = false">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="simpanProfil">
          <div class="form-group">
            <label class="form-label">Nama Lengkap</label>
            <input v-model="modalEditProfil.nama" type="text" required class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">Kategori Keahlian</label>
            <input v-model="modalEditProfil.kategori" type="text" required class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">Bio / Ringkasan Pengalaman</label>
            <textarea v-model="modalEditProfil.bio" rows="3" class="form-textarea"></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">Tautan Foto Profil (Avatar URL)</label>
            <input v-model="modalEditProfil.avatarUrl" type="url" class="form-input" />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-secondary" @click="modalEditProfil.isOpen = false">Batal</button>
            <button type="submit" class="btn-primary">Simpan Profil</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { portfolioService } from '../services/portfolioService'

// Data Profil Freelancer (Sesuai Mockup 4.5.7: Nama, Badge "Baru", Kategori Keahlian, Bio)
const freelancer = reactive({
  nama: 'User',
  kategori: 'Desain Grafis & Fotografi Produk UMKM',
  bio: 'Menyediakan jasa visual profesional bagi UMKM kuliner dan usaha kreatif homemade. Berfokus pada desain logo, banner feed media sosial, dan fotografi produk siap jual.',
  avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80'
})

// State Portofolio
const portfolios = ref([])
const isLoading = ref(false)
const alertMessage = ref('')
const alertType = ref('success')

// State Modal Form Tambah/Ubah Karya
const modalKarya = reactive({
  isOpen: false,
  isEdit: false,
  isSubmitting: false,
  id: null,
  title: '',
  description: '',
  imageUrl: '',
  imageMode: 'upload', // 'upload' | 'url'
  errorMessage: ''
})

// State Modal Lightbox
const lightbox = reactive({
  isOpen: false,
  title: '',
  description: '',
  imageUrl: ''
})

// State Modal Konfirmasi Hapus
const modalHapus = reactive({
  isOpen: false,
  id: null,
  title: '',
  isSubmitting: false
})

// State Modal Edit Profil
const modalEditProfil = reactive({
  isOpen: false,
  nama: '',
  kategori: '',
  bio: '',
  avatarUrl: ''
})

// Helper Inisial Avatar
function getInitials(name) {
  if (!name) return 'FL'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return name.substring(0, 2).toUpperCase()
}

// Format Tanggal Indonesia
function formatDate(dateStr) {
  if (!dateStr) return 'Baru ditambahkan'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('id-ID', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    })
  } catch (e) {
    return dateStr
  }
}

// Load data saat komponen dimuat
onMounted(async () => {
  await muatPortofolio()
})

async function muatPortofolio() {
  isLoading.value = true
  try {
    const data = await portfolioService.getMyPortfolios()
    portfolios.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Gagal memuat portofolio:', err)
    showAlert('Gagal memuat daftar portofolio', 'error')
  } finally {
    isLoading.value = false
  }
}

function showAlert(msg, type = 'success') {
  alertMessage.value = msg
  alertType.value = type
  setTimeout(() => {
    if (alertMessage.value === msg) {
      alertMessage.value = ''
    }
  }, 4500)
}

// Handler Modal Tambah Karya
function bukaModalTambah() {
  modalKarya.isOpen = true
  modalKarya.isEdit = false
  modalKarya.isSubmitting = false
  modalKarya.id = null
  modalKarya.title = ''
  modalKarya.description = ''
  modalKarya.imageUrl = ''
  modalKarya.imageMode = 'upload'
  modalKarya.errorMessage = ''
}

// Handler Modal Ubah Karya
function bukaModalUbah(item) {
  modalKarya.isOpen = true
  modalKarya.isEdit = true
  modalKarya.isSubmitting = false
  modalKarya.id = item.id
  modalKarya.title = item.title || item.judul || ''
  modalKarya.description = item.description || item.deskripsi || ''
  modalKarya.imageUrl = item.imageUrl || item.gambarUrl || ''
  modalKarya.imageMode = modalKarya.imageUrl.startsWith('data:') ? 'upload' : 'url'
  modalKarya.errorMessage = ''
}

function tutupModalKarya() {
  modalKarya.isOpen = false
  modalKarya.errorMessage = ''
}

// Handler Berkas Gambar Komputer (Validasi 5MB & Format JPG/PNG/WebP Sesuai UC-17)
function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return

  // Validasi Format File
  const validFormats = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!validFormats.includes(file.type.toLowerCase())) {
    modalKarya.errorMessage = 'Format berkas tidak didukung. Harap pilih gambar dengan format JPG, PNG, atau WebP.'
    event.target.value = ''
    return
  }

  // Validasi Ukuran File (Maksimal 5 MB)
  const maxBytes = 5 * 1024 * 1024
  if (file.size > maxBytes) {
    modalKarya.errorMessage = 'Ukuran berkas melebihi batas maksimal 5 MB. Silakan kompres atau pilih berkas lain.'
    event.target.value = ''
    return
  }

  modalKarya.errorMessage = ''
  const reader = new FileReader()
  reader.onload = (e) => {
    modalKarya.imageUrl = e.target.result
  }
  reader.readAsDataURL(file)
}

function hapusPreview() {
  modalKarya.imageUrl = ''
}

// Eksekusi Simpan Karya (Tambah / Ubah)
async function simpanKarya() {
  modalKarya.errorMessage = ''

  if (!modalKarya.title.trim()) {
    modalKarya.errorMessage = 'Judul karya wajib diisi.'
    return
  }

  if (modalKarya.title.trim().length > 150) {
    modalKarya.errorMessage = 'Judul karya tidak boleh melebihi 150 karakter.'
    return
  }

  if (!modalKarya.imageUrl.trim()) {
    modalKarya.errorMessage = 'Berkas gambar atau tautan gambar karya wajib diisi.'
    return
  }

  modalKarya.isSubmitting = true
  const payload = {
    title: modalKarya.title.trim(),
    judul: modalKarya.title.trim(),
    description: modalKarya.description.trim(),
    deskripsi: modalKarya.description.trim(),
    imageUrl: modalKarya.imageUrl.trim(),
    gambarUrl: modalKarya.imageUrl.trim()
  }

  try {
    if (modalKarya.isEdit) {
      await portfolioService.updatePortfolio(modalKarya.id, payload)
      showAlert('Karya portofolio berhasil diperbarui!')
    } else {
      await portfolioService.createPortfolio(payload)
      showAlert('Karya portofolio berhasil ditambahkan ke etalase!')
    }
    tutupModalKarya()
    await muatPortofolio()
  } catch (err) {
    modalKarya.errorMessage = err.message || 'Gagal menyimpan karya portofolio.'
  } finally {
    modalKarya.isSubmitting = false
  }
}

// Handler Lightbox Pratinjau Gambar Penuh (FR-03)
function bukaLightbox(item) {
  lightbox.isOpen = true
  lightbox.title = item.title || item.judul
  lightbox.description = item.description || item.deskripsi
  lightbox.imageUrl = item.imageUrl || item.gambarUrl
}

function tutupLightbox() {
  lightbox.isOpen = false
}

// Handler Konfirmasi Hapus
function konfirmasiHapus(item) {
  modalHapus.isOpen = true
  modalHapus.id = item.id
  modalHapus.title = item.title || item.judul
  modalHapus.isSubmitting = false
}

async function eksekusiHapus() {
  modalHapus.isSubmitting = true
  try {
    await portfolioService.deletePortfolio(modalHapus.id)
    modalHapus.isOpen = false
    showAlert('Karya portofolio berhasil dihapus dari etalase.')
    await muatPortofolio()
  } catch (err) {
    showAlert(err.message || 'Gagal menghapus portofolio.', 'error')
  } finally {
    modalHapus.isSubmitting = false
  }
}

// Handler Modal Edit Profil
function bukaModalEditProfil() {
  modalEditProfil.isOpen = true
  modalEditProfil.nama = freelancer.nama
  modalEditProfil.kategori = freelancer.kategori
  modalEditProfil.bio = freelancer.bio
  modalEditProfil.avatarUrl = freelancer.avatarUrl
}

function simpanProfil() {
  freelancer.nama = modalEditProfil.nama
  freelancer.kategori = modalEditProfil.kategori
  freelancer.bio = modalEditProfil.bio
  freelancer.avatarUrl = modalEditProfil.avatarUrl
  modalEditProfil.isOpen = false
  showAlert('Profil freelancer berhasil diperbarui!')
}
</script>

<style scoped>
/* ========================================================= */
/* CSS Styling Halaman Portofolio Sesuai Standar Mockup 4.5.7 */
/* ========================================================= */

.portfolio-page {
  min-height: 100vh;
  background-color: #F8FAFC;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}

/* Header Navigation Bar */
.app-header {
  background-color: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
  position: sticky;
  top: 0;
  z-index: 40;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-container {
  max-width: 1140px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand-group {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.brand-title {
  font-size: 22px;
  font-weight: 800;
  color: #2563EB;
  text-decoration: none;
  letter-spacing: -0.5px;
}

.brand-sub {
  font-size: 12px;
  color: #64748B;
  display: none;
}

@media (min-width: 768px) {
  .brand-sub {
    display: inline;
  }
}

.nav-menu {
  display: flex;
  gap: 8px;
}

.nav-link {
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: #2563EB;
  background-color: #EFF6FF;
}

.nav-link.active {
  color: #2563EB;
  background-color: #DBEAFE;
  font-weight: 600;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #F1F5F9;
  border: 1px solid #E2E8F0;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.pill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10B981;
}

/* Page Layout */
.page-content {
  padding: 28px 16px 60px;
}

.container {
  max-width: 1140px;
  margin: 0 auto;
}

/* Alert Banner */
.alert-banner {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
}

.alert-success {
  background-color: #ECFDF5;
  color: #065F46;
  border: 1px solid #A7F3D0;
}

.alert-danger {
  background-color: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}

.alert-text {
  flex: 1;
}

.alert-close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: inherit;
}

/* 1. Profil Header Card (Mockup 4.5.7) */
.profile-header-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 28px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-bottom: 32px;
}

.profile-main-info {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex: 1;
}

.avatar-container {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #E2E8F0;
  flex-shrink: 0;
  border: 3px solid #FFFFFF;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-initials {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2563EB;
  color: #FFFFFF;
  font-size: 28px;
  font-weight: 700;
}

.profile-details {
  flex: 1;
}

.name-badge-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.freelancer-name {
  font-size: 24px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}

/* Badge "Baru" Sesuai Mockup 4.5.7 */
.badge-baru {
  display: inline-block;
  background-color: #EFF6FF;
  color: #2563EB;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 9999px;
  border: 1px solid #BFDBFE;
}

.category-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #059669;
  margin-bottom: 10px;
}

.freelancer-bio {
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
  margin: 0;
  max-width: 680px;
}

.btn-edit-profile {
  background-color: #FFFFFF;
  border: 1px solid #CBD5E1;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-edit-profile:hover {
  background-color: #F8FAFC;
  border-color: #94A3B8;
  color: #0F172A;
}

/* 2. Seksi Portofolio (Mockup 4.5.7) */
.portfolio-section {
  margin-top: 10px;
}

.section-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 2px solid #E2E8F0;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.section-heading {
  font-size: 22px;
  font-weight: 800;
  color: #0F172A;
  margin: 0 0 4px 0;
}

.section-subheading {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.btn-primary-upload {
  background-color: #2563EB;
  color: #FFFFFF;
  border: none;
  font-size: 14px;
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
  transition: background-color 0.2s, transform 0.1s;
}

.btn-primary-upload:hover {
  background-color: #1D4ED8;
}

.btn-primary-upload:active {
  transform: scale(0.98);
}

.plus-sign {
  font-size: 16px;
  font-weight: 700;
}

/* Loading & Empty States */
.state-container {
  text-align: center;
  padding: 60px 20px;
  color: #64748B;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #E2E8F0;
  border-top-color: #2563EB;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 14px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-portfolio-card {
  background-color: #FFFFFF;
  border: 2px dashed #CBD5E1;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  max-width: 540px;
  margin: 40px auto;
}

.empty-icon-box {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-title {
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 6px 0;
}

.empty-description {
  font-size: 14px;
  color: #64748B;
  margin: 0 auto;
  line-height: 1.5;
}

/* 3. Grid Portofolio Responsif */
.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.portfolio-card {
  background-color: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.portfolio-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.card-media {
  position: relative;
  width: 100%;
  height: 190px;
  background-color: #0F172A;
  cursor: pointer;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.card-media:hover .card-img {
  transform: scale(1.05);
}

.media-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.card-media:hover .media-overlay {
  opacity: 1;
}

.zoom-pill {
  background: rgba(255, 255, 255, 0.95);
  color: #0F172A;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 9999px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-desc {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0 0 16px 0;
  flex: 1;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #F1F5F9;
}

.card-date {
  font-size: 11px;
  color: #94A3B8;
  font-weight: 500;
}

.card-action-btns {
  display: flex;
  gap: 8px;
}

.btn-action {
  font-size: 12px;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  border: none;
  transition: background-color 0.15s;
}

.btn-edit {
  background-color: #EFF6FF;
  color: #2563EB;
}

.btn-edit:hover {
  background-color: #DBEAFE;
}

.btn-delete {
  background-color: #FEF2F2;
  color: #DC2626;
}

.btn-delete:hover {
  background-color: #FEE2E2;
}

/* ========================================================= */
/* MODAL STYLES (CRUD & LIGHTBOX)                            */
/* ========================================================= */

.modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 16px;
}

.modal-dialog {
  background-color: #FFFFFF;
  border-radius: 12px;
  width: 100%;
  max-width: 520px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-dialog-sm {
  max-width: 420px;
}

.modal-header {
  padding: 18px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}

.text-danger {
  color: #DC2626;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #94A3B8;
  cursor: pointer;
  line-height: 1;
}

.modal-close:hover {
  color: #0F172A;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.form-error-box {
  background-color: #FEF2F2;
  color: #B91C1C;
  border: 1px solid #FECACA;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 18px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
  display: block;
}

.required-star {
  color: #DC2626;
}

.file-note {
  font-size: 12px;
  color: #64748B;
  font-weight: 400;
  margin-left: 4px;
}

.char-counter {
  font-size: 11px;
  color: #94A3B8;
}

.char-limit {
  color: #DC2626;
  font-weight: 600;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #CBD5E1;
  border-radius: 8px;
  font-size: 14px;
  color: #0F172A;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus,
.form-textarea:focus {
  border-color: #2563EB;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.file-mode-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.switch-btn {
  flex: 1;
  background-color: #F8FAFC;
  border: 1px solid #CBD5E1;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
}

.switch-btn.active {
  background-color: #EFF6FF;
  border-color: #2563EB;
  color: #2563EB;
}

.upload-dropzone {
  border: 2px dashed #93C5FD;
  background-color: #EFF6FF;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  position: relative;
  cursor: pointer;
  transition: background-color 0.2s;
}

.upload-dropzone:hover {
  background-color: #DBEAFE;
}

.file-hidden-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  width: 100%;
  height: 100%;
}

.dropzone-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
}

.dropzone-icon {
  font-size: 28px;
  margin-bottom: 4px;
}

.dropzone-text {
  font-size: 13px;
  font-weight: 600;
  color: #1E40AF;
}

.dropzone-sub {
  font-size: 11px;
  color: #6B7280;
  margin-top: 2px;
}

.image-preview-wrapper {
  margin-top: 12px;
}

.preview-label {
  font-size: 12px;
  color: #64748B;
  margin: 0 0 6px 0;
}

.preview-box {
  position: relative;
  display: inline-block;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #CBD5E1;
}

.preview-img {
  width: 140px;
  height: 90px;
  object-fit: cover;
  display: block;
}

.btn-remove-preview {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(15, 23, 42, 0.75);
  color: #FFFFFF;
  border: none;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-footer {
  padding: 16px 24px;
  background-color: #F8FAFC;
  border-top: 1px solid #E2E8F0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-secondary {
  background-color: #FFFFFF;
  border: 1px solid #CBD5E1;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-primary {
  background-color: #2563EB;
  border: none;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-danger {
  background-color: #DC2626;
  border: none;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
}

.delete-warning {
  font-size: 13px;
  color: #64748B;
  margin-top: 8px;
}

/* Lightbox Modal (FR-03) */
.lightbox-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 120;
  padding: 20px;
}

.lightbox-dialog {
  position: relative;
  max-width: 900px;
  width: 100%;
  background: #0F172A;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.lightbox-close {
  position: absolute;
  top: 14px;
  right: 16px;
  background: rgba(255, 255, 255, 0.2);
  color: #FFFFFF;
  border: none;
  font-size: 26px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.4);
}

.lightbox-media-container {
  max-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #020617;
}

.lightbox-img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.lightbox-info {
  padding: 16px 20px;
  background: #0F172A;
  color: #FFFFFF;
}

.lightbox-title {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 700;
  color: #F8FAFC;
}

.lightbox-desc {
  margin: 0;
  font-size: 13px;
  color: #94A3B8;
  line-height: 1.5;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
