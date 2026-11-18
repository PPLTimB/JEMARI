<template>
  <header class="navbar">
    <div class="nav-container">
      <div class="brand-section">
        <router-link to="/request-saya" class="brand-logo">
          <span class="logo-text">JEMARI</span>
        </router-link>
        <span class="brand-tagline">Jembatan Ekonomi Mandiri Rakyat Indonesia</span>
      </div>

      <nav class="nav-links">
        <router-link to="/request-saya" class="nav-item" active-class="active">
          Request Saya
        </router-link>
        <router-link to="/request/buat" class="nav-item nav-btn-create" active-class="active">
          + Buat Request
        </router-link>
        <router-link to="/portfolio" class="nav-item" active-class="active">
          Portofolio
        </router-link>
      </nav>

      <div class="user-badge">
        <span class="role-pill">Peran: UMKM</span>
      </div>
    </div>
  </header>
</template>

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
  max-width: 1080px;
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

@media (min-width: 768px) {
  .brand-tagline {
    display: inline;
  }
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  color: #4b5563;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
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

.nav-btn-create {
  background-color: #2563eb;
  color: #ffffff !important;
}

.nav-btn-create:hover {
  background-color: #1d4ed8 !important;
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
