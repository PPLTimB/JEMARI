<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import servicePackageService from '../../services/servicePackageService'

const STORAGE_KEY = 'jemari_paid_addons_v1'
const packages = ref([])
const addons = ref([])
const loadingPackages = ref(false)
const packageError = ref('')
const search = ref('')
const packageFilter = ref('all')
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const form = ref({ packageId: '', name: '', description: '', price: '' })

const formatRupiah = (value) => new Intl.NumberFormat('id-ID', { maximumFractionDigits: 0 }).format(Number(value || 0))

const filteredAddons = computed(() => addons.value.filter((item) => {
  const matchesSearch = `${item.name} ${item.description || ''} ${item.packageTitle}`.toLowerCase().includes(search.value.toLowerCase())
  const matchesPackage = packageFilter.value === 'all' || String(item.packageId) === String(packageFilter.value)
  return matchesSearch && matchesPackage
}))

const activeCount = computed(() => addons.value.filter((item) => item.active).length)
const totalPrice = computed(() => addons.value.reduce((sum, item) => sum + Number(item.price || 0), 0))

const persist = () => localStorage.setItem(STORAGE_KEY, JSON.stringify(addons.value))
watch(addons, persist, { deep: true })

const loadPackages = async () => {
  loadingPackages.value = true
  packageError.value = ''
  try {
    const response = await servicePackageService.getMyPackages()
    packages.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    packageError.value = error.response?.data?.message || 'Paket jasa gagal dimuat. Pastikan backend aktif dan akun Freelancer sudah login.'
  } finally {
    loadingPackages.value = false
  }
}

const loadAddons = () => {
  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    addons.value = Array.isArray(stored) ? stored : []
  } catch {
    addons.value = []
  }
}

const openCreate = () => {
  editingId.value = null
  form.value = { packageId: packages.value[0]?.id ? String(packages.value[0].id) : '', name: '', description: '', price: '' }
  formError.value = ''
  showForm.value = true
}

const openEdit = (item) => {
  editingId.value = item.id
  form.value = { packageId: String(item.packageId), name: item.name, description: item.description || '', price: String(item.price) }
  formError.value = ''
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  editingId.value = null
  formError.value = ''
}

const saveAddon = () => {
  formError.value = ''
  if (!form.value.packageId || !form.value.name.trim() || !form.value.price || Number(form.value.price) <= 0) {
    formError.value = 'Pilih paket, isi nama add-on, dan masukkan harga lebih dari 0.'
    return
  }
  const selectedPackage = packages.value.find((item) => String(item.id) === String(form.value.packageId))
  if (!selectedPackage) {
    formError.value = 'Paket jasa tidak ditemukan. Muat ulang daftar paket.'
    return
  }
  saving.value = true
  const payload = {
    id: editingId.value || `addon-${Date.now()}`,
    packageId: selectedPackage.id,
    packageTitle: selectedPackage.title,
    name: form.value.name.trim(),
    description: form.value.description.trim(),
    price: Number(form.value.price),
    active: editingId.value ? (addons.value.find((item) => item.id === editingId.value)?.active ?? true) : true,
    updatedAt: new Date().toISOString(),
  }
  if (editingId.value) {
    const index = addons.value.findIndex((item) => item.id === editingId.value)
    if (index !== -1) addons.value[index] = payload
  } else {
    addons.value.unshift({ ...payload, createdAt: payload.updatedAt })
  }
  saving.value = false
  closeForm()
}

const deleteAddon = (item) => {
  if (!window.confirm(`Hapus add-on "${item.name}"?`)) return
  addons.value = addons.value.filter((addon) => addon.id !== item.id)
}

const toggleActive = (item) => {
  item.active = !item.active
  item.updatedAt = new Date().toISOString()
}

onMounted(() => {
  loadAddons()
  loadPackages()
})
</script>

