
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const apiBase =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const form = ref({
  email: '',
  businessName: '',
  phoneNumber: '',
  category: '',
  businessScale: '',
  password: ''
})

const loading = ref(true)
const menyimpan = ref(false)
const pesan = ref('')
const tipePesan = ref('')

function getToken() {
  return (
    localStorage.getItem('jemari_token') ||
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken')
  )
}

function hapusDataLogin() {
  localStorage.removeItem('jemari_token')
  localStorage.removeItem('token')
  localStorage.removeItem('accessToken')
  localStorage.removeItem('jemari_user')
  localStorage.removeItem('user')
}

// Mengambil data profil dari backend
async function ambilProfil() {
  const token = getToken()

  if (!token) {
    router.push('/login')
    return
  }

  loading.value = true
  pesan.value = ''

  try {
    const response = await fetch(`${apiBase}/api/auth/me`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json'
      }
    })

    const result = await response.json().catch(() => null)

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        hapusDataLogin()
        router.push('/login')
        return
      }

      throw new Error(
        result?.message || 'Gagal mengambil data profil.'
      )
    }

    // Mengambil data dari respons API
    const data = result?.data ?? result

    form.value = {
      email: data.email || '',
      businessName: data.businessName || '',
      phoneNumber: data.phoneNumber || '',
      category: data.category || '',
      businessScale: data.businessScale || '',
      password: ''
    }

  } catch (error) {
    console.error('Gagal mengambil profil:', error)
    pesan.value = error.message
    tipePesan.value = 'error'
  } finally {
    loading.value = false
  }
}

