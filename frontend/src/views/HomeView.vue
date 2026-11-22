<script setup>
import { ref, computed, onMounted } from 'vue';
import servicePackageService from '../services/servicePackageService';

// State Katalog & Filter
const selectedCategory = ref('Semua');
const apiPackages = ref([]);
const isLoading = ref(false);
const dummyNotice = ref('');

const categories = [
  { name: 'Semua', icon: '✨' },
  { name: 'Desain Grafis', icon: '🎨' },
  { name: 'Fotografi Produk', icon: '📷' },
  { name: 'Konten Media Sosial', icon: '📱' },
  { name: 'Website & IT', icon: '💻' },
  { name: 'Copywriting', icon: '✍️' },
  { name: 'Video Editing', icon: '🎬' },
  { name: 'Laporan Keuangan', icon: '📊' }
];

// Kolase 2x2 representasi ekosistem UMKM & Freelancer
const showcaseTiles = [
  {
    title: 'Pelaku UMKM Kuliner & Ritel',
    tag: 'UMKM Lokal',
    emoji: '🧑‍🍳',
    bg: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)',
    accent: '#92400e'
  },
  {
    title: 'Desainer & Kreator Visual',
    tag: 'Adobe & Figma',
    emoji: '🎨',
    bg: 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)',
    accent: '#1e40af'
  },
  {
    title: 'Fotografi Produk & Menu',
    tag: 'Katalog Siap Jual',
    emoji: '🍔',
    bg: 'linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%)',
    accent: '#9a3412'
  },
  {
    title: 'Konten & Media Sosial',
    tag: 'Digital Branding',
    emoji: '📱',
    bg: 'linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%)',
    accent: '#075985'
  }
];

const fallbackPackages = [
  {
    id: 'demo-1',
    category: 'Website & IT',
    title: 'Toko Online Sederhana (hingga 50 Produk)',
    basePrice: 1200000,
    freelancerName: 'Dewi Lestari',
    rating: '4.9',
    reviews: 20
  },
  {
    id: 'demo-2',
    category: 'Fotografi Produk',
    title: 'Foto Katalog Produk Studio (hingga 20 Produk)',
    basePrice: 500000,
    freelancerName: 'Siti Nurhaliza',
    rating: '4.9',
    reviews: 32
  },
  {
    id: 'demo-3',
    category: 'Website & IT',
    title: 'Setup & Optimasi Akun Google Bisnisku',
    basePrice: 150000,
    freelancerName: 'Dewi Lestari',
    rating: '4.9',
    reviews: 20
  },
  {
    id: 'demo-4',
    category: 'Fotografi Produk',
    title: 'Foto Menu & Interior Restoran',
    basePrice: 600000,
    freelancerName: 'Siti Nurhaliza',
    rating: '4.9',
    reviews: 32
  },
  {
    id: 'demo-5',
    category: 'Website & IT',
    title: 'Landing Page Usaha + Domain Setup',
    basePrice: 750000,
    freelancerName: 'Dewi Lestari',
    rating: '4.9',
    reviews: 20
  },
  {
    id: 'demo-6',
    category: 'Desain Grafis',
    title: 'Banner Spanduk Toko & X-Banner',
    basePrice: 180000,
    freelancerName: 'Ahmad Rizki',
    rating: '4.8',
    reviews: 24
  },
  {
    id: 'demo-7',
    category: 'Copywriting',
    title: 'Caption & Copy Konten Sosmed (20 Caption)',
    basePrice: 150000,
    freelancerName: 'Maya Anggraini',
    rating: '4.8',
    reviews: 28
  },
  {
    id: 'demo-8',
    category: 'Copywriting',
    title: 'Deskripsi Produk Copywriting (10 SKU)',
    basePrice: 200000,
    freelancerName: 'Maya Anggraini',
    rating: '4.8',
    reviews: 28
  }
];

const formatRupiah = (value) => {
  return new Intl.NumberFormat('id-ID', {
    minimumFractionDigits: 0
  }).format(value || 0);
};

const displayedPackages = computed(() => {
  const mappedApi = apiPackages.value.map((pkg) => ({
    id: pkg.id,
    category: pkg.category,
    title: pkg.title,
    basePrice: pkg.basePrice,
    freelancerName: `Freelancer #${pkg.freelancerId}`,
    rating: '5.0',
    reviews: 12,
    isFromDb: true
  }));

  const combined = mappedApi.length > 0 ? [...mappedApi, ...fallbackPackages] : fallbackPackages;

  if (selectedCategory.value === 'Semua') {
    return combined.slice(0, 8);
  }
  return combined.filter((item) => item.category === selectedCategory.value);
});

