<script setup>
import { ref, computed, watch } from 'vue'
import { formatRupiah, formatDurasi } from '../../utils/rupiahPaket'

const props = defineProps({
  varian: {
    type: Array,
    default: () => [],
  },
  hargaMulai: {
    type: [Number, String],
    default: null,
  },
})

const selectedVariantId = ref(null)

// Otomatis pilih varian termurah atau varian pertama saat data masuk
watch(
  () => props.varian,
  (newVal) => {
    if (newVal && newVal.length > 0 && !selectedVariantId.value) {
      selectedVariantId.value = newVal[0].id
    }
  },
  { immediate: true },
)

const selectedVariant = computed(() => {
  return props.varian.find((v) => v.id === selectedVariantId.value) || props.varian[0] || null
})

function isCheapest(variant) {
  if (!props.hargaMulai || !variant) return false
  return Number(variant.price) === Number(props.hargaMulai)
}
</script>

<template>
  <div class="rincian-biaya-container">
    <div class="header-rincian">
      <div class="judul-wrapper">
        <h3 class="judul">Pilihan Paket & Rincian Biaya</h3>
        <span class="badge-transparan" title="Biaya transparan tanpa biaya tambahan tersembunyi">
          ✓ Transparan (Tanpa Biaya Tersembunyi)
        </span>
      </div>
      <p class="keterangan">
        Pilih varian pengerjaan yang sesuai dengan kebutuhan skala usaha Anda.
      </p>
    </div>

    <!-- Jika belum ada varian -->
    <div v-if="!varian || varian.length === 0" class="empty-state">
      <p>Belum ada rincian biaya atau varian yang aktif pada paket ini.</p>
    </div>

    <!-- Daftar Pilihan Varian -->
    <div v-else class="varian-grid" role="radiogroup" aria-label="Pilihan varian harga paket">
      <div
        v-for="item in varian"
        :key="item.id"
        class="varian-card"
        :class="{
          selected: selectedVariantId === item.id,
          'is-cheapest': isCheapest(item),
        }"
        tabindex="0"
        role="radio"
        :aria-checked="selectedVariantId === item.id"
        @click="selectedVariantId = item.id"
        @keydown.enter="selectedVariantId = item.id"
        @keydown.space.prevent="selectedVariantId = item.id"
      >
        <div class="varian-header">
          <div class="nama-wrapper">
            <span class="radio-indicator" :class="{ active: selectedVariantId === item.id }"></span>
            <span class="nama-varian">{{ item.name }}</span>
          </div>
          <span v-if="isCheapest(item)" class="tag-harga-mulai"> Harga Mulai </span>
        </div>

        <div class="varian-body">
          <p class="deskripsi-varian">
            {{ item.description || 'Tidak ada keterangan tambahan pada varian ini.' }}
          </p>
        </div>

        <div class="varian-footer">
          <div class="durasi-info">
            <span class="durasi-icon" aria-hidden="true">⏱️</span>
            <span>{{ formatDurasi(item.estimatedDays) }}</span>
          </div>
          <div class="harga-info">
            <span class="nominal-harga">{{ formatRupiah(item.price) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Panel Rincian Varian Terpilih -->
    <div v-if="selectedVariant" class="ringkasan-pilihan">
      <div class="ringkasan-header">
        <span class="ringkasan-label">Varian Terpilih:</span>
        <strong class="ringkasan-nama">{{ selectedVariant.name }}</strong>
      </div>
      <div class="ringkasan-detail">
        <div class="baris-detail">
          <span>Estimasi Pengerjaan:</span>
          <strong>{{ formatDurasi(selectedVariant.estimatedDays) }}</strong>
        </div>
        <div class="baris-detail">
          <span>Total Biaya Paket:</span>
          <strong class="harga-total">{{ formatRupiah(selectedVariant.price) }}</strong>
        </div>
      </div>
      <p class="catatan-garansi">
        💡 Harga di atas adalah harga pasti untuk varian yang dipilih. Tidak ada biaya tersembunyi.
      </p>
    </div>
  </div>
</template>

<style scoped>
.rincian-biaya-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.header-rincian {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.judul-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.judul {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.badge-transparan {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #047857;
  background-color: #d1fae5;
  padding: 4px 10px;
  border-radius: 9999px;
  border: 1px solid #a7f3d0;
}

.keterangan {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.empty-state {
  padding: 24px;
  background-color: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.varian-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.varian-card {
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  display: flex;
  flex-direction: column;
  gap: 10px;
  outline: none;
  min-height: 44px;
}

.varian-card:hover {
  border-color: #c4b5fd;
  background-color: #faf5ff;
}

.varian-card:focus-visible {
  border-color: #7c3aed;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.2);
}

.varian-card.selected {
  border-color: #7c3aed;
  background-color: #f5f3ff;
}

.varian-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.nama-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.radio-indicator {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.radio-indicator.active {
  border-color: #7c3aed;
}

.radio-indicator.active::after {
  content: '';
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #7c3aed;
}

.nama-varian {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.tag-harga-mulai {
  font-size: 11px;
  font-weight: 700;
  color: #7c3aed;
  background-color: #ede9fe;
  padding: 2px 8px;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.deskripsi-varian {
  font-size: 13px;
  color: #4b5563;
  margin: 0;
  line-height: 1.4;
}

.varian-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
}

.durasi-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.nominal-harga {
  font-size: 16px;
  font-weight: 700;
  color: #7c3aed;
}

.ringkasan-pilihan {
  margin-top: 4px;
  padding: 16px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ringkasan-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.ringkasan-label {
  color: #64748b;
}

.ringkasan-nama {
  color: #0f172a;
}

.ringkasan-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.baris-detail {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #334155;
}

.harga-total {
  color: #7c3aed;
  font-size: 16px;
}

.catatan-garansi {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}
</style>
