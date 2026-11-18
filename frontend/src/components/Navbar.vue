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
        <router-link to="/freelancer/packages" class="nav-item" active-class="active">Kelola Jasa</router-link>
        <router-link to="/request-saya" class="nav-item" active-class="active">Request Saya</router-link>
        <router-link to="/portfolio" class="nav-item" active-class="active">Portofolio</router-link>
      </nav>

      <!-- Action Buttons & Badge -->
      <div class="nav-actions">
        <router-link to="/request/buat" class="nav-btn-create">
          + Buat Request
        </router-link>
        <router-link to="/register" class="nav-btn-register">
          Daftar
        </router-link>
        <div class="user-badge">
          <span class="role-pill">Peran: UMKM</span>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

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
</style>