
<script setup>
import { ref } from 'vue'

const kuotaRevisi = ref(3)
const batasHari = ref(7)
const pesan = ref('')

const daftarKetentuan = ref([
  {
    id: 1,
    namaPaket: 'Paket Basic',
    kuota: 3,
    batasHari: 7,
  },
  {
    id: 2,
    namaPaket: 'Paket Premium',
    kuota: 5,
    batasHari: 14,
  },
])

const namaPaket = ref('')
const sedangEdit = ref(false)
const idEdit = ref(null)

function simpanKetentuan() {
  pesan.value = ''

  if (!namaPaket.value.trim()) {
    pesan.value = 'Nama paket wajib diisi.'
    return
  }

  if (
    kuotaRevisi.value === '' ||
    !Number.isInteger(kuotaRevisi.value) ||
    kuotaRevisi.value < 0
  ) {
    pesan.value = 'Kuota revisi harus berupa angka minimal 0.'
    return
  }

  if (
    batasHari.value === '' ||
    !Number.isInteger(batasHari.value) ||
    batasHari.value < 1
  ) {
    pesan.value = 'Batas waktu revisi harus minimal 1 hari.'
    return
  }

  if (sedangEdit.value) {
    const index = daftarKetentuan.value.findIndex(
      (item) => item.id === idEdit.value
    )

    if (index !== -1) {
      daftarKetentuan.value[index] = {
        id: idEdit.value,
        namaPaket: namaPaket.value,
        kuota: kuotaRevisi.value,
        batasHari: batasHari.value,
      }
    }

    pesan.value = 'Ketentuan berhasil diperbarui!'
  } else {
    const ketentuanBaru = {
      id: Date.now(),
      namaPaket: namaPaket.value,
      kuota: kuotaRevisi.value,
      batasHari: batasHari.value,
    }

    daftarKetentuan.value.push(ketentuanBaru)
    pesan.value = 'Ketentuan berhasil ditambahkan!'
  }

  resetForm()
}

function editKetentuan(item) {
  namaPaket.value = item.namaPaket
  kuotaRevisi.value = item.kuota
  batasHari.value = item.batasHari

  idEdit.value = item.id
  sedangEdit.value = true
  pesan.value = ''
}

function hapusKetentuan(id) {
  daftarKetentuan.value = daftarKetentuan.value.filter(
    (item) => item.id !== id
  )

  pesan.value = 'Ketentuan berhasil dihapus!'
}

function resetForm() {
  namaPaket.value = ''
  kuotaRevisi.value = 3
  batasHari.value = 7

  sedangEdit.value = false
  idEdit.value = null
}
</script>


<template>
  <main class="container">
    <header>
      <p class="subtitle">JEMARI • FREELANCER</p>
      <h1>Ketentuan Revisi</h1>
      <p class="description">
        Atur jumlah revisi dan batas waktu revisi untuk paket freelancer.
      </p>
    </header>

    <section class="card">
      <h2>Pengaturan Revisi</h2>
      

<div class="form-group">
  <label for="namaPaket">Nama Paket</label>
  <input
    id="namaPaket"
    v-model="namaPaket"
    type="text"
    placeholder="Contoh: Paket Basic"
  />
  <small>Nama paket freelancer yang akan diatur.</small>
</div>


      <div class="form-group">
        <label for="kuota">Jumlah Kuota Revisi</label>
        <input
          id="kuota"
          v-model.number="kuotaRevisi"
          type="number"
          min="0"
        />
        <small>Jumlah revisi yang diperbolehkan.</small>
      </div>

      <div class="form-group">
        <label for="batas">Batas Waktu Revisi (hari)</label>
        <input
          id="batas"
          v-model.number="batasHari"
          type="number"
          min="1"
        />
        <small>Batas waktu untuk mengajukan revisi.</small>
      </div>

      
<div class="button-group">
  <button @click="simpanKetentuan">
    {{ sedangEdit ? 'Perbarui Ketentuan' : 'Simpan Ketentuan' }}
  </button>

  <button
    v-if="sedangEdit"
    type="button"
    class="cancel-button"
    @click="resetForm"
  >
    Batal Edit
  </button>
</div>


      <p v-if="pesan" class="success">
        {{ pesan }}
      </p>
      <div class="list-section">
  <h2>Daftar Ketentuan Revisi</h2>

  <table>
    <thead>
      <tr>
        <th>Nama Paket</th>
        <th>Kuota Revisi</th>
        <th>Batas Hari</th>
        <th>Aksi</th>
      </tr>
    </thead>

    <tbody>
      <tr
        v-for="item in daftarKetentuan"
        :key="item.id"
      >
        <td>{{ item.namaPaket }}</td>
        <td>{{ item.kuota }} kali</td>
        <td>{{ item.batasHari }} hari</td>
        <td>
          <button
            type="button"
            @click="editKetentuan(item)"
          >
            Edit
          </button>

          <button
            type="button"
            class="delete-button"
            @click="hapusKetentuan(item.id)"
          >
            Hapus
          </button>
        </td>
      </tr>

      <tr v-if="daftarKetentuan.length === 0">
        <td colspan="4">
          Belum ada ketentuan revisi.
        </td>
      </tr>
    </tbody>
  </table>
</div>
    </section>
  </main>
</template>

<style scoped>
.container {
  max-width: 720px;
  margin: 60px auto;
  padding: 24px;
  font-family: Arial, sans-serif;
  color: #1f2937;
}

.subtitle {
  color: #7c3aed;
  font-weight: bold;
  font-size: 13px;
  letter-spacing: 1px;
}

h1 {
  margin-bottom: 8px;
}

.description {
  color: #6b7280;
  margin-bottom: 32px;
}

.card {
  padding: 28px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
}

h2 {
  margin-top: 0;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

label {
  font-weight: bold;
}

input {
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
}

small {
  color: #6b7280;
}

button {
  padding: 12px 20px;
  background: #7c3aed;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

button:hover {
  background: #6d28d9;
}

.success {
  color: #15803d;
  margin-top: 20px;
}


.button-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cancel-button {
  background: #6b7280;
}

.list-section {
  margin-top: 36px;
}

.list-section h2 {
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}

th,
td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

th {
  background: #f9fafb;
}

.delete-button {
  background: #dc2626;
  margin-left: 8px;
}

.delete-button:hover {
  background: #b91c1c;
}

</style>