const handleDummyClick = (roleLabel) => {
  dummyNotice.value = `Tombol "${roleLabel}" adalah mode pratinjau (dummy). Gunakan menu Katalog atau Kelola Jasa untuk pengujian fitur aktif.`;
  setTimeout(() => {
    dummyNotice.value = '';
  }, 4000);
};

const scrollToSection = (sectionId) => {
  const el = document.getElementById(sectionId);
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' });
  }
};

const fetchPackagesFromBackend = async () => {
  isLoading.value = true;
  try {
    const res = await servicePackageService.getAllActivePackages();
    if (Array.isArray(res.data)) {
      apiPackages.value = res.data;
    }
  } catch (err) {
    console.warn('Menggunakan data katalog pratinjau:', err.message);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchPackagesFromBackend();
});
</script>

<template>
  <div class="jemari-landing">
    <!-- Toast Notifikasi Dummy Button -->
    <transition name="fade">
      <div v-if="dummyNotice" class="dummy-toast">
        {{ dummyNotice }}
      </div>
    </transition>



    <!-- Hero Section dengan Gradasi Emerald dari Referensi Baru -->
    <section class="hero-emerald">
      <div class="container hero-grid">
        <!-- Kolom Kiri: Identitas & Value Proposition -->
        <div class="hero-copy">
          <div class="hero-pill">
            <span>🤝</span> Platform UMKM #1 Indonesia
          </div>
          <h1>Jembatan Ekonomi Mandiri Rakyat Indonesia</h1>
          <p class="hero-desc">
            Pertemukan UMKM dengan freelancer kreatif. Harga jelas, pembayaran aman, revisi tertulis.
          </p>

          <!-- <div class="hero-buttons">
            <button class="btn-amber btn-lg" @click="handleDummyClick('Saya UMKM')">
              Saya UMKM
            </button>
            <button class="btn-glass-white btn-lg" @click="handleDummyClick('Saya Freelancer')">
              Saya Freelancer
            </button>
          </div> -->

          <div class="hero-badges-row">
            <span class="trust-chip">🛡️ Escrow 50:50</span>
            <span class="trust-chip">⭐ Fair Visibility</span>
            <span class="trust-chip">✓ Tanpa biaya tersembunyi</span>
          </div>
        </div>

        <!-- Kolom Kanan: Preview Dashboard Mockup Card dari Referensi Awal -->
        <div class="hero-mockup-wrapper">
          <div class="mockup-window">
            <div class="window-dots">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>

            <div class="umkm-profile-header">
            <div class="umkm-profile-left">
            <div class="umkm-avatar">🥙</div>
            <div>
              <div class="umkm-name">Kebab Nutrisix</div>
              <div class="umkm-score">Skor Kesiapan 67%</div>
            </div>
            </div>
            <div class="preview">Pratinjau</div>
              </div>


              <div class="umkm-stats-row">
              <div class="stat-box">
                <div class="stat-label">Pesanan Aktif</div>
                <div class="stat-value">3</div>
              </div>
              <div class="stat-box">
                <div class="stat-label">Transaksi</div>
                <div class="stat-value">Rp 750rb</div>
              </div>
              <div class="stat-box">
                <div class="stat-label">Ulasan</div>
                <div class="stat-value">4.8 <span class="star">★</span></div>
              </div>
            </div>

            <div class="order-list">
              <div class="order-item">
                <span>Desain Logo Brand Kit</span>
                <span class="status-pill blue">Diproses</span>
              </div>
              <div class="order-item">
                <span>Video Produk Kuliner</span>
                <span class="status-pill yellow">Revisi</span>
              </div>
              <div class="order-item">
                <span>Foto Menu Terbaru</span>
                <span class="status-pill green">Selesai</span>
              </div>
            </div>

            <div class="progress-bar-track">
              <div class="progress-bar-fill"></div>
            </div>
          </div>

          <!-- Floating Escrow Badge -->
          <div class="floating-escrow-badge">
            <div class="escrow-icon">🔒</div>
            <div>
              <div class="escrow-title">Dana Aman</div>
              <div class="escrow-sub">Rp 375.000 dalam escrow</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Floating Card: Cara Kerja JEMARI (Overlapping Hero Bottom) -->
    <section id="cara-kerja" class="how-it-works-overlap">
      <div class="container">
        <div class="how-card-container">
          <div class="how-header">
            <h2>Cara Kerja JEMARI</h2>
            <p>Tiga langkah transparan untuk kolaborasi UMKM dan Freelancer</p>
          </div>

          <div class="steps-grid">
            <div class="step-item">
              <div class="step-icon-box">🔍</div>
              <div>
                <h3>1. Pilih Jasa</h3>
                <p>Jelajahi katalog paket jasa dengan harga transparan di awal.</p>
              </div>
            </div>

            <div class="step-item">
              <div class="step-icon-box">💳</div>
              <div>
                <h3>2. Bayar DP 50%</h3>
                <p>Dana aman di rekening bersama JEMARI, freelancer mulai bekerja.</p>
              </div>
            </div>

            <div class="step-item">
              <div class="step-icon-box">🛡️</div>
              <div>
                <h3>3. Setujui &amp; Lunasi</h3>
                <p>Periksa hasil, ajukan revisi, lalu bayar pelunasan 50%.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section Perpaduan: Kolase Visual 2x2 + Pemilih Peran "Anda adalah siapa?" -->
    <section id="peran" class="role-showcase-section">
      <div class="container role-grid">
        <!-- Kolase 2x2 Visual -->
        <div class="collage-grid">
          <div
            v-for="tile in showcaseTiles"
            :key="tile.title"
            class="collage-tile"
            :style="{ background: tile.bg }"
          >
            <span class="tile-tag" :style="{ color: tile.accent }">{{ tile.tag }}</span>
            <div class="tile-emoji">{{ tile.emoji }}</div>
            <div class="tile-title" :style="{ color: tile.accent }">{{ tile.title }}</div>
          </div>
        </div>

        <!-- Panel Pemilih Peran -->
        <div class="role-selector-box">
          <span class="section-kicker">Mulai Langkahmu</span>
          <h2>Anda adalah siapa?</h2>
          <p class="role-sub">
            Pilih peran yang sesuai dengan kebutuhanmu saat ini untuk merasakan alur kerja terstruktur di JEMARI.
          </p>

          <div class="role-cards-stack">
            <button
              class="role-action-card primary-emerald"
              @click="handleDummyClick('Saya UMKM / Usaha Rumahan')"
            >
              <div class="role-left">
                <span class="role-icon-circle light">🏪</span>
                <div class="role-text">
                  <div class="role-name">Saya UMKM / Usaha Rumahan</div>
                  <div class="role-hint">Cari jasa kreatif dengan harga pasti &amp; escrow aman</div>
                </div>
              </div>
              <span class="arrow">→</span>
            </button>

            <button
              class="role-action-card outline-card"
              @click="handleDummyClick('Saya Freelancer')"
            >
              <div class="role-left">
                <span class="role-icon-circle green">🎨</span>
                <div class="role-text">
                  <div class="role-name">Saya Freelancer</div>
                  <div class="role-hint">Tawarkan paket keahlianmu &amp; bangun portofolio terverifikasi</div>
                </div>
              </div>
              <span class="arrow">→</span>
            </button>
          </div>

          <p class="terms-note">Dengan mendaftar, Anda menyetujui ketentuan layanan JEMARI.</p>
        </div>
      </div>
    </section>

    <!-- Jelajahi Kategori & Jasa Terpopuler -->
    <section id="katalog" class="catalog-section">
      <div class="container">
        <div class="category-block">
          <h2>Jelajahi Kategori Jasa</h2>
          <div class="category-pills">
            <button
              v-for="cat in categories"
              :key="cat.name"
              :class="['cat-pill', { active: selectedCategory === cat.name }]"
              @click="selectedCategory = cat.name"
            >
              <span class="cat-icon">{{ cat.icon }}</span>
              <span>{{ cat.name }}</span>
            </button>
          </div>
        </div>

        <div class="popular-block">
          <div class="popular-header">
            <h2>Jasa Terpopuler</h2>
            <span v-if="apiPackages.length > 0" class="live-badge">
              ● {{ apiPackages.length }} Paket Aktif dari Database
            </span>
          </div>

          <div v-if="isLoading" class="loading-box">Memuat katalog jasa...</div>

          <div v-else-if="displayedPackages.length === 0" class="empty-box">
            Belum ada paket jasa pada kategori <strong>{{ selectedCategory }}</strong>.
          </div>

          <div v-else class="services-grid">
            <div v-for="item in displayedPackages" :key="item.id" class="service-card">
              <div class="card-image-area">
                <span class="card-cat-tag">{{ item.category }}</span>
                <span v-if="item.isFromDb" class="db-pill">Live</span>
                <div class="placeholder-art">🖼️</div>
              </div>

              <div class="card-body">
                <h3 class="service-title">{{ item.title }}</h3>
                <div class="service-price">Mulai dari Rp {{ formatRupiah(item.basePrice) }}</div>

                <div class="freelancer-meta">
                  <div class="mini-avatar">{{ item.freelancerName.charAt(0) }}</div>
                  <div class="meta-info">
                    <div class="freelancer-name">{{ item.freelancerName }}</div>
                    <div class="rating-row">
                      <span class="stars">★★★★★</span>
                      <span class="rating-num">{{ item.rating }}</span>
                      <span class="review-count">({{ item.reviews }} ulasan)</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="catalog-footer-action">
            <router-link to="/freelancer/service-packages" class="link-all-services">
              Kelola &amp; Lihat Semua Jasa →
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer Emerald -->
    <footer class="jemari-footer">
      <div class="container footer-grid">
        <div class="footer-brand">
          <div class="footer-logo">JEMARI</div>
          <p>Jembatan Ekonomi Mandiri Rakyat Indonesia</p>
        </div>

        <div class="footer-col">
          <h4>Platform</h4>
          <ul>
            <li><a href="#cara-kerja" @click.prevent="scrollToSection('cara-kerja')">Cara Kerja</a></li>
            <li><a href="#katalog" @click.prevent="scrollToSection('katalog')">Katalog</a></li>
            <li><a href="#tentang" @click.prevent="handleDummyClick('Tentang Kami')">Tentang Kami</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4>Pengguna</h4>
          <ul>
            <li><a href="#daftar-umkm" @click.prevent="handleDummyClick('Daftar UMKM')">Daftar UMKM</a></li>
            <li><a href="#daftar-freelancer" @click.prevent="handleDummyClick('Daftar Freelancer')">Daftar Freelancer</a></li>
            <li><a href="#bantuan" @click.prevent="handleDummyClick('Pusat Bantuan')">Pusat Bantuan</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4>Kontak</h4>
          <ul>
            <li>halo@jemari.id</li>
            <li>@jemari.id</li>
            <li>+62 812-3456-7890</li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        © 2026 JEMARI. Seluruh hak dilindungi.
      </div>
    </footer>
  </div>
