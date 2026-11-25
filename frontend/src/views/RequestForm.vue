<template>
  <div class="form-page">
    <div class="form-container">
      
      <!-- Navigasi Balik -->
      <router-link to="/request-saya" class="back-link">
        &larr; Kembali ke Request Saya
      </router-link>

      <!-- Card Formulir (Mockup 4.5.4) -->
      <div class="form-card">
        <div class="card-header">
          <h2 class="form-title">{{ isEditMode ? 'Ubah Request Kebutuhan' : 'Buat Request Baru' }}</h2>
          <p class="form-subtitle">
            {{ isEditMode
              ? 'Perbarui rincian kebutuhan jasa Anda agar freelancer memahami spesifikasi terbaru.'
              : 'Tuliskan kebutuhan jasa Anda secara rinci untuk menarik freelancer yang kompeten.' }}
          </p>
        </div>

        <!-- Alert Error -->
        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleSubmit">
          
          <!-- 1. Judul Pekerjaan -->
          <div class="form-group">
            <label class="form-label" for="judul">
              Judul Pekerjaan <span class="required">*</span>
            </label>
            <input
              id="judul"
              type="text"
              v-model="formRequest.judul"
              class="form-control"
              placeholder="Contoh: Foto produk 10 pcs kue kering & brownies"
              maxlength="255"
              required
            />
            <span class="field-hint">Gunakan judul yang jelas dan spesifik mencerminkan output yang Anda harapkan.</span>
          </div>

          <!-- 2. Kategori Skill / Jasa -->
          <div class="form-group">
            <label class="form-label" for="kategori">
              Kategori Skill / Jasa <span class="required">*</span>
            </label>
            <select id="kategori" v-model="formRequest.kategori" class="form-control" required>
              <option value="" disabled>-- Pilih Kategori Jasa --</option>
              <option v-for="item in kategoriJasa" :key="item" :value="item">
                {{ item }}
              </option>
            </select>
          </div>

          <!-- 3. Deskripsi Kebutuhan -->
          <div class="form-group">
            <label class="form-label" for="deskripsi">
              Deskripsi Detail Kebutuhan <span class="required">*</span>
            </label>
            <textarea
              id="deskripsi"
              v-model="formRequest.deskripsi"
              class="form-control textarea-control"
              placeholder="Jelaskan kebutuhanmu di sini (misal: jumlah foto/desain, konsep gaya yang diinginkan, format file akhir, dll)..."
              rows="4"
              required
            ></textarea>
            <span class="field-hint">Deskripsi yang detail membantu freelancer memberikan estimasi penawaran yang akurat.</span>
          </div>

          <!-- 4. Rentang Budget / Anggaran (Mockup 4.5.4) -->
          <div class="form-group">
            <label class="form-label">
              Ekspektasi Anggaran (Rp) <span class="required">*</span>
            </label>
            <div class="budget-grid">
              <div>
                <label class="sub-label">Budget Minimum</label>
                <input
                  type="number"
                  v-model.number="formRequest.budgetMin"
                  class="form-control"
                  placeholder="Contoh: 150000"
                  min="1"
                  required
                />
              </div>
              <div>
                <label class="sub-label">Budget Maksimum</label>
                <input
                  type="number"
                  v-model.number="formRequest.budgetMax"
                  class="form-control"
                  placeholder="Contoh: 300000"
                  min="1"
                  required
                />
              </div>
            </div>
            <div class="budget-preview" v-if="formRequest.budgetMin || formRequest.budgetMax">
              Estimasi: <strong>{{ formatRupiah(formRequest.budgetMin) }} &ndash; {{ formatRupiah(formRequest.budgetMax) }}</strong>
            </div>
          </div>

          <!-- 5. Tenggat Waktu (Deadline) -->
          <div class="form-group">
            <label class="form-label" for="deadline">
              Tenggat Waktu Pekerjaan (Deadline) <span class="required">*</span>
            </label>
            <input
              id="deadline"
              type="date"
              v-model="formRequest.deadline"
              :min="minDate"
              class="form-control"
              required
            />
            <span class="field-hint">Pilih tanggal batas akhir penyerahan hasil oleh freelancer.</span>
          </div>

          <!-- 6. Lampiran Foto Referensi (Mockup 4.5.4 / UC-09) -->
          <div class="form-group">
            <label class="form-label">
              Foto Referensi Produk (Opsional)
            </label>
            <div class="upload-box" @click="triggerFileInput">
              <input
                type="file"
                ref="fileInputRef"
                accept="image/*"
                style="display: none;"
                @change="handleFileUpload"
              />
              <div v-if="!fotoPreview" class="upload-placeholder">
                <span class="upload-icon">&#128247;</span>
                <p>Klik untuk unggah contoh foto produk atau referensi gaya</p>
                <span class="upload-note">Format JPG/PNG, maksimal 5 MB</span>
              </div>
              <div v-else class="preview-container">
                <img :src="fotoPreview" alt="Preview Foto Referensi" class="image-preview" />
                <button type="button" class="btn-remove-photo" @click.stop="hapusFoto">Hapus Foto</button>
              </div>
            </div>
          </div>

          <!-- Tombol Aksi Formulir -->
          <div class="form-actions">
            <router-link to="/request-saya" class="btn btn-secondary">
              Batal
            </router-link>
            <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
              <span v-if="isSubmitting">Menyimpan...</span>
              <span v-else>{{ isEditMode ? 'Simpan Perubahan' : 'Posting Request Sekarang' }}</span>
            </button>
          </div>

        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requestService } from '../services/requestService'

