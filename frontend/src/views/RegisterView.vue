
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const activeRole = ref('UMKM')
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Form UMKM
const umkmForm = ref({
  businessName: '',
  email: '',
  password: '',
  whatsapp: '',
  category: '',
  businessScale: ''
})

// Form Freelancer
const freelancerForm = ref({
  name: '',
  email: '',
  password: '',
  whatsapp: '',
  primaryCategory: ''
})

const businessScales = [
  { value: 'MIKRO', label: 'Mikro — omzet s.d. Rp 2 miliar/tahun' },
  { value: 'KECIL', label: 'Kecil — s.d. Rp 15 miliar/tahun' },
  { value: 'MENENGAH', label: 'Menengah — di atas Rp 15 miliar/tahun' },
]

const freelancerCategories = [
  'Desain Grafis',
  'Fotografi Produk',
  'Konten Media Sosial',
  'Website & IT',
  'Copywriting',
  'Video Editing',
  'Laporan Keuangan',
  'Lainnya'
]

const switchRole = (role) => {
  activeRole.value = role
  errorMessage.value = ''
  successMessage.value = ''
}

const handleRegister = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  // Endpoint backend yang tersedia saat ini hanya registrasi UMKM
  if (activeRole.value === 'FREELANCER') {
    errorMessage.value =
      'Registrasi Freelancer belum tersedia pada endpoint backend saat ini.'
    return
  }

  const form = umkmForm.value

  if (form.password.length < 8) {
    errorMessage.value = 'Kata sandi minimal 8 karakter.'
    return
  }

  isSubmitting.value = true

  try {
    const apiBase =
      import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

    const phoneNumber = form.whatsapp
      ? `+62${form.whatsapp.replace(/\D/g, '').replace(/^0+/, '')}`
      : ''

    const payload = {
      email: form.email.trim(),
      password: form.password,
      businessName: form.businessName.trim(),
      category: form.category.trim(),
      businessScale: form.businessScale,
      phoneNumber,
      contactEmail: form.email.trim()
    }

    const response = await axios.post(
      `${apiBase}/api/auth/register`,
      payload
    )

    successMessage.value =
      response.data?.message ||
      'Registrasi UMKM berhasil! Silakan login.'

    setTimeout(() => {
      router.push('/login')
    }, 1500)

  } catch (err) {
    console.error('Register Error:', err)
    console.error('Response:', err.response?.data)

    errorMessage.value =
    err.response?.data?.message ||
    err.response?.data?.error ||
    err.message ||
    'Registrasi gagal. Periksa backend.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="register-split-page">
    <!-- Kolom Kiri: Branding -->
    <aside class="brand-panel">
      <div class="brand-content">
        <h1 class="brand-title">JEMARI</h1>
        <p class="brand-subtitle">Jembatan Keahlian untuk Usahamu</p>

        <div class="handshake-emblem" aria-hidden="true">🤝</div>

        <div class="testimonial-card">
          <p class="quote-text">
            "JEMARI bantu saya dapat desainer kemasan dalam 2 hari!"
          </p>
          <div class="quote-author">
            <span>Rina, Kebab Nutrisix</span>
            <span>🥙</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Kolom Kanan: Form Pendaftaran -->
    <main class="form-panel">
      <div class="form-wrapper">
        <router-link to="/" class="back-link">
          ← Kembali ke Beranda
        </router-link>

        <div class="form-header">
          <h2>Daftar</h2>
          <p>Gratis mendaftar. Pilih peran Anda untuk memulai.</p>
        </div>

        <!-- Tabs Pemilih Peran -->
        <div class="role-tabs">
          <button
            type="button"
            :class="['tab-btn', { active: activeRole === 'UMKM' }]"
            @click="switchRole('UMKM')"
          >
            UMKM / Homemade
          </button>

          <button
            type="button"
            :class="['tab-btn', { active: activeRole === 'FREELANCER' }]"
            @click="switchRole('FREELANCER')"
          >
            Freelancer / Penyedia Jasa
          </button>
        </div>

        <!-- Alert -->
        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="alert alert-success">
          {{ successMessage }}
        </div>

        <!-- Form Pendaftaran -->
        <form @submit.prevent="handleRegister" class="register-form">

          <!-- Form UMKM -->
          <template v-if="activeRole === 'UMKM'">
            <div class="form-group">
              <label>Nama Usaha</label>
              <input
                v-model="umkmForm.businessName"
                type="text"
                placeholder="Contoh: Kebab Nutrisix"
                required
              />
            </div>

            <div class="form-group">
              <label>Email</label>
              <div class="input-with-icon">
                <span class="input-icon">✉️</span>
                <input
                  v-model.trim="umkmForm.email"
                  type="email"
                  placeholder="nama@usaha.id"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Kata Sandi</label>
              <div class="input-with-icon">
                <span class="input-icon">🔒</span>
                <input
                  v-model="umkmForm.password"
                  type="password"
                  minlength="8"
                  placeholder="Minimal 8 karakter"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Nomor WhatsApp</label>
              <div class="wa-input-group">
                <span class="wa-prefix">+62</span>
                <input
                  v-model="umkmForm.whatsapp"
                  type="tel"
                  placeholder="812-3456-7890"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Kategori Usaha</label>
              <input
                v-model.trim="umkmForm.category"
                type="text"
                placeholder="Contoh: Kuliner"
                required
              />
            </div>

            <div class="form-group">
              <label>Skala Usaha</label>
              <select v-model="umkmForm.businessScale" required>
                <option value="" disabled>Pilih skala usaha</option>
                <option
                  v-for="scale in businessScales"
                  :key="scale.value"
                  :value="scale.value"
                >
                  {{ scale.label }}
                </option>
              </select>
            </div>
          </template>

          <!-- Form Freelancer -->
          <template v-else>
            <div class="form-group">
              <label>Nama Lengkap</label>
              <input
                v-model="freelancerForm.name"
                type="text"
                placeholder="Contoh: Ahmad Rizki"
                required
              />
            </div>

            <div class="form-group">
              <label>Email</label>
              <div class="input-with-icon">
                <span class="input-icon">✉️</span>
                <input
                  v-model.trim="freelancerForm.email"
                  type="email"
                  placeholder="nama@email.com"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Kata Sandi</label>
              <div class="input-with-icon">
                <span class="input-icon">🔒</span>
                <input
                  v-model="freelancerForm.password"
                  type="password"
                  minlength="8"
                  placeholder="Minimal 8 karakter"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Nomor WhatsApp</label>
              <div class="wa-input-group">
                <span class="wa-prefix">+62</span>
                <input
                  v-model="freelancerForm.whatsapp"
                  type="tel"
                  placeholder="812-3456-7890"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label>Kategori Jasa Utama</label>
              <select
                v-model="freelancerForm.primaryCategory"
                required
              >
                <option value="" disabled>Pilih kategori</option>
                <option
                  v-for="cat in freelancerCategories"
                  :key="cat"
                  :value="cat"
                >
                  {{ cat }}
                </option>
              </select>
            </div>
          </template>

          <button
            type="submit"
            class="btn-submit-amber"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Memproses...' : 'Daftar Sekarang' }}
          </button>
        </form>

        <p class="login-redirect">
          Sudah punya akun?
          <router-link to="/login">Masuk di sini</router-link>
        </p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.register-split-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: #111827;
}

