<script setup>
import { formatRupiah, formatDurasi } from '../../utils/rupiahPaket'

defineProps({
  varianList: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['edit', 'hapus'])
</script>

<template>
  <div class="table-wrapper">
    <table class="varian-table" aria-label="Tabel daftar varian paket">
      <thead>
        <tr>
          <th scope="col">Nama Varian</th>
          <th scope="col">Rincian Termasuk</th>
          <th scope="col">Estimasi Durasi</th>
          <th scope="col">Harga</th>
          <th scope="col" class="kolom-aksi">Aksi</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="varianList.length === 0">
          <td colspan="5" class="empty-cell">
            <div class="empty-content">
              <span class="empty-icon" aria-hidden="true">📦</span>
              <p>Belum ada varian aktif pada paket ini.</p>
              <small
                >Klik tombol <strong>+ Tambah Varian</strong> di atas untuk menambahkan varian harga
                baru.</small
              >
            </div>
          </td>
        </tr>
        <tr v-for="item in varianList" :key="item.id">
          <td class="nama-cell">
            <strong>{{ item.name }}</strong>
          </td>
          <td class="deskripsi-cell">
            {{ item.description || '—' }}
          </td>
          <td>
            <span class="badge-durasi">{{ formatDurasi(item.estimatedDays) }}</span>
          </td>
          <td class="harga-cell">
            {{ formatRupiah(item.price) }}
          </td>
          <td class="aksi-cell">
            <button
              type="button"
              class="btn-action btn-edit"
              :aria-label="`Ubah varian ${item.name}`"
              @click="emit('edit', item)"
            >
              Ubah
            </button>
            <button
              type="button"
              class="btn-action btn-delete"
              :aria-label="`Hapus varian ${item.name}`"
              @click="emit('hapus', item)"
            >
              Hapus
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrapper {
  width: 100%;
  overflow-x: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.varian-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 14px;
}

th,
td {
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
  vertical-align: middle;
}

th {
  background-color: #f9fafb;
  font-weight: 700;
  color: #374151;
  white-space: nowrap;
}

.kolom-aksi {
  text-align: right;
}

.nama-cell {
  color: #111827;
  font-weight: 600;
  white-space: nowrap;
}

.deskripsi-cell {
  color: #4b5563;
  max-width: 260px;
  line-height: 1.4;
}

.badge-durasi {
  display: inline-block;
  background-color: #f3f4f6;
  color: #374151;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.harga-cell {
  color: #7c3aed;
  font-weight: 700;
  white-space: nowrap;
}

.aksi-cell {
  text-align: right;
  white-space: nowrap;
}

.btn-action {
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  min-height: 40px;
  transition: background-color 0.15s ease;
}

.btn-edit {
  background-color: #f3f4f6;
  color: #374151;
  margin-right: 8px;
  border: 1px solid #d1d5db;
}

.btn-edit:hover {
  background-color: #e5e7eb;
}

.btn-delete {
  background-color: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-delete:hover {
  background-color: #fca5a5;
  color: #991b1b;
}

.empty-cell {
  text-align: center;
  padding: 48px 16px;
  background-color: #fdfdfd;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #6b7280;
}

.empty-icon {
  font-size: 32px;
}

.empty-content p {
  margin: 0;
  font-weight: 600;
  font-size: 15px;
  color: #374151;
}

.empty-content small {
  font-size: 13px;
}
</style>
