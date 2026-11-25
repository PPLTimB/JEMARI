<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { freelancerProfileService } from '../services/freelancerProfileService'
import { freelancerSkillService } from '../services/freelancerSkillService'
import { freelancerExperienceService } from '../services/freelancerExperienceService'

const activeMenu = ref('profil') // 'beranda' | 'request' | 'pekerjaan' | 'paket' | 'profil' | 'keahlian' | 'saldo' | 'ulasan' | 'pengaturan'
const searchQuery = ref('')

const tokenInput = ref(localStorage.getItem('jemari_token') || '')
const showTokenBar = ref(true)
const isLoading = ref(false)
const isSaving = ref(false)
const isDeactivating = ref(false)
const profileExists = ref(false)
const alertMessage = ref('')
const alertType = ref('info') // 'success' | 'error' | 'info'
const validationErrors = ref([])

// Skills & Experiences State (PBI-11)
const skills = ref([])
const experiences = ref([])
const isLoadingSkills = ref(false)
const isLoadingExperiences = ref(false)
const isSubmittingSkill = ref(false)
const isSubmittingExperience = ref(false)
const showSkillModal = ref(false)
const showExperienceModal = ref(false)
const editingSkill = ref(null)
const editingExperience = ref(null)
const skillForm = reactive({ skillName: '', skillLevel: '' })
const experienceForm = reactive({ position: '', companyName: '', startDate: '', endDate: '', description: '' })
const skillLevels = ['PEMULA', 'MENENGAH', 'MAHIR']

const profileMeta = reactive({
  id: null,
  active: true,
  createdAt: null,
  updatedAt: null,
})

const form = reactive({
  fullName: '',
  phoneNumber: '',
  headline: '',
  bio: '',
  city: '',
  province: '',
  avatarUrl: '',
})


const sampleStats = [
  { label: 'Pekerjaan Aktif', value: '1', accent: 'emerald' },
  { label: 'Selesai Bulan Ini', value: '1', accent: 'emerald' },
  { label: 'Saldo Tersedia', value: 'Rp 300.000', accent: 'emerald' },
  { label: 'Rating', value: '5.0', accent: 'amber', isRating: true },
]

const sampleJobs = [
  {
    id: 1,
    title: 'Desain Logo UMKM — Demo',
    client: 'Usaha UMKM Demo',
    price: 'Rp 300.000',
    deadline: '24 November 2026',
    status: 'Dikerjakan',
    icon: '🎨',
  },
  {
    id: 2,
    title: 'Fotografi Produk — Demo',
    client: 'Usaha UMKM Demo',
    price: 'Rp 150.000',
    deadline: '24 November 2026',
    status: 'Selesai',
    icon: '📷',
  },
]

const samplePackages = ref([
  {
    id: 1,
    title: 'Desain Logo UMKM — Demo',
    category: 'Desain Grafis',
    price: 'Mulai Rp 300.000',
    rating: '5.0',
    active: true,
    icon: '🎨',
  },
  {
    id: 2,
    title: 'Fotografi Produk — Demo',
    category: 'Fotografi Produk',
    price: 'Mulai Rp 150.000',
    rating: '5.0',
    active: true,
    icon: '📷',
  },
  {
    id: 3,
    title: 'Konten Media Sosial — Demo',
    category: 'Konten Media Sosial',
    price: 'Mulai Rp 200.000',
    rating: '5.0',
    active: true,
    icon: '📱',
  },
])

const menuItems = [
  { key: 'beranda', label: 'Beranda', icon: '🏠' },
  { key: 'request', label: 'Jelajahi Request', icon: '📋' },
  { key: 'pekerjaan', label: 'Pekerjaan Aktif', icon: '💼' },
  { key: 'paket', label: 'Paket Jasa Saya', icon: '🎨' },
  { key: 'profil', label: 'Profil & Portofolio', icon: '👤' },
  { key: 'keahlian', label: 'Keahlian & Pengalaman', icon: '🛠️' },
  { key: 'saldo', label: 'Saldo & Pembayaran', icon: '💰' },
  { key: 'ulasan', label: 'Ulasan Saya', icon: '⭐' },
  { key: 'pengaturan', label: 'Pengaturan', icon: '⚙️' },
]

const initials = computed(() => {
  if (!form.fullName) return 'FR'
  return form.fullName
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase() || '')
    .join('')
})

const displayPhoneLocal = computed({
  get() {
    if (!form.phoneNumber) return ''
    return form.phoneNumber.replace(/^\+62/, '')
  },
  set(val) {
    const cleaned = val.replace(/\s+/g, '')
    if (!cleaned) {
      form.phoneNumber = ''
    } else if (cleaned.startsWith('+62')) {
      form.phoneNumber = cleaned
    } else if (cleaned.startsWith('0')) {
      form.phoneNumber = `+62${cleaned.slice(1)}`
    } else {
      form.phoneNumber = `+62${cleaned}`
    }
  },
})

function showAlert(type, message, errors = []) {
  alertType.value = type
  alertMessage.value = message
  validationErrors.value = errors
}

function saveTokenAndLoad() {
  localStorage.setItem('jemari_token', tokenInput.value.trim())
  loadProfile()
}

async function loadProfile() {
  const token = localStorage.getItem('jemari_token')
  if (!token) {
    showAlert('info', 'Masukkan token JWT akun FREELANCER pada bilah atas untuk memuat profil.')
    return
  }

  isLoading.value = true
  alertMessage.value = ''
  validationErrors.value = []

  try {
    const res = await freelancerProfileService.getMyProfile()
    const data = res.data
    profileExists.value = true

    form.fullName = data.fullName || ''
    form.phoneNumber = data.phoneNumber || ''
    form.headline = data.headline || ''
    form.bio = data.bio || ''
    form.city = data.city || ''
    form.province = data.province || ''
    form.avatarUrl = data.avatarUrl || ''

    profileMeta.id = data.id
    profileMeta.active = data.active
    profileMeta.createdAt = data.createdAt
    profileMeta.updatedAt = data.updatedAt

    // Load skills & experiences from profile response
    if (data.skills) skills.value = data.skills
    if (data.experiences) experiences.value = data.experiences
  } catch (err) {
    if (err.status === 404) {
      profileExists.value = false
      showAlert('info', 'Profil freelancer belum dibuat. Silakan lengkapi form di bawah lalu simpan.')
    } else if (err.status === 401 || err.status === 403) {
      showAlert('error', 'Token JWT tidak valid, kedaluwarsa, atau bukan akun ber-role FREELANCER.')
    } else {
      showAlert('error', err.message || 'Gagal memuat profil freelancer.')
    }
  } finally {
    isLoading.value = false
  }
}