</template>

<style scoped>
.jemari-landing {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: #111827;
  background-color: #ffffff;
  line-height: 1.5;
}

.container {
  max-width: 1140px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Toast Dummy */
.dummy-toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  background: #1f2937;
  color: #ffffff;
  padding: 12px 18px;
  border-radius: 10px;
  font-size: 0.875rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  max-width: 350px;
}

/* Navbar */
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 50;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  font-size: 1.25rem;
  color: #0d7a4e;
  text-decoration: none;
  letter-spacing: -0.02em;
}

.brand-badge {
  background: #e6f4ed;
  padding: 4px 7px;
  border-radius: 8px;
  font-size: 1rem;
}

.nav-links {
  display: flex;
  gap: 28px;
}

.nav-links a {
  text-decoration: none;
  color: #4b5563;
  font-size: 0.92rem;
  font-weight: 500;
  transition: color 0.15s;
}

.nav-links a:hover {
  color: #0d7a4e;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.link-login {
  text-decoration: none;
  color: #374151;
  font-size: 0.9rem;
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
  font-size: 0.9rem;
}

.btn-amber:hover {
  background-color: #d97706;
}

.btn-glass-white {
  background-color: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  border: 1.5px solid rgba(255, 255, 255, 0.55);
  padding: 9px 18px;
  font-size: 0.9rem;
  backdrop-filter: blur(4px);
}

