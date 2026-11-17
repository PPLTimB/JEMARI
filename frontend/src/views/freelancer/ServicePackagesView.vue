<script setup>
import { ref, computed, onMounted } from 'vue';
import servicePackageService from '../../services/servicePackageService';

const packages = ref([]);
const isLoading = ref(false);
const isSubmitting = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const selectedFilter = ref('Semua');

// State Form & Edit Mode
const showForm = ref(false);
const isEditing = ref(false);
const editingId = ref(null);

const form = ref({
  title: '',
  category: '',
  description: '',
  basePrice: ''
});

const categories = [
  { name: 'Desain Grafis', icon: '🎨' },
  { name: 'Fotografi Produk', icon: '📷' },
  { name: 'Konten Media Sosial', icon: '📱' },
  { name: 'Website & IT', icon: '💻' },
  { name: 'Copywriting', icon: '✍️' },
  { name: 'Video Editing', icon: '🎬' },
  { name: 'Laporan Keuangan', icon: '📊' }
];

const getCategoryIcon = (catName) => {
  const found = categories.find((c) => c.name === catName);
  return found ? found.icon : '✨';
};

const formatRupiah = (value) => {
  return new Intl.NumberFormat('id-ID', {
    minimumFractionDigits: 0
  }).format(value || 0);
};

const filteredPackages = computed(() => {
  if (selectedFilter.value === 'Semua') {
    return packages.value;
  }
  return packages.value.filter((pkg) => pkg.category === selectedFilter.value);
});

const averagePrice = computed(() => {
  if (packages.value.length === 0) return 0;
  const total = packages.value.reduce((sum, item) => sum + Number(item.basePrice || 0), 0);
  return Math.round(total / packages.value.length);
});

const fetchMyPackages = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await servicePackageService.getMyPackages();
    packages.value = Array.isArray(response.data) ? response.data : [];
  } catch (err) {
    if (err.response?.status === 401 || err.response?.status === 403) {
      errorMessage.value = 'Sesi autentikasi tidak ditemukan atau kedaluwarsa. Pastikan token JWT Freelancer tersimpan di localStorage.';
    } else {
      errorMessage.value = err.response?.data?.message || 'Gagal memuat daftar paket jasa.';
    }
  } finally {
    isLoading.value = false;
  }
};

const openCreateForm = () => {
  isEditing.value = false;
  editingId.value = null;
  form.value = { title: '', category: '', description: '', basePrice: '' };
  showForm.value = true;
  errorMessage.value = '';
  successMessage.value = '';
};

