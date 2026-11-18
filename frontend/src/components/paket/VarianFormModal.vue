<script setup>
import { ref, reactive, watch } from 'vue'
import { formatRupiah } from '../../utils/rupiahPaket'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  initialData: {
    type: Object,
    default: null,
  },
  serverErrors: {
    type: Array,
    default: () => [],
  },
  serverGeneralError: {
    type: String,
    default: '',
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'submit'])

const form = reactive({
  name: '',
  description: '',
  price: '',
  estimatedDays: '',
})

const clientErrors = reactive({
  name: '',
  description: '',
  price: '',
  estimatedDays: '',
})

// Sinkronkan data saat modal dibuka untuk tambah atau edit
watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      resetErrors()
      if (props.isEdit && props.initialData) {
        form.name = props.initialData.name || ''
        form.description = props.initialData.description || ''
        form.price = props.initialData.price !== undefined ? props.initialData.price : ''
        form.estimatedDays =
          props.initialData.estimatedDays !== undefined ? props.initialData.estimatedDays : ''
      } else {
        form.name = ''
        form.description = ''
        form.price = ''
        form.estimatedDays = ''
      }
    }
  },
)

function resetErrors() {
  clientErrors.name = ''
  clientErrors.description = ''
  clientErrors.price = ''
  clientErrors.estimatedDays = ''
}

// Cari pesan error untuk suatu kolom (mengutamakan pesan error dari backend, lalu client-side)
function getFieldError(fieldName) {
  if (clientErrors[fieldName]) {
    return clientErrors[fieldName]
  }
  const fromServer = props.serverErrors.find((e) => e.field === fieldName)
  return fromServer ? fromServer.message : ''
}

function validateClient() {
  resetErrors()
  let valid = true

  // Validasi Nama
  const trimmedName = form.name.trim()
  if (!trimmedName) {
    clientErrors.name = 'Nama varian tidak boleh kosong.'
    valid = false
  } else if (trimmedName.length > 100) {
    clientErrors.name = 'Nama varian maksimal 100 karakter.'
    valid = false
  }

  // Validasi Deskripsi
  if (form.description && form.description.length > 255) {
    clientErrors.description = 'Deskripsi varian maksimal 255 karakter.'
    valid = false
  }

  // Validasi Harga
  if (form.price === '' || form.price === null || form.price === undefined) {
    clientErrors.price = 'Harga varian tidak boleh kosong.'
    valid = false
  } else {
    const numPrice = Number(form.price)
    if (Number.isNaN(numPrice) || numPrice <= 0) {
      clientErrors.price = 'Harga varian harus berupa angka lebih dari 0.'
      valid = false
    } else if (numPrice > 9999999999.99) {
      clientErrors.price = 'Harga varian melebihi batas maksimal yang diperbolehkan.'
      valid = false
    }
  }

  // Validasi Estimasi Durasi
  if (
    form.estimatedDays === '' ||
    form.estimatedDays === null ||
    form.estimatedDays === undefined
  ) {
    clientErrors.estimatedDays = 'Estimasi durasi pengerjaan tidak boleh kosong.'
    valid = false
  } else {
    const numDays = Number(form.estimatedDays)
    if (!Number.isInteger(numDays) || numDays < 1 || numDays > 365) {
      clientErrors.estimatedDays =
        'Estimasi durasi harus berupa bilangan bulat antara 1 sampai 365 hari.'
      valid = false
    }
  }

  return valid
}

function handleSubmit() {
  if (!validateClient()) {
    return
  }

  emit('submit', {
    name: form.name.trim(),
    description: form.description ? form.description.trim() : null,
    price: Number(form.price),
    estimatedDays: Number(form.estimatedDays),
  })
}
</script>

