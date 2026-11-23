<template>
  <header class="navbar">
    <div class="nav-container">
      <!-- Brand Logo -->
      <div class="brand-section">
        <router-link to="/" class="brand-logo">
          <span class="logo-text">JEMARI</span>
        </router-link>
        <span class="brand-tagline">Jembatan Ekonomi Mandiri Rakyat Indonesia</span>
      </div>

      <!-- Combined Navigation Links (Biru Style) -->
      <nav class="nav-links">
        <a href="/#cara-kerja" @click="handleScroll('cara-kerja')" class="nav-item">Cara Kerja</a>
        <a href="/#peran" @click="handleScroll('peran')" class="nav-item">Peran</a>
        <a href="/#katalog" @click="handleScroll('katalog')" class="nav-item">Katalog</a>
        <router-link
          v-if="userRole !== 'UMKM'"
          to="/freelancer/packages"
          class="nav-item"
          active-class="active"
        >
          Kelola Jasa
        </router-link>
        <router-link
          v-if="userRole !== 'FREELANCER'"
          to="/request-saya"
          class="nav-item"
          active-class="active"
        >
          Request Saya
        </router-link>
        <!-- Profil Akun UMKM - PBI 10 -->
        <router-link
          v-if="userRole === 'UMKM'"
          to="/profile"
          class="nav-item"
          active-class="active"
        >
          Profil Akun
        </router-link>

        <!-- Profil Usaha UMKM - PBI 09 -->
        <router-link
          v-if="userRole === 'UMKM'"
          to="/umkm/profile"
          class="nav-item"
          active-class="active"
        >
          Profil Usaha
        </router-link>
        <router-link
          v-if="userRole !== 'UMKM'"
          to="/portfolio"
          class="nav-item"
          active-class="active"
        >
          Portofolio
        </router-link>
      </nav>

      <!-- Action Buttons & Badge -->
      <div class="nav-actions">
        <router-link
          v-if="userRole !== 'FREELANCER'"
          to="/request/buat"
          class="nav-btn-create"
        >
          + Buat Request
        </router-link>

        <!-- Tampil saat BELUM login -->
        <template v-if="!userRole">
          <router-link to="/login" class="nav-btn-login">
            Masuk
          </router-link>
          <router-link to="/register" class="nav-btn-register">
            Daftar
          </router-link>
        </template>

        <!-- Tampil saat SUDAH login -->
        <template v-else>
          <div class="user-badge">
            <span class="role-pill" :class="roleBadgeClass">
              Peran: {{ userRole }}
            </span>
          </div>
          <button type="button" @click="handleLogout" class="nav-btn-logout">
            Logout
          </button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const authTick = ref(0)

const refreshAuth = () => {
  authTick.value++
}

onMounted(() => {
  window.addEventListener('storage', refreshAuth)
})

onUnmounted(() => {
  window.removeEventListener('storage', refreshAuth)
})

watch(() => route.fullPath, refreshAuth)

const userRole = computed(() => {
  void authTick.value

  // 1. Cek data objek user di localStorage
  const rawUser = localStorage.getItem('user') || localStorage.getItem('jemari_user')
  if (rawUser) {
    try {
      const parsed = JSON.parse(rawUser)
      if (parsed?.role) {
        return String(parsed.role).replace(/^ROLE_/i, '').toUpperCase()
      }
    } catch (_) {}
  }

  // 2. Decode payload JWT dari token yang tersimpan di localStorage
  const token =
    localStorage.getItem('jemari_token') ||
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken')

  if (!token || !token.includes('.')) return null

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
})

const roleBadgeClass = computed(() => {
  if (userRole.value === 'FREELANCER') return 'role-freelancer'
  if (userRole.value === 'UMKM') return 'role-umkm'
  if (userRole.value === 'ADMIN') return 'role-admin'
  return ''
})

const handleLogout = () => {
  localStorage.removeItem('jemari_token')
  localStorage.removeItem('token')
  localStorage.removeItem('accessToken')
  localStorage.removeItem('jemari_user')
  localStorage.removeItem('user')
  refreshAuth()
  router.push('/login')
}

const handleScroll = (id) => {
  if (route.path === '/') {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push({ path: '/', hash: `#${id}` })
  }
}
</script>

<style scoped>
.navbar {
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  text-decoration: none;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: #1d4ed8;
}

.brand-tagline {
  font-size: 12px;
  color: #6b7280;
  border-left: 1px solid #e5e7eb;
  padding-left: 12px;
  display: none;
}

@media (min-width: 900px) {
  .brand-tagline {
    display: inline;
  }
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 500;
  color: #4b5563;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.nav-item:hover {
  color: #1d4ed8;
  background-color: #eff6ff;
}

.nav-item.active {
  color: #1d4ed8;
  background-color: #dbeafe;
  font-weight: 600;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-btn-create {
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 600;
  background-color: #2563eb;
  color: #ffffff;
  padding: 8px 14px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.nav-btn-create:hover {
  background-color: #1d4ed8;
}

.nav-btn-login {
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 600;
  color: #374151;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
  padding: 7px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-btn-login:hover {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.nav-btn-register {
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 600;
  color: #1d4ed8;
  border: 1px solid #93c5fd;
  background-color: #eff6ff;
  padding: 7px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-btn-register:hover {
  background-color: #dbeafe;
  border-color: #60a5fa;
}

.nav-btn-logout {
  font-size: 13px;
  font-weight: 600;
  color: #dc2626;
  border: 1px solid #fecaca;
  background-color: #fef2f2;
  padding: 7px 13px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-btn-logout:hover {
  background-color: #fee2e2;
  border-color: #fca5a5;
}

.user-badge {
  display: flex;
  align-items: center;
}

.role-pill {
  font-size: 12px;
  font-weight: 600;
  background-color: #f3f4f6;
  color: #374151;
  padding: 4px 10px;
  border-radius: 9999px;
  border: 1px solid #e5e7eb;
}

.role-pill.role-freelancer {
  background-color: #eff6ff;
  color: #1d4ed8;
  border-color: #bfdbfe;
}

.role-pill.role-umkm {
  background-color: #ecfdf5;
  color: #047857;
  border-color: #a7f3d0;
}

.role-pill.role-admin {
  background-color: #fef3c7;
  color: #b45309;
  border-color: #fde68a;
}
</style>