const openEditForm = (pkg) => {
  isEditing.value = true;
  editingId.value = pkg.id;
  form.value = {
    title: pkg.title,
    category: pkg.category,
    description: pkg.description || '',
    basePrice: pkg.basePrice
  };
  showForm.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const closeForm = () => {
  showForm.value = false;
  isEditing.value = false;
  editingId.value = null;
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  const payload = {
    title: form.value.title,
    category: form.value.category,
    description: form.value.description,
    basePrice: Number(form.value.basePrice)
  };

  try {
    if (isEditing.value) {
      await servicePackageService.updatePackage(editingId.value, payload);
      successMessage.value = 'Perubahan paket jasa berhasil disimpan.';
    } else {
      await servicePackageService.createPackage(payload);
      successMessage.value = 'Paket jasa baru berhasil diterbitkan ke katalog.';
    }
    closeForm();
    await fetchMyPackages();
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Terjadi kesalahan saat menyimpan paket jasa.';
  } finally {
    isSubmitting.value = false;
  }
};

const handleDelete = async (pkg) => {
  const confirmed = window.confirm(`Nonaktifkan paket jasa "${pkg.title}" dari katalog publik?`);
  if (!confirmed) return;

  errorMessage.value = '';
  successMessage.value = '';
  try {
    await servicePackageService.deletePackage(pkg.id);
    successMessage.value = `Paket "${pkg.title}" telah dinonaktifkan.`;
    await fetchMyPackages();
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Gagal menonaktifkan paket jasa.';
  }
};

onMounted(() => {
  fetchMyPackages();
});
</script>

<template>
  <div class="jemari-page">
    <!-- Top Navigation Bar (Selaras HomeView) -->
    <header class="navbar">
      <div class="container nav-inner">
        <router-link to="/" class="brand-logo">JEMARI</router-link>

        <nav class="nav-links">
          <router-link to="/">Beranda &amp; Katalog</router-link>
          <router-link to="/freelancer/service-packages" class="active-link">Kelola Paket Jasa</router-link>
        </nav>

        <button class="btn-amber" @click="openCreateForm">+ Paket Baru</button>
      </div>
    </header>

    <!-- Dashboard Header Banner -->
    <section class="workspace-banner">
      <div class="container banner-grid">
        <div>
          <div class="role-pill">
            <span>💼</span> Workspace Freelancer
          </div>
          <h1>Kelola Paket Jasa</h1>
          <p class="banner-desc">
            Atur penawaran jasa dengan harga pasti dan ruang lingkup jelas agar pelaku UMKM mudah berkolaborasi.
          </p>
        </div>

        <!-- Ringkasan Statistik Freelancer -->
        <div class="stats-summary-card">
          <div class="stat-item">
            <span class="stat-label">Paket Aktif</span>
            <span class="stat-num">{{ packages.length }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">Rata-rata Tarif</span>
            <span class="stat-num emerald">Rp {{ formatRupiah(averagePrice) }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">Status Katalog</span>
            <span class="status-dot-badge">● Tayang</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <main class="container main-content">
      <!-- Notifikasi Error / Sukses -->
      <div v-if="errorMessage" class="alert alert-error">
        <span>⚠️</span> {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="alert alert-success">
        <span>✅</span> {{ successMessage }}
      </div>

      <!-- Form Modal / Card Tambah & Edit -->
      <div v-if="showForm" class="form-panel">
        <div class="form-panel-header">
          <div>
            <h3>{{ isEditing ? 'Edit Paket Jasa' : 'Buat Paket Jasa Baru' }}</h3>
            <p>Lengkapi detail layanan yang akan tampil di katalog UMKM.</p>
          </div>
          <button type="button" class="btn-close-icon" @click="closeForm" aria-label="Tutup">✕</button>
        </div>

        <form @submit.prevent="handleSubmit" class="package-form">
          <div class="form-group">
            <label>Judul Paket Jasa <span class="req">*</span></label>
            <input
              v-model="form.title"
              type="text"
              maxlength="150"
              placeholder="Contoh: Desain Logo & Kemasan Produk UMKM"
              required
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Kategori Jasa <span class="req">*</span></label>
              <select v-model="form.category" required>
                <option value="" disabled>Pilih kategori keahlian</option>
                <option v-for="cat in categories" :key="cat.name" :value="cat.name">
                  {{ cat.icon }} {{ cat.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Harga Dasar (IDR) <span class="req">*</span></label>
              <div class="price-input-wrap">
                <span class="currency-prefix">Rp</span>
                <input
                  v-model="form.basePrice"
                  type="number"
                  min="1000"
                  step="500"
                  placeholder="250000"
                  required
                />
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Deskripsi &amp; Ruang Lingkup Layanan</label>
            <textarea
              v-model="form.description"
              rows="4"
              placeholder="Jelaskan output file yang didapat klien, estimasi hari pengerjaan, dan batas revisi..."
            ></textarea>
          </div>

          <div class="form-footer">
            <button type="button" class="btn-outline" @click="closeForm">Batal</button>
            <button type="submit" class="btn-emerald" :disabled="isSubmitting">
              {{ isSubmitting ? 'Menyimpan...' : (isEditing ? 'Simpan Perubahan' : 'Terbitkan Paket') }}
            </button>
          </div>
        </form>
      </div>

      <!-- Toolbar Filter Kategori -->
      <div class="toolbar-row">
        <div class="filter-pills">
          <button
            :class="['filter-pill', { active: selectedFilter === 'Semua' }]"
            @click="selectedFilter = 'Semua'"
          >
            Semua ({{ packages.length }})
          </button>
          <button
            v-for="cat in categories"
            :key="cat.name"
            :class="['filter-pill', { active: selectedFilter === cat.name }]"
            @click="selectedFilter = cat.name"
          >
            <span>{{ cat.icon }}</span> {{ cat.name }}
          </button>
        </div>

        <button v-if="!showForm" class="btn-emerald" @click="openCreateForm">
          + Tambah Paket Jasa
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="state-box">
        Memuat daftar paket jasa Anda...
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredPackages.length === 0" class="state-box empty">
        <div class="empty-icon">📦</div>
        <h3>{{ selectedFilter === 'Semua' ? 'Belum Ada Paket Jasa Aktif' : `Tidak Ada Paket di Kategori ${selectedFilter}` }}</h3>
        <p>
          Mulai buat paket jasa pertama Anda dengan harga tetap dan deskripsi yang jelas untuk menarik klien UMKM.
        </p>
        <button class="btn-amber" @click="openCreateForm">+ Buat Paket Pertama</button>
      </div>

      <!-- Grid Kartu Paket Jasa (Gaya Katalog JEMARI) -->
      <div v-else class="packages-grid">
        <article v-for="pkg in filteredPackages" :key="pkg.id" class="package-card">
          <div class="card-banner">
            <span class="cat-badge">{{ getCategoryIcon(pkg.category) }} {{ pkg.category }}</span>
            <span class="active-badge">Aktif</span>
            <div class="card-art">{{ getCategoryIcon(pkg.category) }}</div>
          </div>

          <div class="card-content">
            <h3 class="pkg-title">{{ pkg.title }}</h3>
            <div class="pkg-price">Mulai dari <strong>Rp {{ formatRupiah(pkg.basePrice) }}</strong></div>
            <p class="pkg-desc">
              {{ pkg.description || 'Belum ada rincian deskripsi layanan pada paket ini.' }}
            </p>

            <div class="card-actions">
              <button class="btn-card-edit" @click="openEditForm(pkg)">
                ✏️ Edit
              </button>
              <button class="btn-card-delete" @click="handleDelete(pkg)">
                Nonaktifkan
              </button>
            </div>
          </div>
        </article>
      </div>
    </main>

    <!-- Footer Emerald -->
    <footer class="jemari-footer">
      <div class="container footer-inner">
        <div>
          <span class="footer-logo">JEMARI</span>
          <span class="footer-tagline"> — Jembatan Keahlian untuk Usahamu</span>
        </div>
        <div class="footer-copy">© 2026 JEMARI. Seluruh hak dilindungi.</div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.jemari-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: #111827;
  background-color: #f8faf9;
}

.container {
  max-width: 1140px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* Navbar */
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 40;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
}

.brand-logo {
  font-weight: 800;
  font-size: 1.25rem;
  color: #1B6B46;
  text-decoration: none;
  letter-spacing: -0.02em;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  text-decoration: none;
  color: #4b5563;
  font-size: 0.92rem;
  font-weight: 500;
}

.nav-links a:hover,
.nav-links a.active-link {
  color: #1B6B46;
  font-weight: 600;
}

/* Workspace Banner */
.workspace-banner {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 36px 0;
}

.banner-grid {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #e6f4ed;
  color: #1B6B46;
  font-size: 0.78rem;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
  margin-bottom: 10px;
}

.workspace-banner h1 {
  font-size: 1.85rem;
  font-weight: 800;
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.banner-desc {
  color: #6b7280;
  font-size: 0.95rem;
  margin: 0;
  max-width: 540px;
}

.stats-summary-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px 22px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 2px;
}

.stat-num {
  font-size: 1.1rem;
  font-weight: 800;
  color: #111827;
}

.stat-num.emerald {
  color: #1B6B46;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: #e5e7eb;
}

.status-dot-badge {
  font-size: 0.8rem;
  font-weight: 700;
  color: #047857;
  background: #d1fae5;
  padding: 3px 10px;
  border-radius: 999px;
}

/* Main Content */
.main-content {
  padding: 32px 20px 64px;
  flex-grow: 1;
}

/* Alerts */
.alert {
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
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

/* Form Panel */
.form-panel {
  background: #ffffff;
  border: 1px solid #d1fae5;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: 0 12px 28px rgba(27, 107, 70, 0.08);
}

.form-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f3f4f6;
}

.form-panel-header h3 {
  margin: 0 0 4px;
  font-size: 1.2rem;
  font-weight: 700;
  color: #111827;
}

.form-panel-header p {
  margin: 0;
  font-size: 0.86rem;
  color: #6b7280;
}

.btn-close-icon {
  background: #f3f4f6;
  border: none;
  color: #4b5563;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 0.95rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

label {
  font-size: 0.88rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.req {
  color: #dc2626;
}

input, select, textarea {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.92rem;
  font-family: inherit;
  color: #111827;
  background: #ffffff;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #1B6B46;
  box-shadow: 0 0 0 3px rgba(27, 107, 70, 0.12);
}

.price-input-wrap {
  display: flex;
  align-items: center;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
}

.price-input-wrap:focus-within {
  border-color: #1B6B46;
  box-shadow: 0 0 0 3px rgba(27, 107, 70, 0.12);
}

.currency-prefix {
  background: #f3f4f6;
  color: #4b5563;
  font-weight: 600;
  font-size: 0.9rem;
  padding: 10px 12px;
  border-right: 1px solid #d1d5db;
}

.price-input-wrap input {
  border: none;
  box-shadow: none;
  width: 100%;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

/* Toolbar & Filter Pills */
.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-pills {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-pill {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
}

.filter-pill:hover,
.filter-pill.active {
  background: #ecfdf5;
  border-color: #1B6B46;
  color: #1B6B46;
  font-weight: 600;
}

/* Buttons */
button {
  cursor: pointer;
  font-family: inherit;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.15s ease;
}

.btn-amber {
  background-color: #F59E0B;
  color: #111827;
  border: none;
  padding: 9px 18px;
  font-size: 0.88rem;
}

.btn-amber:hover {
  background-color: #d97706;
}

.btn-emerald {
  background-color: #1B6B46;
  color: #ffffff;
  border: none;
  padding: 10px 18px;
  font-size: 0.88rem;
}

.btn-emerald:hover {
  background-color: #145336;
}

.btn-emerald:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-outline {
  background: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
  padding: 10px 18px;
  font-size: 0.88rem;
}

.btn-outline:hover {
  background: #f9fafb;
}

/* Grid Kartu Paket Jasa */
.packages-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.package-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.15s, box-shadow 0.15s;
}

.package-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.06);
}

.card-banner {
  background: #eceef1;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cat-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.94);
  color: #1B6B46;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.active-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #d1fae5;
  color: #065f46;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 999px;
}

.card-art {
  font-size: 2.2rem;
  opacity: 0.5;
}

.card-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.pkg-title {
  font-size: 1.02rem;
  font-weight: 700;
  margin: 0 0 6px;
  color: #111827;
  line-height: 1.4;
}

.pkg-price {
  font-size: 0.88rem;
  color: #4b5563;
  margin-bottom: 12px;
}

.pkg-price strong {
  color: #1B6B46;
  font-size: 0.98rem;
}

.pkg-desc {
  font-size: 0.86rem;
  color: #6b7280;
  line-height: 1.55;
  margin: 0 0 18px;
  flex-grow: 1;
}

.card-actions {
  display: flex;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid #f3f4f6;
}

.btn-card-edit {
  flex: 1;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  color: #1f2937;
  padding: 8px 12px;
  font-size: 0.84rem;
}

.btn-card-edit:hover {
  background: #e5e7eb;
}

.btn-card-delete {
  flex: 1;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  padding: 8px 12px;
  font-size: 0.84rem;
}

.btn-card-delete:hover {
  background: #fee2e2;
}

/* Empty & Loading State */
.state-box {
  background: #ffffff;
  border: 1px dashed #d1d5db;
  border-radius: 16px;
  padding: 48px 24px;
  text-align: center;
  color: #6b7280;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.state-box h3 {
  color: #111827;
  margin: 0 0 8px;
  font-size: 1.15rem;
}

.state-box p {
  max-width: 440px;
  margin: 0 auto 20px;
  font-size: 0.9rem;
}

/* Footer */
.jemari-footer {
  background-color: #1B6B46;
  color: #ffffff;
  padding: 20px 0;
  margin-top: auto;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-logo {
  font-weight: 800;
  font-size: 1.05rem;
}

.footer-tagline,
.footer-copy {
  font-size: 0.82rem;
  color: #d1fae5;
}

/* Responsive */
@media (max-width: 900px) {
  .packages-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .packages-grid,
  .form-row {
    grid-template-columns: 1fr;
  }
  .stats-summary-card {
    width: 100%;
    justify-content: space-between;
  }
}
</style>