<template>
  <div v-if="isOpen" class="modal-backdrop" @click.self="emit('close')">
    <div
      class="modal-dialog"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="isEdit ? 'judul-modal-edit' : 'judul-modal-tambah'"
    >
      <header class="modal-header">
        <h2 :id="isEdit ? 'judul-modal-edit' : 'judul-modal-tambah'" class="modal-title">
          {{ isEdit ? 'Ubah Varian Paket' : 'Tambah Varian Baru' }}
        </h2>
        <button
          type="button"
          class="btn-close"
          aria-label="Tutup jendela formulir"
          @click="emit('close')"
        >
          ✕
        </button>
      </header>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <!-- Banner Catatan UC-18 -->
        <div class="banner-uc18">
          <span class="icon-info" aria-hidden="true">ℹ️</span>
          <p class="teks-uc18">
            <strong>Ketentuan Pemesanan:</strong> Perubahan harga atau durasi hanya berlaku untuk
            pemesanan baru. Pesanan yang sedang berjalan tetap menggunakan ketentuan awal.
          </p>
        </div>

        <!-- Pesan Error Umum Server -->
        <div v-if="serverGeneralError" class="alert-general-error" role="alert">
          {{ serverGeneralError }}
        </div>

        <!-- Kolom Nama Varian -->
        <div class="form-group" :class="{ 'has-error': !!getFieldError('name') }">
          <label for="varian-name">
            Nama Varian <span class="required" aria-hidden="true">*</span>
          </label>
          <input
            id="varian-name"
            v-model="form.name"
            type="text"
            placeholder="Contoh: Basic, Standard, atau Premium"
            maxlength="100"
            :aria-invalid="!!getFieldError('name')"
            :aria-describedby="getFieldError('name') ? 'error-name' : 'hint-name'"
            required
          />
          <small id="hint-name" class="field-hint"
            >Maksimal 100 karakter. Nama tidak boleh kembar dengan varian lain.</small
          >
          <span v-if="getFieldError('name')" id="error-name" class="field-error" role="alert">
            {{ getFieldError('name') }}
          </span>
        </div>

        <!-- Kolom Deskripsi -->
        <div class="form-group" :class="{ 'has-error': !!getFieldError('description') }">
          <label for="varian-description"> Rincian / Deskripsi Layanan </label>
          <textarea
            id="varian-description"
            v-model="form.description"
            rows="3"
            placeholder="Rincian fitur atau hasil kerja yang diperoleh UMKM pada varian ini..."
            maxlength="255"
            :aria-invalid="!!getFieldError('description')"
            :aria-describedby="
              getFieldError('description') ? 'error-description' : 'hint-description'
            "
          ></textarea>
          <div class="field-hint-row">
            <small id="hint-description" class="field-hint">Opsional, maksimal 255 karakter.</small>
            <small class="char-count">{{ form.description.length }}/255</small>
          </div>
          <span
            v-if="getFieldError('description')"
            id="error-description"
            class="field-error"
            role="alert"
          >
            {{ getFieldError('description') }}
          </span>
        </div>

        <!-- Kolom Harga & Estimasi Durasi (2 Kolom) -->
        <div class="form-row">
          <!-- Kolom Harga -->
          <div class="form-group" :class="{ 'has-error': !!getFieldError('price') }">
            <label for="varian-price">
              Harga (Rp) <span class="required" aria-hidden="true">*</span>
            </label>
            <input
              id="varian-price"
              v-model.number="form.price"
              type="number"
              min="1"
              step="1000"
              placeholder="Contoh: 150000"
              :aria-invalid="!!getFieldError('price')"
              :aria-describedby="getFieldError('price') ? 'error-price' : 'hint-price'"
              required
            />
            <small id="hint-price" class="field-hint">
              {{
                form.price
                  ? `Pratinjau: ${formatRupiah(form.price)}`
                  : 'Angka lebih dari 0, tanpa titik atau koma.'
              }}
            </small>
            <span v-if="getFieldError('price')" id="error-price" class="field-error" role="alert">
              {{ getFieldError('price') }}
            </span>
          </div>

          <!-- Kolom Estimasi Durasi -->
          <div class="form-group" :class="{ 'has-error': !!getFieldError('estimatedDays') }">
            <label for="varian-days">
              Estimasi Pengerjaan (Hari) <span class="required" aria-hidden="true">*</span>
            </label>
            <input
              id="varian-days"
              v-model.number="form.estimatedDays"
              type="number"
              min="1"
              max="365"
              placeholder="Contoh: 3"
              :aria-invalid="!!getFieldError('estimatedDays')"
              :aria-describedby="getFieldError('estimatedDays') ? 'error-days' : 'hint-days'"
              required
            />
            <small id="hint-days" class="field-hint">1 s.d. 365 hari.</small>
            <span
              v-if="getFieldError('estimatedDays')"
              id="error-days"
              class="field-error"
              role="alert"
            >
              {{ getFieldError('estimatedDays') }}
            </span>
          </div>
        </div>

        <!-- Tombol Aksi Formulir -->
        <footer class="modal-footer">
          <button type="button" class="btn-batal" :disabled="isLoading" @click="emit('close')">
            Batal
          </button>
          <button type="submit" class="btn-simpan" :disabled="isLoading">
            {{ isLoading ? 'Menyimpan...' : isEdit ? 'Perbarui Varian' : 'Simpan Varian' }}
          </button>
        </footer>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(17, 24, 39, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1000;
}

.modal-dialog {
  background: #ffffff;
  border-radius: 12px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  min-height: 44px;
  min-width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #374151;
  background-color: #f3f4f6;
}

.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.banner-uc18 {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  padding: 12px 14px;
  border-radius: 8px;
}

.icon-info {
  font-size: 16px;
  flex-shrink: 0;
}

.teks-uc18 {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
  color: #1e40af;
}

.alert-general-error {
  padding: 12px 14px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 500px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.required {
  color: #dc2626;
}

input,
textarea {
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 15px;
  color: #111827;
  font-family: inherit;
  min-height: 44px;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #7c3aed;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.15);
}

.form-group.has-error input,
.form-group.has-error textarea {
  border-color: #dc2626;
  background-color: #fef2f2;
}

.field-hint {
  font-size: 12px;
  color: #6b7280;
}

.field-hint-row {
  display: flex;
  justify-content: space-between;
}

.char-count {
  font-size: 12px;
  color: #9ca3af;
}

.field-error {
  font-size: 12px;
  color: #dc2626;
  font-weight: 600;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.btn-batal {
  padding: 12px 18px;
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-batal:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.btn-simpan {
  padding: 12px 22px;
  background-color: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-simpan:hover:not(:disabled) {
  background-color: #6d28d9;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