// ========== SKILLS CRUD (PBI-11) ==========
async function loadSkills() {
  isLoadingSkills.value = true
  try {
    const res = await freelancerSkillService.getMySkills()
    skills.value = res.data || []
  } catch (err) {
    showAlert('error', err.message || 'Gagal memuat keahlian.')
  } finally {
    isLoadingSkills.value = false
  }
}

function openSkillModal(skill = null) {
  editingSkill.value = skill
  if (skill) {
    skillForm.skillName = skill.skillName
    skillForm.skillLevel = skill.skillLevel || ''
  } else {
    skillForm.skillName = ''
    skillForm.skillLevel = ''
  }
  showSkillModal.value = true
}

function closeSkillModal() {
  showSkillModal.value = false
  editingSkill.value = null
  skillForm.skillName = ''
  skillForm.skillLevel = ''
}

async function handleSkillSubmit() {
  if (!skillForm.skillName.trim()) {
    showAlert('error', 'Nama keahlian wajib diisi.')
    return
  }
  isSubmittingSkill.value = true
  try {
    if (editingSkill.value) {
      await freelancerSkillService.updateSkill(editingSkill.value.id, {
        skillName: skillForm.skillName.trim(),
        skillLevel: skillForm.skillLevel || null
      })
      showAlert('success', 'Keahlian berhasil diperbarui.')
    } else {
      await freelancerSkillService.createSkill({
        skillName: skillForm.skillName.trim(),
        skillLevel: skillForm.skillLevel || null
      })
      showAlert('success', 'Keahlian berhasil ditambahkan.')
    }
    closeSkillModal()
    await loadSkills()
  } catch (err) {
    showAlert('error', err.message || 'Gagal menyimpan keahlian.')
  } finally {
    isSubmittingSkill.value = false
  }
}

async function handleSkillDelete(skill) {
  const confirmed = window.confirm(`Hapus keahlian "${skill.skillName}"?`)
  if (!confirmed) return
  try {
    await freelancerSkillService.deleteSkill(skill.id)
    showAlert('success', 'Keahlian berhasil dihapus.')
    await loadSkills()
  } catch (err) {
    showAlert('error', err.message || 'Gagal menghapus keahlian.')
  }
}

// ========== EXPERIENCES CRUD (PBI-11) ==========
async function loadExperiences() {
  isLoadingExperiences.value = true
  try {
    const res = await freelancerExperienceService.getMyExperiences()
    experiences.value = res.data || []
  } catch (err) {
    showAlert('error', err.message || 'Gagal memuat pengalaman kerja.')
  } finally {
    isLoadingExperiences.value = false
  }
}

function openExperienceModal(exp = null) {
  editingExperience.value = exp
  if (exp) {
    experienceForm.position = exp.position
    experienceForm.companyName = exp.companyName
    experienceForm.startDate = exp.startDate || ''
    experienceForm.endDate = exp.endDate || ''
    experienceForm.description = exp.description || ''
  } else {
    experienceForm.position = ''
    experienceForm.companyName = ''
    experienceForm.startDate = ''
    experienceForm.endDate = ''
    experienceForm.description = ''
  }
  showExperienceModal.value = true
}

function closeExperienceModal() {
  showExperienceModal.value = false
  editingExperience.value = null
  experienceForm.position = ''
  experienceForm.companyName = ''
  experienceForm.startDate = ''
  experienceForm.endDate = ''
  experienceForm.description = ''
}

async function handleExperienceSubmit() {
  if (!experienceForm.position.trim() || !experienceForm.companyName.trim() || !experienceForm.startDate) {
    showAlert('error', 'Posisi, nama perusahaan, dan tanggal mulai wajib diisi.')
    return
  }
  if (experienceForm.endDate && experienceForm.endDate < experienceForm.startDate) {
    showAlert('error', 'Tanggal selesai tidak boleh sebelum tanggal mulai.')
    return
  }
  isSubmittingExperience.value = true
  try {
    const payload = {
      position: experienceForm.position.trim(),
      companyName: experienceForm.companyName.trim(),
      startDate: experienceForm.startDate,
      endDate: experienceForm.endDate || null,
      description: experienceForm.description.trim() || null
    }
    if (editingExperience.value) {
      await freelancerExperienceService.updateExperience(editingExperience.value.id, payload)
      showAlert('success', 'Pengalaman kerja berhasil diperbarui.')
    } else {
      await freelancerExperienceService.createExperience(payload)
      showAlert('success', 'Pengalaman kerja berhasil ditambahkan.')
    }
    closeExperienceModal()
    await loadExperiences()
  } catch (err) {
    showAlert('error', err.message || 'Gagal menyimpan pengalaman kerja.')
  } finally {
    isSubmittingExperience.value = false
  }
}

async function handleExperienceDelete(exp) {
  const confirmed = window.confirm(`Hapus pengalaman kerja "${exp.position} di ${exp.companyName}"?`)
  if (!confirmed) return
  try {
    await freelancerExperienceService.deleteExperience(exp.id)
    showAlert('success', 'Pengalaman kerja berhasil dihapus.')
    await loadExperiences()
  } catch (err) {
    showAlert('error', err.message || 'Gagal menghapus pengalaman kerja.')
  }
}

// Load skills & experiences when needed
function loadSkillsAndExperiences() {
  if (profileExists.value) {
    loadSkills()
    loadExperiences()
  }
}

