<template>
  <div class="login-page">
    <div class="login-wrapper">
      <!-- Kartu Utama Login -->
      <div class="login-card">
        <!-- Header Kartu -->
        <div class="card-header">
          <span class="role-chip">Portal Akses JEMARI</span>
          <h1 class="page-title">Masuk ke Akun</h1>
          <p class="page-subtitle">
            Kelola kebutuhan jasa UMKM dan portofolio layanan freelancer dalam satu ruang kolaborasi.
          </p>
        </div>

        <!-- Alert Error -->
        <div v-if="errorMessage" class="alert-error">
          {{ errorMessage }}
        </div>

        <!-- Form Login -->
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email" class="form-label">EMAIL TERDAFTAR</label>
            <input
              id="email"
              v-model.trim="form.email"
              type="email"
              class="form-input"
              placeholder="nama@email.com"
              required
              autocomplete="email"
            />
          </div>

          <div class="form-group">
            <div class="label-row">
              <label for="password" class="form-label">KATA SANDI</label>
            </div>
            <div class="password-wrapper">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="Masukkan kata sandi"
                required
                autocomplete="current-password"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword = !showPassword"
              >
                {{ showPassword ? 'Sembunyikan' : 'Tampilkan' }}
              </button>
            </div>
          </div>

          <button type="submit" class="btn-submit" :disabled="isLoading">
            {{ isLoading ? 'Memproses...' : 'Masuk Sekarang' }}
          </button>
        </form>

        <!-- Footer Kartu -->
        <div class="card-footer">
          <span>Belum memiliki akun JEMARI?</span>
          <router-link to="/register" class="link-register">
            Daftar Sekarang &rarr;
          </router-link>
        </div>
      </div>

      <!-- Strip Informasi Peran (Senada dengan Status Pill) -->
      <div class="info-strip">
        <div class="info-item">
          <span class="dot dot-blue"></span>
          <span><strong>UMKM:</strong> Buat request jasa &amp; pantau progres pesanan</span>
        </div>
        <div class="info-item">
          <span class="dot dot-emerald"></span>
          <span><strong>Freelancer:</strong> Kelola paket layanan &amp; portofolio</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const form = reactive({
  email: '',
  password: ''
})

const showPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const decodeRoleFromToken = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(window.atob(base64))
    const rawRole =
      payload.role ||
      (Array.isArray(payload.roles) ? payload.roles[0] : null) ||
      (Array.isArray(payload.authorities) ? payload.authorities[0] : null)
    return rawRole ? String(rawRole).replace(/^ROLE_/i, '').toUpperCase() : null
  } catch (_) {
    return null
  }
}

const handleLogin = async () => {
  errorMessage.value = ''
  isLoading.value = true

  try {
    const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const response = await fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: form.email,
        password: form.password
      })
    })

    const data = await response.json().catch(() => ({}))

    if (!response.ok) {
      throw new Error(data.message || data.error || 'Email atau kata sandi tidak valid.')
    }

    const token = data.token || data.accessToken || data.jwt || data.data?.token
    if (!token) {
      throw new Error('Token autentikasi tidak ditemukan pada respons server.')
    }

    const resolvedRole = (
      data.role ||
      data.user?.role ||
      decodeRoleFromToken(token) ||
      ''
    )
      .replace(/^ROLE_/i, '')
      .toUpperCase()

    // Simpan token & profil user agar Navbar langsung memperbarui badge peran
    localStorage.setItem('jemari_token', token)
    localStorage.setItem('token', token)
    localStorage.setItem(
      'jemari_user',
      JSON.stringify({
        id: data.id || data.user?.id || null,
        name: data.name || data.fullName || data.user?.name || form.email,
        email: data.email || form.email,
        role: resolvedRole
      })
    )

    window.dispatchEvent(new Event('storage'))

    // Arahkan sesuai query redirect atau halaman utama masing-masing role
    if (route.query.redirect) {
      router.push(String(route.query.redirect))
    } else if (resolvedRole === 'FREELANCER') {
      router.push('/freelancer/packages')
    } else if (resolvedRole === 'UMKM') {
      router.push('/request-saya')
    } else {
      router.push('/')
    }
  } catch (err) {
    errorMessage.value = err.message || 'Gagal terhubung ke server.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 68px);
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login-card {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.card-header {
  margin-bottom: 22px;
}

.role-chip {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  padding: 4px 10px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.4px;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

.alert-error {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13px;
  font-weight: 500;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 18px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-label {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.4px;
  color: #64748b;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  font-size: 14px;
  color: #0f172a;
  background-color: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 10px 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  box-sizing: border-box;
}

.password-wrapper .form-input {
  padding-right: 92px;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.toggle-password {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  cursor: pointer;
  padding: 4px 6px;
}

.toggle-password:hover {
  color: #1d4ed8;
}

.btn-submit {
  margin-top: 4px;
  width: 100%;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  background-color: #2563eb;
  border: none;
  border-radius: 8px;
  padding: 11px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn-submit:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.card-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #64748b;
}

.link-register {
  font-weight: 600;
  color: #1d4ed8;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 7px;
  transition: all 0.2s ease;
}

.link-register:hover {
  background-color: #dbeafe;
}

.info-strip {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #475569;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  flex-shrink: 0;
}

.dot-blue {
  background-color: #2563eb;
}

.dot-emerald {
  background-color: #10b981;
}
</style>