const route = useRoute()
const router = useRouter()

const isEditMode = computed(() => Boolean(route.params.id))
const requestId = computed(() => route.params.id)

const kategoriJasa = [
  'Fotografi Produk',
  'Desain Banner & Poster',
  'Desain Grafis & Logo',
  'Pengelolaan Konten Media Sosial',
  'Pembuatan Website & IT',
  'Digital Marketing & SEO',
  'Penulisan & Copywriting',
  'Video Promosi Produk'
]

const formRequest = ref({
  judul: '',
  kategori: '',
  deskripsi: '',
  budgetMin: null,
  budgetMax: null,
  deadline: ''
})

const fotoPreview = ref(null)
const fileInputRef = ref(null)
const errorMessage = ref('')
const isSubmitting = ref(false)
const isUploadingPhoto = ref(false)
const referencePhotos = ref([])

// Tanggal minimal = hari ini (format YYYY-MM-DD)
const minDate = new Date().toISOString().split('T')[0]

onMounted(async () => {
  if (isEditMode.value) {
    try {
      const data = await requestService.getRequestById(requestId.value)
      formRequest.value = {
        judul: data.judul || data.title,
        kategori: data.kategori || data.category,
        deskripsi: data.deskripsi || data.description,
        budgetMin: data.budgetMin || data.budget || data.harga,
        budgetMax: data.budgetMax || data.budget || data.harga,
        deadline: data.deadline ? data.deadline.substring(0, 10) : ''
      }
      // Load reference photos
      const photos = await requestService.getReferencePhotos(requestId.value)
      referencePhotos.value = photos || []
      if (photos && photos.length > 0) {
        fotoPreview.value = photos[0].photoUrl
      }
    } catch (err) {
      errorMessage.value = 'Gagal memuat data request: ' + err.message
    }
  }
})

function triggerFileInput() {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

async function handleFileUpload(e) {
  const file = e.target.files[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      alert('Ukuran file melebihi batas maksimal 5 MB!')
      return
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      alert('Format file harus JPG, PNG, atau WEBP!')
      return
    }
    
    isUploadingPhoto.value = true
    try {
      // Show local preview immediately
      const reader = new FileReader()
      reader.onload = (event) => {
        fotoPreview.value = event.target.result
      }
      reader.readAsDataURL(file)

      // Upload to API if editing existing request
      if (isEditMode.value) {
        const result = await requestService.uploadReferencePhoto(requestId.value, file)
        if (result) {
          referencePhotos.value.push(result)
        }
      }
    } catch (err) {
      errorMessage.value = err.message || 'Gagal mengunggah foto referensi'
      fotoPreview.value = null
    } finally {
      isUploadingPhoto.value = false
    }
  }
}