<template>
  <div class="paid-addon-page">
    <header class="topbar">
      <router-link to="/" class="brand">JEMARI<span>.</span></router-link>
      <nav>
        <router-link to="/">Beranda</router-link>
        <router-link to="/freelancer/service-packages">Paket Jasa</router-link>
        <span class="current">Paid Add-ons</span>
      </nav>
      <button class="primary-btn" @click="openCreate" :disabled="packages.length === 0">＋ Tambah Add-on</button>
    </header>

    <main class="content">
      <section class="hero">
        <div>
          <div class="eyebrow">WORKSPACE FREELANCER</div>
          <h1>Paid <span>Add-ons</span></h1>
          <p>Kelola layanan tambahan berbayar untuk setiap paket jasa yang kamu tawarkan.</p>
        </div>
        <div class="hero-mark">＋</div>
      </section>

      <div v-if="packageError" class="notice error">{{ packageError }} <button @click="loadPackages">Coba lagi</button></div>
      <div v-if="loadingPackages" class="notice">Memuat paket jasa...</div>
      <div v-else-if="packages.length === 0" class="notice">Belum ada paket jasa aktif. Buat paket jasa terlebih dahulu sebelum menambahkan add-on.</div>

      <section class="stats">
        <article><span>Total Add-ons</span><strong>{{ addons.length }}</strong><small>Seluruh add-on tersimpan</small></article>
        <article><span>Add-ons Aktif</span><strong>{{ activeCount }}</strong><small>Siap ditawarkan ke klien</small></article>
        <article><span>Total Nilai Harga</span><strong>Rp {{ formatRupiah(totalPrice) }}</strong><small>Akumulasi harga add-on</small></article>
      </section>

      <section class="toolbar">
        <div><h2>Daftar Add-ons</h2><p>Atur layanan ekstra pada paket jasa freelancer.</p></div>
        <div class="filters">
          <input v-model="search" type="search" placeholder="Cari add-on atau paket..." aria-label="Cari add-on" />
          <select v-model="packageFilter" aria-label="Filter paket">
            <option value="all">Semua paket</option>
            <option v-for="pkg in packages" :key="pkg.id" :value="String(pkg.id)">{{ pkg.title }}</option>
          </select>
        </div>
      </section>

      <section v-if="filteredAddons.length" class="addon-grid">
        <article v-for="item in filteredAddons" :key="item.id" class="addon-card">
          <div class="card-top">
            <span class="addon-icon">✦</span>
            <button class="status" :class="{ inactive: !item.active }" @click="toggleActive(item)">{{ item.active ? '● Aktif' : '○ Nonaktif' }}</button>
          </div>
          <div class="package-label">{{ item.packageTitle }}</div>
          <h3>{{ item.name }}</h3>
          <p class="description">{{ item.description || 'Tidak ada deskripsi tambahan.' }}</p>
          <div class="price">Rp {{ formatRupiah(item.price) }}</div>
          <div class="card-actions">
            <button class="edit-btn" @click="openEdit(item)">Edit</button>
            <button class="delete-btn" @click="deleteAddon(item)">Hapus</button>
          </div>
        </article>
      </section>

      <section v-else class="empty-state">
        <div class="empty-icon">✦</div>
        <h3>{{ addons.length ? 'Add-on tidak ditemukan' : 'Belum ada add-on berbayar' }}</h3>
        <p>{{ addons.length ? 'Coba ubah kata kunci atau filter paket.' : 'Tambahkan layanan ekstra seperti revisi tambahan, pengerjaan express, atau file source.' }}</p>
        <button v-if="!addons.length && packages.length" class="primary-btn" @click="openCreate">＋ Buat Add-on Pertama</button>
      </section>
    </main>

    <div v-if="showForm" class="modal-backdrop" @click.self="closeForm">
      <section class="modal" role="dialog" aria-modal="true" aria-labelledby="form-title">
        <div class="modal-head"><div><div class="eyebrow">PENGELOLAAN ADD-ON</div><h2 id="form-title">{{ editingId ? 'Edit Add-on' : 'Tambah Add-on Baru' }}</h2></div><button class="close-btn" @click="closeForm" aria-label="Tutup">×</button></div>
        <form @submit.prevent="saveAddon">
          <label>Paket Jasa <span>*</span>
            <select v-model="form.packageId" required>
              <option value="" disabled>Pilih paket jasa</option>
              <option v-for="pkg in packages" :key="pkg.id" :value="String(pkg.id)">{{ pkg.title }}</option>
            </select>
          </label>
          <label>Nama Add-on <span>*</span><input v-model="form.name" maxlength="120" required placeholder="Contoh: Revisi Tambahan" /></label>
          <label>Deskripsi<textarea v-model="form.description" rows="3" maxlength="500" placeholder="Jelaskan layanan tambahan yang akan diterima klien..."></textarea></label>
          <label>Harga Add-on (Rp) <span>*</span><input v-model="form.price" type="number" min="1" step="1000" required placeholder="50000" /></label>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions"><button type="button" class="cancel-btn" @click="closeForm">Batal</button><button type="submit" class="primary-btn" :disabled="saving">{{ editingId ? 'Simpan Perubahan' : 'Simpan Add-on' }}</button></div>
        </form>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@500;600;700;800&display=swap');