.btn-glass-white:hover {
  background-color: rgba(255, 255, 255, 0.24);
}

.btn-lg {
  padding: 12px 24px;
  font-size: 0.95rem;
}

/* Hero Emerald Section */
.hero-emerald {
  background: linear-gradient(135deg, #065f46 0%, #0d7a4e 55%, #168a59 100%);
  color: #ffffff;
  padding: 64px 0 112px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 48px;
  align-items: center;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: rgba(255, 255, 255, 0.16);
  color: #ecfdf5;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 6px 13px;
  border-radius: 999px;
  margin-bottom: 18px;
}

.hero-copy h1 {
  font-size: 2.65rem;
  font-weight: 800;
  line-height: 1.18;
  color: #ffffff;
  margin: 0 0 16px;
  letter-spacing: -0.025em;
}

.hero-desc {
  color: #d1fae5;
  font-size: 1.05rem;
  margin: 0 0 28px;
  max-width: 490px;
}

.hero-buttons {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}

.hero-badges-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.82rem;
  color: #ecfdf5;
}

.trust-chip {
  background: rgba(255, 255, 255, 0.12);
  padding: 5px 11px;
  border-radius: 6px;
}

/* Mockup Card */
.hero-mockup-wrapper {
  position: relative;
  color: #111827;
}