.brand-panel {
  background: linear-gradient(160deg, #065f46 0%, #1B6B46 60%, #15803d 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.brand-content {
  max-width: 360px;
  text-align: center;
}

.brand-title {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 8px;
}

.brand-subtitle {
  font-size: 0.95rem;
  color: #d1fae5;
  margin: 0 0 36px;
}

.handshake-emblem {
  font-size: 3.5rem;
  margin-bottom: 32px;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.15));
}

.testimonial-card {
  background: #ffffff;
  color: #1f2937;
  border-radius: 14px;
  padding: 20px 22px;
  text-align: left;
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.16);
}

.quote-text {
  font-size: 0.9rem;
  font-weight: 500;
  line-height: 1.5;
  margin: 0 0 10px;
  color: #111827;
}

.quote-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  font-weight: 600;
  color: #6b7280;
}

.form-panel {
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
}

.back-link {
  display: inline-block;
  font-size: 0.84rem;
  font-weight: 600;
  color: #1B6B46;
  text-decoration: none;
  margin-bottom: 24px;
}

.back-link:hover {
  text-decoration: underline;
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 800;
  margin: 0 0 4px;
  color: #111827;
}

.form-header p {
  font-size: 0.9rem;
  color: #6b7280;
  margin: 0 0 24px;
}

.role-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-bottom: 1.5px solid #e5e7eb;
  margin-bottom: 22px;
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2.5px solid transparent;
  padding: 10px 8px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  margin-bottom: -1.5px;
  transition: all 0.15s;
}

.tab-btn:hover {
  color: #111827;
}

.tab-btn.active {
  color: #1B6B46;
  border-bottom-color: #1B6B46;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  font-size: 0.84rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

input,
select {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: inherit;
  color: #111827;
  background-color: #ffffff;
}

input:focus,
select:focus {
  outline: none;
  border-color: #1B6B46;
  box-shadow: 0 0 0 3px rgba(27, 107, 70, 0.12);
}

.input-with-icon {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  font-size: 0.9rem;
  opacity: 0.6;
  pointer-events: none;
}

.input-with-icon input {
  padding-left: 36px;
}

.wa-input-group {
  display: flex;
  align-items: stretch;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  overflow: hidden;
}

.wa-input-group:focus-within {
  border-color: #1B6B46;
  box-shadow: 0 0 0 3px rgba(27, 107, 70, 0.12);
}

.wa-prefix {
  background: #f3f4f6;
  color: #374151;
  font-size: 0.88rem;
  font-weight: 600;
  padding: 10px 12px;
  border-right: 1px solid #d1d5db;
  display: flex;
  align-items: center;
}

.wa-input-group input {
  border: none;
  box-shadow: none;
}

.btn-submit-amber {
  margin-top: 6px;
  width: 100%;
  background-color: #F59E0B;
  color: #111827;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.15s;
}

.btn-submit-amber:hover:not(:disabled) {
  background-color: #d97706;
}

.btn-submit-amber:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.login-redirect {
  text-align: center;
  font-size: 0.85rem;
  color: #6b7280;
  margin-top: 20px;
}

.login-redirect a {
  color: #1B6B46;
  font-weight: 600;
  text-decoration: none;
}

.login-redirect a:hover {
  text-decoration: underline;
}

.alert {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  margin-bottom: 16px;
}

.alert-error {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.alert-success {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

@media (max-width: 860px) {
  .register-split-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    display: none;
  }
}
</style>
