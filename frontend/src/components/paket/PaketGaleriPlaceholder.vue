<script setup>
// Komponen Galeri Hasil Kerja Placeholder sesuai mockup proposal 4.5.12
// Menampilkan area besar bergaris diagonal/silang dan tiga thumbnail di bawahnya.
</script>

<template>
  <div class="galeri-container" aria-label="Galeri hasil kerja freelancer">
    <!-- Gambar Utama Placeholder -->
    <div class="galeri-utama">
      <div class="diagonal-pattern">
        <svg viewBox="0 0 100 100" preserveAspectRatio="none" class="cross-lines">
          <line x1="0" y1="0" x2="100" y2="100" stroke="#d1d5db" stroke-width="1.5" />
          <line x1="100" y1="0" x2="0" y2="100" stroke="#d1d5db" stroke-width="1.5" />
        </svg>
      </div>
      <div class="label-placeholder">
        <span class="icon-camera" aria-hidden="true">📷</span>
        <p class="teks-utama">Galeri Hasil Kerja</p>
        <small class="teks-keterangan">(Data galeri milik modul PBI-02 • Belum tersedia)</small>
      </div>
    </div>

    <!-- Tiga Thumbnail Kecil di Bawahnya -->
    <div class="galeri-thumbnails">
      <div v-for="i in 3" :key="i" class="thumbnail-item" :aria-label="`Thumbnail ${i}`">
        <svg viewBox="0 0 100 100" preserveAspectRatio="none" class="cross-lines-mini">
          <line x1="0" y1="0" x2="100" y2="100" stroke="#e5e7eb" stroke-width="2" />
          <line x1="100" y1="0" x2="0" y2="100" stroke="#e5e7eb" stroke-width="2" />
        </svg>
      </div>
    </div>
  </div>
</template>

<style scoped>
.galeri-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.galeri-utama {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.diagonal-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.cross-lines {
  width: 100%;
  height: 100%;
}

.label-placeholder {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(2px);
  border-radius: 8px;
  border: 1px dashed #d1d5db;
  max-width: 80%;
}

.icon-camera {
  font-size: 28px;
  display: block;
  margin-bottom: 6px;
}

.teks-utama {
  font-size: 16px;
  font-weight: 700;
  color: #374151;
  margin: 0 0 4px 0;
}

.teks-keterangan {
  font-size: 12px;
  color: #6b7280;
  display: block;
}

.galeri-thumbnails {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.thumbnail-item {
  aspect-ratio: 1 / 1;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.cross-lines-mini {
  width: 100%;
  height: 100%;
}
</style>