.mockup-window {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.22);
}

.window-dots {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}
.dot.red { background: #ef4444; }
.dot.yellow { background: #f59e0b; }
.dot.green { background: #10b981; }

.umkm-profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.umkm-profile-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview {
  background-color: #ecfdf5;
  color: #0d7a4e;
  border: 1px solid #a7f3d0;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.umkm-avatar {
  width: 40px;
  height: 40px;
  background: #fef3c7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}


.umkm-name {
  font-weight: 700;
  font-size: 0.95rem;
}

.umkm-score {
  font-size: 0.78rem;
  color: #6b7280;
}

.umkm-stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.stat-box {
  background: #f9fafb;
  padding: 10px 12px;
  border-radius: 8px;
}

.stat-label {
  font-size: 0.72rem;
  color: #6b7280;
}

.stat-value {
  font-weight: 700;
  font-size: 0.95rem;
  margin-top: 2px;
}

.stat-value .star {
  color: #f59e0b;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f9fafb;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 0.84rem;
  font-weight: 500;
}

.status-pill {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 999px;
}

.status-pill.blue { background: #dbeafe; color: #1d4ed8; }
.status-pill.yellow { background: #fef3c7; color: #b45309; }
.status-pill.green { background: #d1fae5; color: #047857; }

.progress-bar-track {
  height: 6px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.progress-bar-fill {
  width: 67%;
  height: 100%;
  background: #0d7a4e;
}

.floating-escrow-badge {
  position: absolute;
  bottom: -18px;
  left: -20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
}

.escrow-icon {
  background: #fef3c7;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.escrow-title {
  font-weight: 700;
  font-size: 0.82rem;
}

.escrow-sub {
  font-size: 0.75rem;
  color: #6b7280;
}

/* Floating Card Cara Kerja (Overlapping) */
.how-it-works-overlap {
  margin-top: -54px;
  padding-bottom: 48px;
  position: relative;
  z-index: 10;
}

.how-card-container {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 28px 32px;
  box-shadow: 0 14px 34px rgba(17, 24, 39, 0.07);
}

.how-header {
  margin-bottom: 22px;
}

.how-header h2 {
  font-size: 1.3rem;
  font-weight: 800;
  margin: 0 0 4px;
}

.how-header p {
  font-size: 0.88rem;
  color: #6b7280;
  margin: 0;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.step-icon-box {
  width: 42px;
  height: 42px;
  min-width: 42px;
  border-radius: 12px;
  background: #ecfdf5;
  color: #0d7a4e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
}

.step-item h3 {
  font-size: 0.96rem;
  font-weight: 700;
  margin: 0 0 4px;
  color: #111827;
}

.step-item p {
  font-size: 0.83rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

/* Role & Collage Showcase Section */
.role-showcase-section {
  padding: 36px 0 64px;
  background: #f9fafb;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}

.role-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 44px;
  align-items: center;
}

.collage-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.collage-tile {
  border-radius: 16px;
  padding: 18px;
  height: 148px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
}

.tile-tag {
  font-size: 0.72rem;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.75);
  padding: 3px 8px;
  border-radius: 6px;
  align-self: flex-start;
}

.tile-emoji {
  font-size: 2.3rem;
  align-self: center;
}

.tile-title {
  font-size: 0.84rem;
  font-weight: 700;
}

.section-kicker {
  display: inline-block;
  font-size: 0.78rem;
  font-weight: 700;
  color: #0d7a4e;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 6px;
}

.role-selector-box h2 {
  font-size: 1.65rem;
  font-weight: 800;
  margin: 0 0 8px;
}

.role-sub {
  color: #6b7280;
  font-size: 0.92rem;
  margin: 0 0 22px;
}

.role-cards-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-action-card {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 18px;
  border-radius: 12px;
  text-align: left;
}

.role-action-card.primary-emerald {
  background: #0d7a4e;
  color: #ffffff;
  border: 1px solid #0d7a4e;
}

.role-action-card.primary-emerald:hover {
  background: #065f46;
}

.role-action-card.outline-card {
  background: #ffffff;
  color: #111827;
  border: 1px solid #d1d5db;
}

.role-action-card.outline-card:hover {
  border-color: #0d7a4e;
  background: #ecfdf5;
}

.role-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-icon-circle {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
}

.role-icon-circle.light {
  background: rgba(255, 255, 255, 0.18);
}

.role-icon-circle.green {
  background: #ecfdf5;
}

.role-name {
  font-size: 0.95rem;
  font-weight: 700;
}

.role-hint {
  font-size: 0.78rem;
  opacity: 0.85;
  margin-top: 2px;
}

.arrow {
  font-size: 1.1rem;
  font-weight: 700;
}

.terms-note {
  font-size: 0.76rem;
  color: #9ca3af;
  margin: 14px 0 0;
}

/* Katalog Section */
.catalog-section {
  padding: 56px 0 80px;
}

.category-block {
  margin-bottom: 44px;
}

.category-block h2,
.popular-header h2 {
  font-size: 1.45rem;
  font-weight: 800;
  margin: 0 0 18px;
}

.category-pills {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.cat-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 0.88rem;
  white-space: nowrap;
}

.cat-pill:hover,
.cat-pill.active {
  border-color: #0d7a4e;
  background: #ecfdf5;
  color: #0d7a4e;
}

.popular-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.live-badge {
  font-size: 0.8rem;
  font-weight: 600;
  color: #059669;
  background: #ecfdf5;
  padding: 4px 10px;
  border-radius: 999px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.service-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.15s, box-shadow 0.15s;
}

.service-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.06);
}

.card-image-area {
  background: #eceef1;
  height: 140px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-cat-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.92);
  color: #0d7a4e;
  font-size: 0.72rem;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
}

.db-pill {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #0d7a4e;
  color: #ffffff;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
}

.placeholder-art {
  font-size: 2rem;
  opacity: 0.45;
}

.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.service-title {
  font-size: 0.95rem;
  font-weight: 700;
  margin: 0 0 8px;
  color: #111827;
  line-height: 1.4;
}

.service-price {
  font-size: 0.85rem;
  color: #4b5563;
  margin-bottom: 16px;
}

.freelancer-meta {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.mini-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e6f4ed;
  color: #0d7a4e;
  font-weight: 700;
  font-size: 0.78rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.freelancer-name {
  font-size: 0.8rem;
  font-weight: 600;
  color: #1f2937;
}

.rating-row {
  font-size: 0.72rem;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 3px;
}

.stars {
  color: #f59e0b;
}

.catalog-footer-action {
  text-align: center;
  margin-top: 32px;
}

.link-all-services {
  color: #0d7a4e;
  font-weight: 600;
  font-size: 0.92rem;
  text-decoration: none;
}

.link-all-services:hover {
  text-decoration: underline;
}

/* Footer */
.jemari-footer {
  background-color: #065f46;
  color: #ffffff;
  padding: 56px 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 32px;
  padding-bottom: 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.footer-logo {
  font-size: 1.25rem;
  font-weight: 800;
  margin-bottom: 10px;
}

.footer-brand p {
  color: #d1fae5;
  font-size: 0.88rem;
  margin: 0;
}

.footer-col h4 {
  font-size: 0.92rem;
  font-weight: 700;
  margin: 0 0 14px;
}

.footer-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.85rem;
  color: #d1fae5;
}

.footer-col a {
  color: #d1fae5;
  text-decoration: none;
}

.footer-col a:hover {
  color: #ffffff;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  font-size: 0.8rem;
  color: #a7f3d0;
}

.loading-box, .empty-box {
  text-align: center;
  padding: 36px;
  background: #f9fafb;
  border-radius: 12px;
  color: #6b7280;
}

/* Responsive */
@media (max-width: 960px) {
  .hero-grid,
  .role-grid {
    grid-template-columns: 1fr;
  }
  .services-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .steps-grid {
    grid-template-columns: 1fr;
  }
  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .services-grid {
    grid-template-columns: 1fr;
  }
  .nav-links {
    display: none;
  }
}
</style>