/**
 * Service untuk komunikasi data Portofolio Freelancer (PBI-06 / UC-17, FR-06, FR-03)
 * Mendukung integrasi REST API backend Spring Boot (/api/portfolios)
 * dengan fallback mock storage lokal otomatis saat server backend belum aktif.
 */

const API_BASE = '/api/portfolios'

const INITIAL_MOCK_PORTFOLIOS = [
  {
    id: 1,
    title: 'Desain Branding & Logo Kopi Nusantara',
    judul: 'Desain Branding & Logo Kopi Nusantara',
    description: 'Konsep identitas visual modern minimalis untuk kedai kopi lokal UMKM, mencakup logo utama, palet warna, dan mockup kemasan cup.',
    deskripsi: 'Konsep identitas visual modern minimalis untuk kedai kopi lokal UMKM, mencakup logo utama, palet warna, dan mockup kemasan cup.',
    imageUrl: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80',
    gambarUrl: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80',
    createdAt: '2026-11-14 11:20:00'
  },
  {
    id: 2,
    title: 'Foto Produk Kuliner & Pastry UMKM',
    judul: 'Foto Produk Kuliner & Pastry UMKM',
    description: 'Sesi pemotretan produk roti artisan dan pastry dengan pencahayaan natural untuk keperluan feed Instagram dan katalog GoFood/GrabFood.',
    deskripsi: 'Sesi pemotretan produk roti artisan dan pastry dengan pencahayaan natural untuk keperluan feed Instagram dan katalog GoFood/GrabFood.',
    imageUrl: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80',
    gambarUrl: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80',
    createdAt: '2026-11-12 16:45:00'
  },
  {
    id: 3,
    title: 'Desain Banner Promosi & Feed Media Sosial',
    judul: 'Desain Banner Promosi & Feed Media Sosial',
    description: 'Paket 10 template feed dan story Instagram untuk promosi diskon produk fashion hijab dan aksesoris UMKM lokal.',
    deskripsi: 'Paket 10 template feed dan story Instagram untuk promosi diskon produk fashion hijab dan aksesoris UMKM lokal.',
    imageUrl: 'https://images.unsplash.com/photo-1542744094-3a31f272c490?auto=format&fit=crop&w=800&q=80',
    gambarUrl: 'https://images.unsplash.com/photo-1542744094-3a31f272c490?auto=format&fit=crop&w=800&q=80',
    createdAt: '2026-11-10 09:15:00'
  }
]

function getLocalPortfolios() {
  const stored = localStorage.getItem('jemari_mock_portfolios')
  if (!stored) {
    localStorage.setItem('jemari_mock_portfolios', JSON.stringify(INITIAL_MOCK_PORTFOLIOS))
    return [...INITIAL_MOCK_PORTFOLIOS]
  }
  try {
    return JSON.parse(stored)
  } catch (e) {
    return [...INITIAL_MOCK_PORTFOLIOS]
  }
}

function saveLocalPortfolios(data) {
  localStorage.setItem('jemari_mock_portfolios', JSON.stringify(data))
}

