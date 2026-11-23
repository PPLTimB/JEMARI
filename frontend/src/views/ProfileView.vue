
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const profil = ref(null)
const loading = ref(true)
const pesan = ref('')

const apiBase =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Mengambil token login
function getToken() {
  return (
    localStorage.getItem('jemari_token') ||
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken')
  )
}

// Menghapus data autentikasi Jemari
function hapusDataLogin() {
  localStorage.removeItem('jemari_token')
  localStorage.removeItem('token')
  localStorage.removeItem('accessToken')
  localStorage.removeItem('jemari_user')
  localStorage.removeItem('user')
}

// Mengambil profil akun UMKM
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
        result?.message || 'Gagal mengambil profil akun.'
      )
    }

    // Mendukung ApiResponse maupun respons langsung
    const profileData = result?.data ?? result

    profil.value = profileData

    console.log('Data profil berhasil diambil:', profileData)

  } catch (error) {
    console.error('Error mengambil profil:', error)
    pesan.value = error.message
  } finally {
    loading.value = false
  }
}

// Menu edit profil
function editProfil() {
  router.push('/profile/edit')
}

// Logout
function logout() {
  hapusDataLogin()
  router.push('/login')
}

// Menonaktifkan akun UMKM
async function nonaktifkanAkun() {
  const token = getToken()

  if (!token) {
    router.push('/login')
    return
  }

  const konfirmasi = window.confirm(
    'Apakah Anda yakin ingin menonaktifkan akun? Akun tidak dapat digunakan untuk login setelah dinonaktifkan.'
  )

  if (!konfirmasi) return

  try {
    const response = await fetch(`${apiBase}/api/auth/me`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json'
      }
    })

    const result = await response.json().catch(() => null)

    if (!response.ok) {
      throw new Error(
        result?.message || 'Gagal menonaktifkan akun.'
      )
    }

    hapusDataLogin()

    window.alert('Akun berhasil dinonaktifkan.')
    router.push('/login')

  } catch (error) {
    pesan.value = error.message
  }
}

onMounted(() => {
  ambilProfil()
})
</script>

<template>
  <main class="profile-page">
    <section class="profile-card">

      <!-- Header -->
      <header class="header">
        <p class="brand">JEMARI</p>
        <h1>Profil Akun UMKM</h1>
        <p class="description">
          Informasi akun usaha Anda.
        </p>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        Memuat informasi profil...
      </div>

      <!-- Informasi Profil -->
      <div v-else-if="profil">

        <div class="profile-info">

          <div class="info-item">
            <span>Nama Usaha</span>
            <strong>{{ profil.businessName || '-' }}</strong>
          </div>

          <div class="info-item">
            <span>Email Usaha</span>
            <strong>{{ profil.email || '-' }}</strong>
          </div>

          <div class="info-item">
            <span>Nomor Telepon</span>
            <strong>{{ profil.phoneNumber || '-' }}</strong>
          </div>

          <div class="info-item">
            <span>Kategori Usaha</span>
            <strong>{{ profil.category || '-' }}</strong>
          </div>

          <div class="info-item">
            <span>Skala Usaha</span>
            <strong class="scale">
              {{ profil.businessScale || '-' }}
            </strong>
          </div>

        </div>

        <!-- Tombol Aksi -->
        <div class="button-group">

          <button
            type="button"
            class="edit-button"
            @click="editProfil"
          >
            Edit Profil
          </button>

          <button
            type="button"
            class="logout-button"
            @click="logout"
          >
            Logout
          </button>

          <button
            type="button"
            class="delete-button"
            @click="nonaktifkanAkun"
          >
            Nonaktifkan Akun
          </button>

        </div>
      </div>

      <!-- Profil Tidak Tersedia -->
      <div v-else class="empty-profile">
        <p>Data profil belum dapat ditampilkan.</p>

        <button
          type="button"
          class="edit-button"
          @click="ambilProfil"
        >
          Coba Lagi
        </button>
      </div>

      <!-- Pesan Error -->
      <p
        v-if="pesan"
        class="message error"
        role="alert"
      >
        {{ pesan }}
      </p>

    </section>
  </main>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f3ff;
  font-family: Arial, sans-serif;
}

.profile-card {
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

.description {
  color: #6b7280;
}

.profile-info {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;
}

.info-item span {
  color: #6b7280;
  font-size: 13px;
}

.info-item strong {
  color: #1f2937;
  font-size: 15px;
  overflow-wrap: anywhere;
}

.scale {
  color: #7c3aed !important;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 28px;
}

button {
  flex: 1;
  min-width: 130px;
  padding: 13px;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.edit-button {
  background: #7c3aed;
}

.edit-button:hover {
  background: #6d28d9;
}

.logout-button {
  background: #6b7280;
}

.logout-button:hover {
  background: #4b5563;
}

.delete-button {
  background: #dc2626;
}

.delete-button:hover {
  background: #b91c1c;
}

.loading {
  padding: 30px;
  text-align: center;
  color: #6b7280;
}

.empty-profile {
  text-align: center;
  color: #6b7280;
  padding: 20px;
}

.message.error {
  padding: 12px;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 8px;
  margin-top: 20px;
}

@media (max-width: 600px) {
  .profile-card {
    padding: 24px 18px;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>
