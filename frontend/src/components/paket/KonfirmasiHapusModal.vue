<script setup>
defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  varianItem: {
    type: Object,
    default: null,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <div v-if="isOpen" class="modal-backdrop" @click.self="emit('close')">
    <div
      class="modal-dialog"
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="judul-hapus"
      aria-describedby="desc-hapus"
    >
      <div class="modal-content">
        <div class="icon-danger-wrapper" aria-hidden="true">⚠️</div>
        <h2 id="judul-hapus" class="modal-title">Hapus Varian Paket?</h2>
        <p id="desc-hapus" class="modal-desc">
          Apakah Anda yakin ingin menonaktifkan varian
          <strong>"{{ varianItem?.name }}"</strong>? Varian ini tidak akan lagi tampil di halaman
          penawaran paket untuk pesanan baru.
        </p>

        <div class="modal-actions">
          <button type="button" class="btn-cancel" :disabled="isLoading" @click="emit('close')">
            Batal
          </button>
          <button
            type="button"
            class="btn-confirm-delete"
            :disabled="isLoading"
            @click="emit('confirm')"
          >
            {{ isLoading ? 'Menghapus...' : 'Ya, Hapus Varian' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(17, 24, 39, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1000;
}

.modal-dialog {
  background: #ffffff;
  border-radius: 12px;
  width: 100%;
  max-width: 440px;
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-content {
  padding: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.icon-danger-wrapper {
  font-size: 36px;
  margin-bottom: 12px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
}

.modal-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.modal-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn-cancel {
  flex: 1;
  padding: 12px;
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-cancel:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.btn-confirm-delete {
  flex: 1;
  padding: 12px;
  background-color: #dc2626;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  min-height: 44px;
}

.btn-confirm-delete:hover:not(:disabled) {
  background-color: #b91c1c;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