// Menyimpan perubahan profil
async function simpanPerubahan() {
  pesan.value = ''
  tipePesan.value = ''

  if (
    !form.value.businessName.trim() ||
    !form.value.category.trim()
  ) {
    pesan.value = 'Nama Usaha dan Kategori Usaha wajib diisi.'
    tipePesan.value = 'error'
    return
  }

  const token = getToken()

  if (!token) {
    router.push('/login')
    return
  }

  menyimpan.value = true

  try {
    // Hanya mengirim field yang didukung backend PUT
    const requestData = {
      businessName: form.value.businessName.trim(),
      category: form.value.category.trim(),
      phoneNumber: form.value.phoneNumber.trim()
    }

    // Password hanya dikirim jika diisi
    if (form.value.password.trim()) {
      requestData.password = form.value.password
    }

    const response = await fetch(`${apiBase}/api/auth/me`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        Accept: 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    const result = await response.json().catch(() => null)

    if (!response.ok) {
      throw new Error(
        result?.message ||
        result?.error ||
        'Gagal memperbarui profil.'
      )
    }

    pesan.value = 'Profil berhasil diperbarui!'
    tipePesan.value = 'success'

    form.value.password = ''

    // Kembali ke profil setelah berhasil
    router.push('/profile')

  } catch (error) {
    console.error('Gagal memperbarui profil:', error)
    pesan.value = error.message
    tipePesan.value = 'error'
  } finally {
    menyimpan.value = false
  }
}

function batal() {
  router.push('/profile')
}

onMounted(() => {
  ambilProfil()
})
</script>

<template>
  <main class="edit-page">
    <section class="edit-card">

      <!-- Header -->
      <header class="header">
        <p class="brand">JEMARI</p>
        <h1>Edit Profil UMKM</h1>
        <p class="description">
          Perbarui informasi akun dan usaha Anda.
        </p>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        Memuat data profil...
      </div>

      <!-- Form Edit Profil -->
      <form v-else @submit.prevent="simpanPerubahan">

        <h2>Informasi Profil Usaha</h2>

        <!-- 1. Nama Usaha -->
        <div class="form-group">
          <label for="businessName">Nama Usaha *</label>
          <input
            id="businessName"
            v-model.trim="form.businessName"
            type="text"
            placeholder="Masukkan nama usaha"
            required
          />
        </div>

        <!-- 2. Email Usaha -->
        <div class="form-group">
          <label for="email">Email Usaha</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            readonly
            disabled
          />
          <small>Email Usaha belum dapat diubah.</small>
        </div>

        <!-- 3. Nomor Telepon -->
        <div class="form-group">
          <label for="phoneNumber">Nomor Telepon</label>
          <input
            id="phoneNumber"
            v-model.trim="form.phoneNumber"
            type="tel"
            placeholder="081234567890"
          />
        </div>

        <!-- 4. Kategori Usaha -->
        <div class="form-group">
          <label for="category">Kategori Usaha *</label>
          <input
            id="category"
            v-model.trim="form.category"
            type="text"
            placeholder="Masukkan kategori usaha"
            required
          />
        </div>

        <!-- 5. Skala Usaha -->
        <div class="form-group">
          <label for="businessScale">Skala Usaha</label>
          <input
            id="businessScale"
            :value="form.businessScale"
            type="text"
            readonly
            disabled
          />
          <small>Skala Usaha belum dapat diubah.</small>
        </div>

        <!-- Password -->
        <h2>Keamanan Akun</h2>

        <div class="form-group">
          <label for="password">Password Baru</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            minlength="8"
            placeholder="Kosongkan jika tidak ingin mengubah password"
          />
          <small>
            Password lama tetap digunakan jika bagian ini dikosongkan.
            Minimal 8 karakter jika ingin menggantinya.
          </small>
        </div>

        <!-- Pesan -->
        <p
          v-if="pesan"
          class="message"
          :class="tipePesan"
          role="alert"
        >
          {{ pesan }}
        </p>

        <!-- Tombol -->
        <div class="button-group">
          <button
            type="submit"
            class="save-button"
            :disabled="menyimpan"
          >
            {{ menyimpan ? 'Menyimpan...' : 'Simpan Perubahan' }}
          </button>

          <button
            type="button"
            class="cancel-button"
            :disabled="menyimpan"
            @click="batal"
          >
            Batal
          </button>
        </div>

      </form>

      <!-- Pesan jika gagal mengambil data -->
      <div v-if="!loading && pesan && !form.email && !form.businessName"
           class="retry-section">
        <button
          type="button"
          class="retry-button"
          @click="ambilProfil"
        >
          Coba Lagi
        </button>
      </div>

    </section>
  </main>
</template>

<style scoped>
.edit-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f3ff;
  font-family: Arial, sans-serif;
}

.edit-card {
  max-width: 620px;
  margin: 0 auto;
  padding: 36px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.brand {
  color: #7c3aed;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 3px;
}

h1 {
  color: #1f2937;
}

h2 {
  margin: 28px 0 20px;
  color: #374151;
  font-size: 18px;
}

.description,
small {
  color: #6b7280;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

label {
  font-size: 14px;
  font-weight: bold;
  color: #374151;
}

input {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
}

input:focus {
  outline: none;
  border-color: #7c3aed;
}

input:disabled {
  background: #f3f4f6;
  color: #6b7280;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

button {
  flex: 1;
  padding: 13px;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.save-button {
  background: #7c3aed;
}

.save-button:hover {
  background: #6d28d9;
}

.save-button:disabled {
  background: #a78bfa;
  cursor: not-allowed;
}

.cancel-button {
  background: #6b7280;
}

.cancel-button:hover {
  background: #4b5563;
}

.cancel-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
}

.message.success {
  background: #dcfce7;
  color: #166534;
}

.message.error {
  background: #fee2e2;
  color: #991b1b;
}

.loading {
  padding: 30px;
  text-align: center;
  color: #6b7280;
}

.retry-section {
  text-align: center;
  margin-top: 16px;
}

.retry-button {
  background: #7c3aed;
}

@media (max-width: 600px) {
  .edit-card {
    padding: 24px 18px;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>