*{box-sizing:border-box}.paid-addon-page{min-height:100vh;background:#f7f8fa;color:#182230;font-family:'DM Sans',sans-serif}.topbar{height:76px;background:#fff;border-bottom:1px solid #e9edf2;display:flex;align-items:center;justify-content:space-between;padding:0 max(5vw,24px);gap:24px}.brand{font:800 24px 'Plus Jakarta Sans',sans-serif;color:#182230;text-decoration:none;letter-spacing:-1px}.brand span,.hero h1 span{color:#d99a36}.topbar nav{display:flex;align-items:center;gap:28px;margin-right:auto;margin-left:52px}.topbar nav a,.topbar nav span{font-size:13px;color:#778292;text-decoration:none;font-weight:600}.topbar nav .current{color:#bd8124}.primary-btn{border:0;border-radius:9px;background:#d99a36;color:#fff;padding:12px 18px;font-weight:700;cursor:pointer;transition:.2s}.primary-btn:hover{background:#c48627;transform:translateY(-1px)}.primary-btn:disabled{opacity:.5;cursor:not-allowed;transform:none}.content{max-width:1180px;margin:0 auto;padding:42px 24px 80px}.hero{background:#202c3c;border-radius:18px;padding:34px 40px;color:#fff;display:flex;align-items:center;justify-content:space-between;overflow:hidden;position:relative}.eyebrow{font-size:10px;letter-spacing:2px;font-weight:800;color:#dca344;margin-bottom:12px}.hero h1{font:800 36px 'Plus Jakarta Sans',sans-serif;margin:0 0 10px;letter-spacing:-1.5px}.hero p{color:#c3cbd5;margin:0;max-width:580px;font-size:14px}.hero-mark{width:112px;height:112px;border:1px solid #657080;border-radius:28px;display:grid;place-items:center;font-size:64px;color:#d99a36;transform:rotate(12deg);margin-right:18px}.notice{background:#fff7e8;border:1px solid #f1d8a9;color:#77531e;padding:13px 16px;border-radius:10px;margin-top:20px;font-size:13px}.notice.error{background:#fff0ef;border-color:#f3c5c1;color:#a33d35}.notice button{margin-left:10px;border:0;background:transparent;color:inherit;text-decoration:underline;cursor:pointer}.stats{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin:24px 0 38px}.stats article{background:#fff;border:1px solid #e9edf2;border-radius:13px;padding:20px 22px;display:flex;flex-direction:column;gap:5px}.stats span{font-size:12px;color:#758092;font-weight:600}.stats strong{font:800 25px 'Plus Jakarta Sans',sans-serif;color:#243247}.stats small{font-size:11px;color:#9aa3af}.toolbar{display:flex;align-items:end;justify-content:space-between;gap:20px;margin-bottom:18px}.toolbar h2{font:800 21px 'Plus Jakarta Sans',sans-serif;margin:0 0 5px}.toolbar p{font-size:12px;color:#8993a1;margin:0}.filters{display:flex;gap:9px}.filters input,.filters select{height:40px;border:1px solid #dfe4ea;border-radius:8px;padding:0 12px;background:#fff;color:#344054;font:13px 'DM Sans',sans-serif;min-width:190px;outline:none}.filters input:focus,.filters select:focus,.modal input:focus,.modal select:focus,.modal textarea:focus{border-color:#d99a36;box-shadow:0 0 0 3px #d99a3620}.addon-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.addon-card{background:#fff;border:1px solid #e8edf2;border-radius:13px;padding:20px;min-height:245px;display:flex;flex-direction:column;box-shadow:0 3px 12px #1d29390a}.card-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:17px}.addon-icon{width:35px;height:35px;border-radius:10px;background:#fff5e5;color:#c78a2d;display:grid;place-items:center;font-size:18px}.status{border:0;background:#eaf7ef;color:#26834d;padding:6px 9px;border-radius:20px;font-size:10px;font-weight:700;cursor:pointer}.status.inactive{background:#f0f2f5;color:#7d8794}.package-label{font-size:10px;color:#b47a22;font-weight:700;text-transform:uppercase;letter-spacing:.7px;margin-bottom:6px}.addon-card h3{font:700 16px 'Plus Jakarta Sans',sans-serif;margin:0 0 7px}.description{font-size:12px;line-height:1.6;color:#7d8794;margin:0 0 14px;min-height:38px}.price{font:800 18px 'Plus Jakarta Sans',sans-serif;color:#26364a;margin-top:auto;padding-top:5px}.card-actions{display:flex;gap:8px;border-top:1px solid #eef0f3;margin-top:16px;padding-top:13px}.card-actions button{border:0;background:transparent;font-size:12px;font-weight:700;cursor:pointer;padding:4px 0}.edit-btn{color:#a66d17}.delete-btn{color:#c45b52;margin-left:auto}.empty-state{background:#fff;border:1px dashed #d8dee6;border-radius:14px;text-align:center;padding:48px 20px}.empty-icon{margin:0 auto 12px;width:48px;height:48px;border-radius:14px;background:#fff5e5;color:#c78a2d;display:grid;place-items:center;font-size:23px}.empty-state h3{font:700 17px 'Plus Jakarta Sans',sans-serif;margin:0 0 8px}.empty-state p{font-size:12px;color:#8993a1;margin:0 auto 18px;max-width:420px;line-height:1.6}.modal-backdrop{position:fixed;inset:0;background:#10182888;display:grid;place-items:center;padding:20px;z-index:20}.modal{width:min(100%,520px);background:#fff;border-radius:16px;padding:27px;box-shadow:0 20px 60px #10182835}.modal-head{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:22px}.modal-head h2{font:800 21px 'Plus Jakarta Sans',sans-serif;margin:0}.close-btn{border:0;background:#f1f3f5;width:32px;height:32px;border-radius:8px;font-size:22px;color:#667085;cursor:pointer}.modal form{display:flex;flex-direction:column;gap:15px}.modal label{display:flex;flex-direction:column;gap:7px;font-size:12px;font-weight:700;color:#344054}.modal label span{color:#c78323;display:inline}.modal input,.modal select,.modal textarea{width:100%;border:1px solid #dfe4ea;border-radius:8px;padding:11px 12px;font:13px 'DM Sans',sans-serif;color:#344054;outline:none;background:#fff}.modal textarea{resize:vertical}.form-error{font-size:12px;color:#bd443d;margin:0}.modal-actions{display:flex;justify-content:flex-end;gap:9px;margin-top:6px}.cancel-btn{border:1px solid #dfe4ea;background:#fff;color:#596575;border-radius:8px;padding:11px 17px;font-weight:700;cursor:pointer}@media(max-width:800px){.topbar{padding:0 18px}.topbar nav{margin-left:12px;gap:12px}.topbar nav a{display:none}.topbar>.primary-btn{padding:10px 12px;font-size:11px}.content{padding:25px 16px 55px}.hero{padding:27px 23px}.hero-mark{display:none}.stats{gap:9px}.stats article{padding:15px 12px}.stats strong{font-size:19px}.addon-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.toolbar{align-items:flex-start;flex-direction:column}.filters{width:100%}.filters input,.filters select{min-width:0;width:50%}}@media(max-width:520px){.brand{font-size:20px}.topbar nav{display:none}.topbar>.primary-btn{margin-left:auto}.hero h1{font-size:29px}.hero p{font-size:12px}.stats{grid-template-columns:1fr}.addon-grid{grid-template-columns:1fr}.filters{flex-direction:column}.filters input,.filters select{width:100%}}
</style>