async function hapusFoto() {
  // If editing and photo exists in reference photos, delete from API
  if (isEditMode.value && referencePhotos.value.length > 0) {
    const photoToDelete = referencePhotos.value[0] // Assuming single photo for now
    try {
      await requestService.deleteReferencePhoto(requestId.value, photoToDelete.id)
      referencePhotos.value = referencePhotos.value.filter(p => p.id !== photoToDelete.id)
      if (referencePhotos.value.length > 0) {
        fotoPreview.value = referencePhotos.value[0].photoUrl
      } else {
        fotoPreview.value = null
      }
    } catch (err) {
      errorMessage.value = err.message || 'Gagal menghapus foto referensi'
    }
  } else {
    // Just clear local preview
    fotoPreview.value = null
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

function formatRupiah(num) {
  if (!num) return 'Rp 0'
  return 'Rp ' + Number(num).toLocaleString('id-ID')
}

async function handleSubmit() {
  errorMessage.value = ''

  // Validasi Sisi Klien
  if (!formRequest.value.judul.trim()) {
    errorMessage.value = 'Judul request wajib diisi!'
    return
  }
  if (!formRequest.value.kategori) {
    errorMessage.value = 'Kategori jasa wajib dipilih!'
    return
  }
  if (!formRequest.value.deskripsi.trim()) {
    errorMessage.value = 'Deskripsi kebutuhan wajib diisi!'
    return
  }
  if (!formRequest.value.budgetMin || formRequest.value.budgetMin <= 0) {
    errorMessage.value = 'Budget minimum harus lebih besar dari Rp 0!'
    return
  }
  if (formRequest.value.budgetMax && formRequest.value.budgetMax < formRequest.value.budgetMin) {
    errorMessage.value = 'Budget maksimum tidak boleh lebih kecil dari budget minimum!'
    return
  }
  if (!formRequest.value.deadline) {
    errorMessage.value = 'Tenggat waktu (deadline) wajib diisi!'
    return
  }

  isSubmitting.value = true

  const payload = {
    judul: formRequest.value.judul.trim(),
    title: formRequest.value.judul.trim(),
    kategori: formRequest.value.kategori,
    category: formRequest.value.kategori,
    deskripsi: formRequest.value.deskripsi.trim(),
    description: formRequest.value.deskripsi.trim(),
    budgetMin: formRequest.value.budgetMin,
    budgetMax: formRequest.value.budgetMax || formRequest.value.budgetMin,
    budget: formRequest.value.budgetMax || formRequest.value.budgetMin,
    harga: formRequest.value.budgetMax || formRequest.value.budgetMin,
    deadline: formRequest.value.deadline
  }

  try {
    if (isEditMode.value) {
      await requestService.updateRequest(requestId.value, payload)
    } else {
      await requestService.createRequest(payload)
    }
    router.push('/request-saya')
  } catch (err) {
    errorMessage.value = 'Gagal menyimpan: ' + err.message
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.form-page {
  padding: 32px 16px 64px 16px;
  background-color: #f9fafb;
  min-height: calc(100vh - 65px);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.form-container {
  max-width: 680px;
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

.form-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 16px;
}

.form-title {
  font-size: 22px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 6px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.sub-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 4px;
}

.required {
  color: #dc2626;
}

.form-control {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background-color: #ffffff;
  color: #111827;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-control:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.textarea-control {
  resize: vertical;
  line-height: 1.5;
}

.field-hint {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.budget-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.budget-preview {
  margin-top: 6px;
  font-size: 13px;
  color: #1d4ed8;
  background-color: #eff6ff;
  padding: 6px 12px;
  border-radius: 6px;
}

/* Upload Box */
.upload-box {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 24px 16px;
  text-align: center;
  background-color: #f9fafb;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-box:hover {
  border-color: #2563eb;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.upload-icon {
  font-size: 28px;
}

.upload-placeholder p {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

.upload-note {
  font-size: 11px;
  color: #9ca3af;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.image-preview {
  max-width: 100%;
  max-height: 180px;
  border-radius: 6px;
  object-fit: contain;
}

.btn-remove-photo {
  background: none;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-remove-photo:hover {
  background-color: #fee2e2;
}

/* Aksi */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  border-top: 1px solid #f3f4f6;
  padding-top: 20px;
}

.btn {
  padding: 10px 20px;
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

.btn-primary:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}
</style>


<style scoped>
.form-page {
  padding: 32px 16px 64px 16px;
  background-color: #f9fafb;
  min-height: calc(100vh - 65px);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.form-container {
  max-width: 680px;
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

.form-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 16px;
}

.form-title {
  font-size: 22px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 6px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.sub-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 4px;
}

.required {
  color: #dc2626;
}

.form-control {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background-color: #ffffff;
  color: #111827;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-control:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.textarea-control {
  resize: vertical;
  line-height: 1.5;
}

.field-hint {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.budget-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.budget-preview {
  margin-top: 6px;
  font-size: 13px;
  color: #1d4ed8;
  background-color: #eff6ff;
  padding: 6px 12px;
  border-radius: 6px;
}

/* Upload Box */
.upload-box {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 24px 16px;
  text-align: center;
  background-color: #f9fafb;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-box:hover {
  border-color: #2563eb;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.upload-icon {
  font-size: 28px;
}

.upload-placeholder p {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

.upload-note {
  font-size: 11px;
  color: #9ca3af;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.image-preview {
  max-width: 100%;
  max-height: 180px;
  border-radius: 6px;
  object-fit: contain;
}

.btn-remove-photo {
  background: none;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-remove-photo:hover {
  background-color: #fee2e2;
}

/* Aksi */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  border-top: 1px solid #f3f4f6;
  padding-top: 20px;
}

.btn {
  padding: 10px 20px;
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

.btn-primary:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
}

.alert-error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}
</style>