function getAuthHeader() {
  const token = localStorage.getItem('jemari_token')
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

export const portfolioService = {
  /**
   * Mengambil daftar portofolio freelancer yang sedang login (Role: FREELANCER)
   */
  async getMyPortfolios() {
    try {
      const res = await fetch(`${API_BASE}/my`, {
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      })
      if (!res.ok) throw new Error(`HTTP error ${res.status}`)
      const json = await res.json()
      if (json.success && json.data) {
        saveLocalPortfolios(json.data)
        return json.data
      }
      return getLocalPortfolios()
    } catch (err) {
      console.warn('Backend offline / gagal memuat portofolio, beralih ke local storage:', err.message)
      return getLocalPortfolios()
    }
  },

  /**
   * Mengambil galeri publik portofolio freelancer berdasarkan ID Profil (Publik untuk UMKM)
   */
  async getPortfoliosByFreelancerId(freelancerProfileId) {
    try {
      const res = await fetch(`${API_BASE}/freelancer/${freelancerProfileId}`, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
      if (!res.ok) throw new Error(`HTTP error ${res.status}`)
      const json = await res.json()
      return json.data || []
    } catch (err) {
      console.warn('Backend offline, beralih ke local storage:', err.message)
      return getLocalPortfolios()
    }
  },

  /**
   * Mengambil detail satu karya portofolio
   */
  async getPortfolioById(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}`)
      if (!res.ok) throw new Error(`HTTP error ${res.status}`)
      const json = await res.json()
      return json.data
    } catch (err) {
      const items = getLocalPortfolios()
      const found = items.find(p => p.id === Number(id))
      if (!found) throw new Error('Karya portofolio tidak ditemukan')
      return found
    }
  },

  /**
   * Menambahkan karya portofolio baru
   */
  async createPortfolio(payload) {
    // Validasi format file dan batas ukuran 5MB di sisi klien
    if (payload.imageUrl && payload.imageUrl.startsWith('data:')) {
      const valid = payload.imageUrl.startsWith('data:image/jpeg') ||
                    payload.imageUrl.startsWith('data:image/jpg') ||
                    payload.imageUrl.startsWith('data:image/png') ||
                    payload.imageUrl.startsWith('data:image/webp')
      if (!valid) {
        throw new Error('Format berkas tidak didukung. Harap unggah format JPG, PNG, atau WebP')
      }
      const base64Data = payload.imageUrl.split(',')[1] || ''
      const sizeBytes = base64Data.length * 0.75
      if (sizeBytes > 5 * 1024 * 1024) {
        throw new Error('Ukuran berkas melebihi batas maksimal 5 MB')
      }
    }

    try {
      const res = await fetch(API_BASE, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify(payload)
      })
      const json = await res.json()
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Gagal menambahkan karya portofolio')
      }
      return json.data
    } catch (err) {
      // Local fallback jika backend belum aktif
      if (err.message.includes('melebihi') || err.message.includes('Format')) {
        throw err
      }
      console.warn('Backend gagal / offline, menyimpan karya ke local storage:', err.message)
      const items = getLocalPortfolios()
      const newItem = {
        id: Date.now(),
        title: payload.title,
        judul: payload.title,
        description: payload.description,
        deskripsi: payload.description,
        imageUrl: payload.imageUrl,
        gambarUrl: payload.imageUrl,
        createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
      }
      items.unshift(newItem)
      saveLocalPortfolios(items)
      return newItem
    }
  },

  /**
   * Memperbarui karya portofolio yang sudah ada
   */
  async updatePortfolio(id, payload) {
    try {
      const res = await fetch(`${API_BASE}/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify(payload)
      })
      const json = await res.json()
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Gagal memperbarui karya portofolio')
      }
      return json.data
    } catch (err) {
      console.warn('Backend offline, memperbarui karya di local storage:', err.message)
      const items = getLocalPortfolios()
      const idx = items.findIndex(p => p.id === Number(id))
      if (idx !== -1) {
        items[idx] = {
          ...items[idx],
          title: payload.title,
          judul: payload.title,
          description: payload.description,
          deskripsi: payload.description,
          imageUrl: payload.imageUrl,
          gambarUrl: payload.imageUrl,
          updatedAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
        }
        saveLocalPortfolios(items)
        return items[idx]
      }
      throw new Error('Karya portofolio tidak ditemukan')
    }
  },

  /**
   * Menghapus karya portofolio (soft delete)
   */
  async deletePortfolio(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}`, {
        method: 'DELETE',
        headers: {
          ...getAuthHeader()
        }
      })
      const json = await res.json()
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Gagal menghapus karya portofolio')
      }
      return true
    } catch (err) {
      console.warn('Backend offline, menghapus karya dari local storage:', err.message)
      const items = getLocalPortfolios().filter(p => p.id !== Number(id))
      saveLocalPortfolios(items)
      return true
    }
  }
}