// Helper functions for Skills & Experiences UI
function getSkillLevelClass(level) {
  switch (level) {
    case 'PEMULA': return 'level-pemula'
    case 'MENENGAH': return 'level-menengah'
    case 'MAHIR': return 'level-mahir'
    default: return ''
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('id-ID', { month: 'short', year: 'numeric' })
}

async function handleSubmit() {
  isSaving.value = true
  alertMessage.value = ''
  validationErrors.value = []

  try {
    const payload = {
      fullName: form.fullName,
      phoneNumber: form.phoneNumber,
      headline: form.headline,
      bio: form.bio,
      city: form.city,
      province: form.province,
      avatarUrl: form.avatarUrl,
    }

    const res = await freelancerProfileService.saveMyProfile(payload)
    const data = res.data
    profileExists.value = true
    profileMeta.id = data.id
    profileMeta.active = data.active
    profileMeta.createdAt = data.createdAt
    profileMeta.updatedAt = data.updatedAt

    showAlert('success', res.message || 'Perubahan profil berhasil disimpan.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal menyimpan perubahan profil.', err.errors || [])
  } finally {
    isSaving.value = false
  }
}

async function handleDeactivate() {
  const confirmed = window.confirm(
    'Apakah Anda yakin ingin menonaktifkan profil freelancer ini? Profil tidak akan tampil di katalog aktif.'
  )
  if (!confirmed) return

  isDeactivating.value = true
  alertMessage.value = ''

  try {
    const res = await freelancerProfileService.deactivateMyProfile()
    profileMeta.active = false
    showAlert('success', res.message || 'Profil berhasil dinonaktifkan.')
  } catch (err) {
    showAlert('error', err.message || 'Gagal menonaktifkan profil.')
  } finally {
    isDeactivating.value = false
  }
}

onMounted(() => {
  if (tokenInput.value) {
    loadProfile()
  }
})
</script>

<template>
  <div class="app-shell">
    <!-- Top Header Bar -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="topbar-left">
          <a href="#" class="brand-logo" @click.prevent="activeMenu = 'beranda'">JEMARI</a>
        </div>

        <div class="topbar-center">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Cari jasa desain, fotografi, konten..."
            />
          </div>
        </div>

        <div class="topbar-right">
          <button
            type="button"
            class="btn-token-toggle"
            @click="showTokenBar = !showTokenBar"
          >
            {{ showTokenBar ? 'Sembunyikan Token' : 'Token JWT' }}
          </button>

          <button type="button" class="icon-btn" title="Notifikasi">
            <span>🔔</span>
            <span class="notif-dot"></span>
          </button>

          <div class="user-chip" @click="activeMenu = 'profil'">
            <div class="avatar-sm">{{ initials }}</div>
            <span class="chevron">⌄</span>
          </div>
        </div>
      </div>
    </header>

    <!-- Dev Token Banner (Terhubung langsung ke API Backend Spring Boot) -->
    <div v-if="showTokenBar" class="dev-banner">
      <div class="dev-banner-inner">
        <div class="dev-info">
          <div class="dev-title-row">
            <span class="dev-icon">🔑</span>
            <strong class="dev-title">Koneksi API Backend &bull; Role Freelancer</strong>
            <span
              id="preview-status-badge"
              :class="['pill-status', profileMeta.active ? 'pill-active' : 'pill-inactive']"
            >
              {{ profileMeta.active ? 'Aktif' : 'Nonaktif' }}
            </span>
          </div>
          <p class="dev-desc">
            Tempel token JWT akun <code>FREELANCER</code> untuk menyinkronkan profil langsung ke database MySQL.
          </p>
        </div>

        <div class="dev-controls">
          <input
            id="jwt-token-input"
            v-model="tokenInput"
            type="text"
            class="token-input"
            placeholder="eyJhbGciOiJIUzI1NiJ9..."
          />
          <button
            id="btn-load-token"
            type="button"
            class="btn-emerald"
            :disabled="isLoading"
            @click="saveTokenAndLoad"
          >
            {{ isLoading ? 'Memuat...' : 'Muat Profil' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Main Workspace Container -->
    <div class="workspace">
      <!-- Left Sidebar -->
      <aside class="sidebar">
        <div class="welcome-card">
          <div class="avatar-lg">
            <img
              v-if="form.avatarUrl"
              :src="form.avatarUrl"
              alt="Avatar"
              class="avatar-lg-img"
              @error="form.avatarUrl = ''"
            />
            <span v-else>{{ initials }}</span>
          </div>
          <div class="welcome-text">
            <span class="welcome-sub">Selamat datang,</span>
            <strong id="preview-full-name" class="welcome-name">
              {{ form.fullName || 'Freelancer JEMARI' }}
            </strong>
          </div>
        </div>

        <nav class="side-nav">
          <button
            v-for="item in menuItems"
            :key="item.key"
            type="button"
            :class="['side-nav-item', { active: activeMenu === item.key }]"
            @click="activeMenu = item.key"
          >
            <span class="nav-emoji">{{ item.icon }}</span>
            <span class="nav-text">{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <!-- Right Content Area -->
      <main class="content-area">
        <!-- Global Alert -->
        <div v-if="alertMessage" id="profile-alert" :class="['alert-box', `alert-${alertType}`]">
          <p class="alert-msg">{{ alertMessage }}</p>
          <ul v-if="validationErrors.length" class="alert-errors">
            <li v-for="(err, idx) in validationErrors" :key="idx">
              {{ err.field ? `${err.field}: ` : '' }}{{ err.message || err }}
            </li>
          </ul>
        </div>

        <!-- TAB 1: PROFIL & PORTOFOLIO (FITUR UTAMA PBI-01 / JRI-14) -->
        <section v-if="activeMenu === 'profil'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Profil &amp; Portofolio</h1>
            <p class="page-subtitle">
              Profil lengkap meningkatkan kepercayaan pelaku UMKM dan rekan kerjamu.
            </p>
          </div>

          <div class="panel-card">
            <!-- Avatar & Quick Summary Row -->
            <div class="profile-top-row">
              <div class="avatar-xl">
                <img
                  v-if="form.avatarUrl"
                  :src="form.avatarUrl"
                  alt="Foto Profil"
                  class="avatar-xl-img"
                  @error="form.avatarUrl = ''"
                />
                <span v-else>{{ initials }}</span>
              </div>

              <div class="avatar-actions">
                <div class="avatar-btn-row">
                  <label for="avatarUrl" class="btn-outline-green">Ubah URL Foto Profil</label>
                  <span v-if="profileMeta.id" class="id-tag">ID Profil #{{ profileMeta.id }}</span>
                </div>
                <p class="helper-text">
                  Gunakan tautan gambar JPG/PNG publik atau avatar profesional Anda.
                </p>
              </div>
            </div>

            <!-- Form Profil Freelancer -->
            <form id="freelancer-profile-form" class="profile-form" @submit.prevent="handleSubmit">
              <div class="field-group">
                <label for="fullName" class="field-label">
                  Nama Lengkap <span class="req">*</span>
                </label>
                <input
                  id="fullName"
                  v-model="form.fullName"
                  type="text"
                  maxlength="150"
                  required
                  class="form-control"
                  placeholder="Contoh: Arsya Syuhada"
                />
              </div>

              <div class="field-group">
                <label for="phoneNumber" class="field-label">
                  Nomor WhatsApp <span class="req">*</span>
                </label>
                <div class="phone-input-group">
                  <span class="phone-prefix">+62</span>
                  <input
                    id="phoneNumber"
                    v-model="displayPhoneLocal"
                    type="text"
                    maxlength="20"
                    required
                    class="form-control phone-field"
                    placeholder="812-3456-7890"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="headline" class="field-label">
                  Headline / Keahlian Utama (pisahkan dengan koma)
                </label>
                <input
                  id="headline"
                  v-model="form.headline"
                  type="text"
                  maxlength="200"
                  class="form-control"
                  placeholder="Contoh: Desain Logo, Kemasan Produk, Fullstack Web"
                />
              </div>

              <div class="two-col">
                <div class="field-group">
                  <label for="city" class="field-label">Kota / Kabupaten</label>
                  <input
                    id="city"
                    v-model="form.city"
                    type="text"
                    maxlength="100"
                    class="form-control"
                    placeholder="Contoh: Jakarta Selatan"
                  />
                </div>

                <div class="field-group">
                  <label for="province" class="field-label">Provinsi</label>
                  <input
                    id="province"
                    v-model="form.province"
                    type="text"
                    maxlength="100"
                    class="form-control"
                    placeholder="Contoh: DKI Jakarta"
                  />
                </div>
              </div>

              <div class="field-group">
                <label for="avatarUrl" class="field-label">URL Foto Profil</label>
                <input
                  id="avatarUrl"
                  v-model="form.avatarUrl"
                  type="url"
                  maxlength="500"
                  class="form-control"
                  placeholder="https://example.com/foto-profil.jpg"
                />
              </div>

              <div class="field-group">
                <label for="bio" class="field-label">Bio Singkat</label>
                <textarea
                  id="bio"
                  v-model="form.bio"
                  rows="4"
                  maxlength="2000"
                  class="form-control textarea-control"
                  placeholder="Ceritakan pengalaman, spesialisasi, dan hasil karya Anda untuk membantu UMKM..."
                ></textarea>
              </div>

              <!-- Ringkasan Live Pratinjau -->
              <div class="live-summary-box">
                <div class="summary-item">
                  <span class="summary-label">Lokasi Tampil:</span>
                  <span id="preview-location" class="summary-val">
                    {{
                      form.city || form.province
                        ? [form.city, form.province].filter(Boolean).join(', ')
                        : 'Belum diisi'
                    }}
                  </span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Keahlian:</span>
                  <span id="preview-headline" class="summary-val">
                    {{ form.headline || 'Belum diisi' }}
                  </span>
                </div>
              </div>

              <div class="form-footer-actions">
                <button
                  id="btn-save-profile"
                  type="submit"
                  class="btn-amber"
                  :disabled="isSaving"
                >
                  {{ isSaving ? 'Menyimpan...' : 'Simpan Perubahan' }}
                </button>

                <button
                  v-if="profileExists"
                  id="btn-deactivate-profile"
                  type="button"
                  class="btn-danger-soft"
                  :disabled="isDeactivating || !profileMeta.active"
                  @click="handleDeactivate"
                >
                  {{ isDeactivating ? 'Memproses...' : 'Nonaktifkan Profil' }}
                </button>
              </div>
            </form>
          </div>
        </section>

        <!-- TAB: KEahlian & PENGALAMAN (PBI-11) -->
        <section v-else-if="activeMenu === 'keahlian'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Keahlian & Pengalaman Kerja</h1>
            <p class="page-subtitle">
              Kelola keahlian dan riwayat pekerjaan yang tampil di profil publik untuk menarik UMKM.
            </p>
          </div>

          <!-- KEahlian -->
          <div class="panel-card">
            <div class="section-header-row">
              <h2 class="section-title">Keahlian</h2>
              <button type="button" class="btn-amber-sm" @click="openSkillModal">+ Tambah Keahlian</button>
            </div>

            <div v-if="isLoadingSkills" class="state-box">Memuat keahlian...</div>

            <div v-else-if="skills.length === 0" class="state-box empty">
              <div class="empty-icon">🛠️</div>
              <h3>Belum Ada Keahlian</h3>
              <p>Tambahkan keahlian Anda agar UMKM mengetahui keahlian utama Anda.</p>
              <button class="btn-amber-sm" @click="openSkillModal">+ Tambah Keahlian Pertama</button>
            </div>

            <div v-else class="list-stack">
              <div v-for="skill in skills" :key="skill.id" class="list-row-card skill-row">
                <div class="row-left">
                  <div class="skill-info">
                    <h3 class="row-title">{{ skill.skillName }}</h3>
                    <p class="row-meta" v-if="skill.skillLevel">
                      <span class="skill-level-badge" :class="getSkillLevelClass(skill.skillLevel)">{{ skill.skillLevel }}</span>
                    </p>
                  </div>
                </div>
                <div class="row-right">
                  <button type="button" class="btn-outline-gray" @click="openSkillModal(skill)">✏️ Edit</button>
                  <button type="button" class="btn-danger-soft" @click="handleSkillDelete(skill)">Hapus</button>
                </div>
              </div>
            </div>
          </div>

          <!-- PENGALAMAN KERJA -->
          <div class="panel-card" style="margin-top: 1.5rem;">
            <div class="section-header-row">
              <h2 class="section-title">Pengalaman Kerja</h2>
              <button type="button" class="btn-amber-sm" @click="openExperienceModal">+ Tambah Pengalaman</button>
            </div>

            <div v-if="isLoadingExperiences" class="state-box">Memuat pengalaman kerja...</div>

            <div v-else-if="experiences.length === 0" class="state-box empty">
              <div class="empty-icon">💼</div>
              <h3>Belum Ada Pengalaman Kerja</h3>
              <p>Tambahkan riwayat pekerjaan Anda untuk meningkatkan kepercayaan UMKM.</p>
              <button class="btn-amber-sm" @click="openExperienceModal">+ Tambah Pengalaman Pertama</button>
            </div>

            <div v-else class="list-stack">
              <div v-for="exp in experiences" :key="exp.id" class="list-row-card experience-row">
                <div class="row-left">
                  <div class="experience-info">
                    <h3 class="row-title">{{ exp.position }}</h3>
                    <p class="row-meta">{{ exp.companyName }}</p>
                    <p class="row-meta exp-date">
                      {{ formatDate(exp.startDate) }} &#8211; {{ exp.endDate ? formatDate(exp.endDate) : 'Sekarang' }}
                    </p>
                    <p class="row-meta exp-desc" v-if="exp.description">{{ exp.description }}</p>
                  </div>
                </div>
                <div class="row-right">
                  <button type="button" class="btn-outline-gray" @click="openExperienceModal(exp)">✏️ Edit</button>
                  <button type="button" class="btn-danger-soft" @click="handleExperienceDelete(exp)">Hapus</button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- TAB 2: BERANDA -->
        <section v-else-if="activeMenu === 'beranda'" class="section-view">
          <div class="kpi-grid">
            <div v-for="stat in sampleStats" :key="stat.label" class="kpi-card">
              <span class="kpi-label">{{ stat.label }}</span>
              <div :class="['kpi-value', stat.accent === 'amber' ? 'text-amber' : 'text-emerald']">
                <span v-if="stat.isRating">⭐</span> {{ stat.value }}
              </div>
            </div>
          </div>

          <div class="escrow-banner">
            <div class="escrow-left">
              <span class="escrow-emoji">🎉</span>
              <p class="escrow-text">
                <strong>DP Diterima!</strong> UMKM <strong>Usaha UMKM Demo</strong> memilihmu untuk
                "Desain Logo UMKM — Demo". DP Rp 150.000 sudah masuk escrow. Silakan mulai mengerjakan!
              </p>
            </div>
            <button type="button" class="btn-amber-sm" @click="activeMenu = 'pekerjaan'">
              Lihat Detail Pesanan
            </button>
          </div>

          <div class="panel-card">
            <div class="section-header-row">
              <h2 class="section-title">Pekerjaan Aktif</h2>
              <a href="#" class="link-emerald" @click.prevent="activeMenu = 'pekerjaan'">
                Lihat Semua &rarr;
              </a>
            </div>

            <div class="list-stack">
              <div v-for="job in sampleJobs" :key="job.id" class="list-row-card">
                <div class="row-left">
                  <div class="icon-square">{{ job.icon }}</div>
                  <div>
                    <h3 class="row-title">{{ job.title }}</h3>
                    <p class="row-meta">{{ job.client }}</p>
                  </div>
                </div>
                <div class="row-right">
                  <span :class="['badge-job', job.status === 'Dikerjakan' ? 'badge-blue' : 'badge-gray']">
                    {{ job.status }}
                  </span>
                  <button type="button" class="btn-outline-gray" @click="activeMenu = 'pekerjaan'">
                    Lihat Detail
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="cta-dashed-box">
            <div class="cta-left">
              <span class="cta-emoji">🎨</span>
              <span>
                Belum punya paket jasa? Publikasikan paketmu agar muncul di <strong>Katalog JEMARI</strong>.
              </span>
            </div>
            <button type="button" class="btn-amber-sm" @click="activeMenu = 'paket'">
              Kelola Paket Jasa
            </button>
          </div>
        </section>

        <!-- TAB 3: JELAJAHI REQUEST -->
        <section v-else-if="activeMenu === 'request'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Jelajahi Request</h1>
            <p class="page-subtitle">
              Request terbuka dari UMKM yang mencari keahlianmu — ajukan diri dengan penawaranmu.
            </p>
          </div>

          <div class="panel-card request-card">
            <div class="request-main">
              <div class="icon-circle-soft">🎨</div>
              <div class="request-body">
                <h3 class="row-title">Desain Kemasan Usaha — Demo</h3>
                <p class="row-meta">oleh Usaha UMKM Demo &bull; Dipost 17 November 2026</p>
                <p class="request-desc">
                  Membutuhkan desain kemasan untuk produk kuliner. Ini data contoh khusus pengembangan.
                </p>
                <div class="request-footer-meta">
                  <span class="budget-text">Budget Rp 200.000 – Rp 400.000</span>
                  <span class="muted-meta">📅 24 November 2026</span>
                  <span class="muted-meta">👥 1 pelamar</span>
                </div>
              </div>
            </div>
            <button type="button" class="btn-emerald">✈️ Ajukan Diri</button>
          </div>
        </section>

        <!-- TAB 4: PEKERJAAN AKTIF -->
        <section v-else-if="activeMenu === 'pekerjaan'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Pesanan Aktif</h1>
            <p class="page-subtitle">Semua pesanan di mana Anda terlibat sebagai freelancer.</p>
          </div>

          <div class="list-stack">
            <div v-for="job in sampleJobs" :key="job.id" class="panel-card list-row-card">
              <div class="row-left">
                <div class="icon-square">{{ job.icon }}</div>
                <div>
                  <h3 class="row-title">{{ job.title }}</h3>
                  <p class="row-meta">{{ job.price }} &bull; Deadline {{ job.deadline }}</p>
                </div>
              </div>
              <div class="row-right">
                <div class="avatar-xs">{{ initials }}</div>
                <span :class="['badge-job', job.status === 'Dikerjakan' ? 'badge-blue' : 'badge-gray']">
                  {{ job.status }}
                </span>
              </div>
            </div>
          </div>
        </section>

        <!-- TAB 5: PAKET JASA SAYA -->
        <section v-else-if="activeMenu === 'paket'" class="section-view">
          <div class="section-header-row mb-4">
            <div>
              <h1 class="page-title">Paket Jasa Saya</h1>
              <p class="page-subtitle">Paket aktif akan tampil di Katalog JEMARI.</p>
            </div>
            <button type="button" class="btn-amber">+ Buat Paket Baru</button>
          </div>

          <div class="list-stack">
            <div v-for="pkg in samplePackages" :key="pkg.id" class="panel-card list-row-card">
              <div class="row-left">
                <div class="icon-square-lg">{{ pkg.icon }}</div>
                <div>
                  <h3 class="row-title">{{ pkg.title }}</h3>
                  <p class="row-meta">
                    {{ pkg.icon }} {{ pkg.category }} &bull; {{ pkg.price }} &bull; ⭐ {{ pkg.rating }}
                  </p>
                </div>
              </div>
              <div class="row-right">
                <span class="muted-meta">{{ pkg.active ? 'Aktif' : 'Nonaktif' }}</span>
                <label class="switch">
                  <input v-model="pkg.active" type="checkbox" />
                  <span class="slider"></span>
                </label>
              </div>
            </div>
          </div>
        </section>

        <!-- TAB 6: SALDO & PEMBAYARAN -->
        <section v-else-if="activeMenu === 'saldo'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Saldo &amp; Pembayaran</h1>
            <p class="page-subtitle">Ringkasan pendapatan proyek dan dana escrow Anda.</p>
          </div>
          <div class="panel-card">
            <span class="kpi-label">Saldo Tersedia untuk Penarikan</span>
            <div class="kpi-value text-emerald" style="margin: 0.4rem 0 1rem">Rp 300.000</div>
            <p class="row-meta">Dana dari proyek yang telah diselesaikan akan masuk ke saldo tersedia.</p>
          </div>
        </section>

        <!-- TAB 7: ULASAN SAYA -->
        <section v-else-if="activeMenu === 'ulasan'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Ulasan Saya</h1>
          </div>

          <div class="panel-card rating-summary-card">
            <span class="big-rating">5.0</span>
            <div>
              <div class="stars">⭐⭐⭐⭐⭐</div>
              <span class="row-meta">1 ulasan diterima</span>
            </div>
          </div>

          <div class="panel-card" style="margin-top: 1rem">
            <div class="review-header">
              <div class="row-left">
                <div class="avatar-sm">UU</div>
                <div>
                  <h3 class="row-title">Usaha UMKM Demo</h3>
                  <span class="row-meta">17 November 2026</span>
                </div>
              </div>
              <div class="stars">⭐⭐⭐⭐⭐</div>
            </div>
            <p class="review-body">Hasil pekerjaan contoh sesuai kebutuhan.</p>
          </div>
        </section>

        <!-- TAB 8: PENGATURAN -->
        <section v-else-if="activeMenu === 'pengaturan'" class="section-view">
          <div class="page-heading">
            <h1 class="page-title">Pengaturan</h1>
            <p class="page-subtitle">Kelola informasi akun dan status keaktifan profil JEMARI kamu.</p>
          </div>

          <div class="panel-card">
            <div class="profile-form">
              <div class="field-group">
                <label class="field-label">Nama Akun Terdaftar</label>
                <input
                  type="text"
                  class="form-control readonly-control"
                  :value="form.fullName || 'arsya.freelancer@jemari.id'"
                  readonly
                />
              </div>

              <div class="field-group">
                <label class="field-label">Nomor WhatsApp</label>
                <div class="phone-input-group">
                  <span class="phone-prefix">+62</span>
                  <input
                    v-model="displayPhoneLocal"
                    type="text"
                    class="form-control phone-field"
                    placeholder="812-3456-7890"
                  />
                </div>
              </div>

              <div class="form-footer-actions">
                <button type="button" class="btn-amber" :disabled="isSaving" @click="handleSubmit">
                  {{ isSaving ? 'Menyimpan...' : 'Simpan' }}
                </button>
                <button
                  v-if="profileExists"
                  type="button"
                  class="btn-danger-soft"
                  :disabled="isDeactivating || !profileMeta.active"
                  @click="handleDeactivate"
                >
                  Nonaktifkan Akun Freelancer
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>

  <!-- Skill Modal (PBI-11) -->
  <Teleport to="body">
    <div v-if="showSkillModal" class="modal-overlay" @click.self="closeSkillModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ editingSkill ? 'Edit Keahlian' : 'Tambah Keahlian' }}</h3>
          <button type="button" class="modal-close" @click="closeSkillModal" aria-label="Tutup">✕</button>
        </div>
        <form @submit.prevent="handleSkillSubmit" class="modal-form">
          <div class="form-group">
            <label for="skillName" class="field-label">Nama Keahlian <span class="req">*</span></label>
            <input
              id="skillName"
              v-model="skillForm.skillName"
              type="text"
              maxlength="100"
              required
              class="form-control"
              placeholder="Contoh: Desain Logo, React, Fotografi Produk"
              autofocus
            />
          </div>

          <div class="form-group">
            <label for="skillLevel" class="field-label">Level Keahlian</label>
            <select id="skillLevel" v-model="skillForm.skillLevel" class="form-control">
              <option value="">-- Pilih Level (Opsional) --</option>
              <option v-for="level in skillLevels" :key="level" :value="level">{{ level }}</option>
            </select>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-outline-gray" @click="closeSkillModal">Batal</button>
            <button type="submit" class="btn-amber" :disabled="isSubmittingSkill">
              {{ isSubmittingSkill ? 'Menyimpan...' : (editingSkill ? 'Simpan Perubahan' : 'Tambah Keahlian') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>

  <!-- Experience Modal (PBI-11) -->
  <Teleport to="body">
    <div v-if="showExperienceModal" class="modal-overlay" @click.self="closeExperienceModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ editingExperience ? 'Edit Pengalaman Kerja' : 'Tambah Pengalaman Kerja' }}</h3>
          <button type="button" class="modal-close" @click="closeExperienceModal" aria-label="Tutup">✕</button>
        </div>
        <form @submit.prevent="handleExperienceSubmit" class="modal-form">
          <div class="form-row">
            <div class="form-group">
              <label for="position" class="field-label">Posisi / Jabatan <span class="req">*</span></label>
              <input
                id="position"
                v-model="experienceForm.position"
                type="text"
                maxlength="150"
                required
                class="form-control"
                placeholder="Contoh: Senior UI/UX Designer"
              />
            </div>

            <div class="form-group">
              <label for="companyName" class="field-label">Nama Perusahaan <span class="req">*</span></label>
              <input
                id="companyName"
                v-model="experienceForm.companyName"
                type="text"
                maxlength="150"
                required
                class="form-control"
                placeholder="Contoh: PT. Digital Kreatif Indonesia"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="startDate" class="field-label">Tanggal Mulai <span class="req">*</span></label>
              <input
                id="startDate"
                v-model="experienceForm.startDate"
                type="date"
                required
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label for="endDate" class="field-label">Tanggal Selesai</label>
              <input
                id="endDate"
                v-model="experienceForm.endDate"
                type="date"
                class="form-control"
              />
              <p class="field-hint">Kosongkan jika masih bekerja di sini</p>
            </div>
          </div>

          <div class="form-group">
            <label for="description" class="field-label">Deskripsi Pekerjaan</label>
            <textarea
              id="description"
              v-model="experienceForm.description"
              rows="3"
              maxlength="1000"
              class="form-control textarea-control"
              placeholder="Tulis tanggung jawab, proyek, dan pencapaian Anda..."
            ></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn-outline-gray" @click="closeExperienceModal">Batal</button>
            <button type="submit" class="btn-amber" :disabled="isSubmittingExperience">
              {{ isSubmittingExperience ? 'Menyimpan...' : (editingExperience ? 'Simpan Perubahan' : 'Tambah Pengalaman') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: #f8fafb;
  color: #111827;
  font-family: 'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}

/* Topbar */
.topbar {
  background-color: #ffffff;
  border-bottom: 1px solid #eef0f3;
  position: sticky;
  top: 0;
  z-index: 20;
}

.topbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0.75rem 1.75rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
}

.brand-logo {
  font-size: 1.35rem;
  font-weight: 800;
  color: #166534;
  text-decoration: none;
  letter-spacing: 0.02em;
}

.topbar-center {
  flex: 1;
  max-width: 460px;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  border-radius: 9999px;
  padding: 0.5rem 1rem;
  gap: 0.5rem;
}

.search-icon {
  font-size: 0.9rem;
  color: #9ca3af;
}

.search-input {
  border: none;
  background: transparent;
  width: 100%;
  font-size: 0.88rem;
  color: #1f2937;
  outline: none;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.btn-token-toggle {
  font-size: 0.78rem;
  font-weight: 600;
  color: #166534;
  background-color: #ecfdf5;
  border: 1px solid #bbf7d0;
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  cursor: pointer;
}

.icon-btn {
  position: relative;
  background: transparent;
  border: none;
  font-size: 1.15rem;
  cursor: pointer;
  padding: 0.25rem;
}

.notif-dot {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 7px;
  height: 7px;
  border-radius: 9999px;
  background-color: #ef4444;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  cursor: pointer;
}

.avatar-sm {
  width: 34px;
  height: 34px;
  border-radius: 9999px;
  background-color: #0284c7;
  color: #ffffff;
  font-size: 0.8rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chevron {
  color: #6b7280;
  font-size: 0.9rem;
}

/* Dev Token Banner */
.dev-banner {
  background-color: #edf7f1;
  border-bottom: 1px solid #d1e7da;
}

.dev-banner-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0.75rem 1.75rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.dev-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dev-title {
  font-size: 0.88rem;
  color: #14532d;
}

.dev-desc {
  margin: 0.15rem 0 0;
  font-size: 0.78rem;
  color: #4b5563;
}

.pill-status {
  font-size: 0.72rem;
  font-weight: 700;
  padding: 0.15rem 0.55rem;
  border-radius: 9999px;
}

.pill-active {
  background-color: #dcfce7;
  color: #166534;
}

.pill-inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.dev-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  max-width: 520px;
}

.token-input {
  flex: 1;
  padding: 0.48rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.84rem;
  background-color: #ffffff;
}

/* Workspace Layout */
.workspace {
  max-width: 1280px;
  margin: 0 auto;
  padding: 1.75rem;
  display: grid;
  grid-template-columns: 245px 1fr;
  gap: 2rem;
  align-items: start;
}

@media (max-width: 900px) {
  .workspace {
    grid-template-columns: 1fr;
  }
}

/* Sidebar */
.welcome-card {
  background-color: #ffffff;
  border: 1px solid #eef0f3;
  border-radius: 14px;
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 0.85rem;
  margin-bottom: 1rem;
}

.avatar-lg {
  width: 48px;
  height: 48px;
  border-radius: 9999px;
  background-color: #0284c7;
  color: #ffffff;
  font-weight: 700;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-lg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.welcome-sub {
  font-size: 0.76rem;
  color: #6b7280;
}

.welcome-name {
  font-size: 0.92rem;
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.side-nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 0.95rem;
  border-radius: 10px;
  border: none;
  border-left: 3px solid transparent;
  background: transparent;
  color: #4b5563;
  font-size: 0.9rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: all 0.15s;
}

.side-nav-item:hover {
  background-color: #f1f5f9;
  color: #111827;
}

.side-nav-item.active {
  background-color: #e6f4ec;
  color: #166534;
  font-weight: 600;
  border-left-color: #166534;
}

/* Content Area */
.page-heading {
  margin-bottom: 1.25rem;
}

.page-title {
  margin: 0 0 0.25rem;
  font-size: 1.55rem;
  font-weight: 800;
  color: #111827;
}

.page-subtitle {
  margin: 0;
  font-size: 0.9rem;
  color: #6b7280;
}

.panel-card {
  background-color: #ffffff;
  border: 1px solid #eef0f3;
  border-radius: 14px;
  padding: 1.75rem;
}

/* Alert */
.alert-box {
  padding: 0.85rem 1.1rem;
  border-radius: 10px;
  margin-bottom: 1.25rem;
  font-size: 0.88rem;
}

.alert-success {
  background-color: #dcfce7;
  border: 1px solid #86efac;
  color: #166534;
}

.alert-error {
  background-color: #fee2e2;
  border: 1px solid #fca5a5;
  color: #991b1b;
}

.alert-info {
  background-color: #fef3c7;
  border: 1px solid #fcd34d;
  color: #92400e;
}

.alert-msg {
  margin: 0;
  font-weight: 600;
}

.alert-errors {
  margin: 0.35rem 0 0 1.2rem;
  padding: 0;
}

/* Profile Form Styling */
.profile-top-row {
  display: flex;
  align-items: center;
  gap: 1.15rem;
  margin-bottom: 1.5rem;
}

.avatar-xl {
  width: 68px;
  height: 68px;
  border-radius: 9999px;
  background-color: #0284c7;
  color: #ffffff;
  font-size: 1.4rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-xl-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-btn-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-outline-green {
  display: inline-block;
  padding: 0.45rem 0.95rem;
  border: 1.5px solid #166534;
  border-radius: 8px;
  color: #166534;
  font-size: 0.84rem;
  font-weight: 600;
  cursor: pointer;
}

.id-tag {
  font-size: 0.78rem;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 0.2rem 0.6rem;
  border-radius: 6px;
}

.helper-text {
  margin: 0.35rem 0 0;
  font-size: 0.78rem;
  color: #6b7280;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.15rem;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.86rem;
  font-weight: 600;
  color: #1f2937;
}

.req {
  color: #dc2626;
}

.form-control {
  width: 100%;
  box-sizing: border-box;
  padding: 0.65rem 0.85rem;
  border: 1px solid #d1d5db;
  border-radius: 9px;
  font-size: 0.9rem;
  color: #111827;
  background-color: #ffffff;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.form-control:focus {
  outline: none;
  border-color: #166534;
  box-shadow: 0 0 0 3px rgba(22, 101, 52, 0.12);
}

.readonly-control {
  background-color: #f3f4f6;
  color: #4b5563;
}

.textarea-control {
  resize: vertical;
  font-family: inherit;
}

.phone-input-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.phone-prefix {
  padding: 0.65rem 0.85rem;
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 9px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 640px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}

.live-summary-box {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.85rem 1rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  font-size: 0.84rem;
}

.summary-label {
  color: #64748b;
  margin-right: 0.35rem;
}

.summary-val {
  font-weight: 600;
  color: #0f172a;
}

.form-footer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

/* Buttons */
.btn-amber {
  background-color: #f59e0b;
  color: #111827;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 0.68rem 1.35rem;
  border: none;
  border-radius: 9px;
  cursor: pointer;
}

.btn-amber:hover:not(:disabled) {
  background-color: #d97706;
}

.btn-amber-sm {
  background-color: #f59e0b;
  color: #111827;
  font-weight: 700;
  font-size: 0.82rem;
  padding: 0.55rem 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-emerald {
  background-color: #166534;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.85rem;
  padding: 0.55rem 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-danger-soft {
  background-color: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  font-weight: 600;
  font-size: 0.85rem;
  padding: 0.62rem 1.1rem;
  border-radius: 9px;
  cursor: pointer;
}

.btn-outline-gray {
  background-color: #ffffff;
  border: 1px solid #d1d5db;
  color: #374151;
  font-size: 0.82rem;
  font-weight: 600;
  padding: 0.45rem 0.85rem;
  border-radius: 8px;
  cursor: pointer;
}

/* Dashboard Other Tabs */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.25rem;
}

@media (max-width: 820px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.kpi-card {
  background-color: #ffffff;
  border: 1px solid #eef0f3;
  border-radius: 14px;
  padding: 1.25rem;
}

.kpi-label {
  font-size: 0.8rem;
  color: #6b7280;
}

.kpi-value {
  font-size: 1.65rem;
  font-weight: 800;
  margin-top: 0.35rem;
}

.text-emerald {
  color: #166534;
}

.text-amber {
  color: #f59e0b;
}

.escrow-banner {
  background-color: #e6f4ec;
  border-left: 4px solid #166534;
  border-radius: 12px;
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.escrow-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.escrow-text {
  margin: 0;
  font-size: 0.86rem;
  color: #14532d;
  line-height: 1.45;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.section-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
}

.link-emerald {
  font-size: 0.85rem;
  font-weight: 600;
  color: #166534;
  text-decoration: none;
}

.list-stack {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.list-row-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.25rem;
  border: 1px solid #eef0f3;
  border-radius: 12px;
  background-color: #ffffff;
}

.row-left,
.row-right {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.icon-square {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background-color: #ecfdf5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.icon-square-lg {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background-color: #ecfdf5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.row-title {
  margin: 0 0 0.2rem;
  font-size: 0.96rem;
  font-weight: 700;
}

.row-meta {
  margin: 0;
  font-size: 0.82rem;
  color: #6b7280;
}

.badge-job {
  font-size: 0.76rem;
  font-weight: 600;
  padding: 0.25rem 0.7rem;
  border-radius: 9999px;
}

.badge-blue {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.badge-gray {
  background-color: #f3f4f6;
  color: #4b5563;
}

.cta-dashed-box {
  margin-top: 1.25rem;
  border: 1.5px dashed #86efac;
  background-color: #f0fdf4;
  border-radius: 12px;
  padding: 0.95rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.cta-left {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  font-size: 0.88rem;
}

.request-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.request-main {
  display: flex;
  gap: 1rem;
}

.icon-circle-soft {
  width: 48px;
  height: 48px;
  border-radius: 9999px;
  background-color: #ecfdf5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
}

.request-desc {
  margin: 0.5rem 0;
  font-size: 0.88rem;
  color: #374151;
}

.request-footer-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.82rem;
  flex-wrap: wrap;
}

.budget-text {
  font-weight: 700;
  color: #166534;
}

.muted-meta {
  color: #6b7280;
  font-size: 0.82rem;
}

.avatar-xs {
  width: 28px;
  height: 28px;
  border-radius: 9999px;
  background-color: #0284c7;
  color: #ffffff;
  font-size: 0.72rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Toggle Switch */
.switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #cbd5e1;
  border-radius: 9999px;
  transition: 0.2s;
}

.slider::before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: #ffffff;
  border-radius: 9999px;
  transition: 0.2s;
}

.switch input:checked + .slider {
  background-color: #166534;
}

.switch input:checked + .slider::before {
  transform: translateX(18px);
}

/* Rating & Review */
.rating-summary-card {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.big-rating {
  font-size: 2.25rem;
  font-weight: 800;
  color: #166534;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.65rem;
}

.review-body {
  margin: 0;
  font-size: 0.9rem;
  color: #374151;
}

/* Skills & Experiences (PBI-11) */
.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.section-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
}

.btn-amber-sm {
  background-color: #f59e0b;
  color: #111827;
  font-weight: 700;
  font-size: 0.82rem;
  padding: 0.55rem 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-amber-sm:hover {
  background-color: #d97706;
}

.skill-row .skill-info,
.experience-row .experience-info {
  min-width: 0;
}

.skill-level-badge {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: 9999px;
  text-transform: uppercase;
}

.skill-level-badge.level-pemula {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.skill-level-badge.level-menengah {
  background-color: #fef3c7;
  color: #92400e;
}

.skill-level-badge.level-mahir {
  background-color: #dcfce7;
  color: #166534;
}

.exp-date {
  font-size: 0.8rem;
  color: #6b7280;
  font-style: italic;
}

.exp-desc {
  font-size: 0.82rem;
  color: #4b5563;
  margin-top: 0.25rem !important;
  line-height: 1.4;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f3f4f6;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
}

.modal-close {
  background: #f3f4f6;
  border: none;
  color: #4b5563;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s;
}

.modal-close:hover {
  background-color: #e5e7eb;
}

.modal-form {
  padding: 1.5rem;
}

.modal-form .form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 640px) {
  .modal-form .form-row {
    grid-template-columns: 1fr;
  }
}

.modal-form .form-group {
  margin-bottom: 1rem;
}

.modal-form .field-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.4rem;
}

.modal-form .req {
  color: #dc2626;
}

.modal-form .form-control {
  width: 100%;
  box-sizing: border-box;
  padding: 0.65rem 0.85rem;
  border: 1px solid #d1d5db;
  border-radius: 9px;
  font-size: 0.9rem;
  color: #111827;
  background-color: #ffffff;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.modal-form .form-control:focus {
  outline: none;
  border-color: #166534;
  box-shadow: 0 0 0 3px rgba(22, 101, 52, 0.12);
}

.modal-form .textarea-control {
  resize: vertical;
  font-family: inherit;
}

.modal-form .field-hint {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
  color: #9ca3af;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.modal-footer .btn-outline-gray {
  padding: 0.6rem 1.2rem;
  font-size: 0.85rem;
}

.modal-footer .btn-amber {
  padding: 0.6rem 1.2rem;
  font-size: 0.85rem;
}

/* List row card adjustments for skills/experiences */
.list-row-card.skill-row,
.list-row-card.experience-row {
  padding: 1rem 1.25rem;
}

.list-row-card .row-left {
  flex: 1;
  min-width: 0;
}

.list-row-card .row-right {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.list-row-card .row-right button {
  white-space: nowrap;
}
</style>