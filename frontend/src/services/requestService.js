/**
 * Service untuk komunikasi data Request Kebutuhan UMKM (PBI-05 / UC-08, UC-10)
 * Mendukung integrasi REST API backend Spring Boot (/api/requests)
 * dengan fallback mock storage lokal otomatis saat server backend belum aktif.
 */

const API_BASE = '/api/requests'

// Data inisial cadangan sesuai Mockup 4.5.5
const INITIAL_MOCK_DATA = [
  {
    id: 1,
    judul: 'Foto produk kue bolu & brownies',
    kategori: 'Fotografi Produk',
    deskripsi: 'Dibutuhkan fotografer untuk memfoto 10 varian kue rumahan. Hasil foto akan digunakan untuk etalase GoFood dan katalog promosi WhatsApp.',
    budget: 350000,
    budgetMin: 250000,
    budgetMax: 400000,
    deadline: '2026-11-25',
    status: 'OPEN',
    statusLabel: 'Dibuka',
    pelamarCount: 3,
    freelancerName: null,
    createdAt: '2026-11-14 10:00:00'
  },
  {
    id: 2,
    judul: 'Banner promosi media sosial bulanan',
    kategori: 'Desain Banner & Poster',
    deskripsi: 'Membuat 5 desain template postingan Instagram promo akhir tahun untuk UMKM keripik singkong khas Bandung.',
    budget: 200000,
    budgetMin: 150000,
    budgetMax: 250000,
    deadline: '2026-11-30',
    status: 'IN_PROGRESS',
    statusLabel: 'Diproses',
    pelamarCount: 5,
    freelancerName: 'Rina D.',
    createdAt: '2026-11-12 14:30:00'
  },
  {
    id: 3,
    judul: 'Desain ulang logo Warung Kopi Barokah',
    kategori: 'Desain Grafis & Logo',
    deskripsi: 'Re-design logo warkop agar bernuansa modern minimalis dengan dominan warna coklat kopi dan krem.',
    budget: 150000,
    budgetMin: 100000,
    budgetMax: 200000,
    deadline: '2026-11-18',
    status: 'COMPLETED',
    statusLabel: 'Selesai',
    pelamarCount: 4,
    freelancerName: 'Budi A.',
    createdAt: '2026-11-05 09:15:00'
  }
]

function getLocalStore() {
  const stored = localStorage.getItem('jemari_requests_data')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      // jika parsing gagal, reset
    }
  }
  localStorage.setItem('jemari_requests_data', JSON.stringify(INITIAL_MOCK_DATA))
  return INITIAL_MOCK_DATA
}

function saveLocalStore(data) {
  localStorage.setItem('jemari_requests_data', JSON.stringify(data))
}

function getAuthHeader() {
  const token = localStorage.getItem('jemari_jwt_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export const requestService = {
  // 1. Ambil daftar request milik UMKM (Mockup 4.5.5)
  async getMyRequests() {
    try {
      const res = await fetch(`${API_BASE}/my`, {
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      })
      if (res.ok) {
        const body = await res.json()
        if (body.data && body.data.length > 0) {
          saveLocalStore(body.data)
          return body.data
        }
      }
    } catch {
      // Backend offline, fallback ke localStorage
    }
    return getLocalStore()
  },

  // 2. Ambil detail request berdasarkan ID
  async getRequestById(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}`)
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
    } catch {
      // Fallback
    }
    const store = getLocalStore()
    const found = store.find((item) => String(item.id) === String(id))
    if (!found) {
      throw new Error(`Request #${id} tidak ditemukan`)
    }
    return found
  },

  // 3. Buat request baru (UC-08 / Mockup 4.5.4)
  async createRequest(payload) {
    const requestBody = {
      title: payload.judul || payload.title,
      judul: payload.judul || payload.title,
      category: payload.kategori || payload.category,
      kategori: payload.kategori || payload.category,
      description: payload.deskripsi || payload.description,
      deskripsi: payload.deskripsi || payload.description,
      budget: Number(payload.harga || payload.budget || payload.budgetMin || 0),
      harga: Number(payload.harga || payload.budget || payload.budgetMin || 0),
      budgetMin: Number(payload.budgetMin || payload.harga || 0),
      budgetMax: Number(payload.budgetMax || payload.harga || 0),
      deadline: payload.deadline
    }

    try {
      const res = await fetch(API_BASE, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify(requestBody)
      })
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
    } catch {
      // Fallback
    }

    // Mock store
    const store = getLocalStore()
    const newEntry = {
      ...requestBody,
      id: Date.now(),
      status: 'OPEN',
      statusLabel: 'Dibuka',
      pelamarCount: 0,
      freelancerName: null,
      createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    store.unshift(newEntry)
    saveLocalStore(store)
    return newEntry
  },

  // 4. Ubah request terbuka (UC-10)
  async updateRequest(id, payload) {
    const requestBody = {
      title: payload.judul || payload.title,
      judul: payload.judul || payload.title,
      category: payload.kategori || payload.category,
      kategori: payload.kategori || payload.category,
      description: payload.deskripsi || payload.description,
      deskripsi: payload.deskripsi || payload.description,
      budget: Number(payload.harga || payload.budget || payload.budgetMin || 0),
      harga: Number(payload.harga || payload.budget || payload.budgetMin || 0),
      budgetMin: Number(payload.budgetMin || payload.harga || 0),
      budgetMax: Number(payload.budgetMax || payload.harga || 0),
      deadline: payload.deadline
    }

    try {
      const res = await fetch(`${API_BASE}/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        },
        body: JSON.stringify(requestBody)
      })
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
    } catch {
      // Fallback
    }

    const store = getLocalStore()
    const index = store.findIndex((item) => String(item.id) === String(id))
    if (index !== -1) {
      store[index] = { ...store[index], ...requestBody }
      saveLocalStore(store)
      return store[index]
    }
    throw new Error('Gagal memperbarui request')
  },

  // 5. Tutup request (UC-10)
  async closeRequest(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}/close`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...getAuthHeader()
        }
      })
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
    } catch {
      // Fallback
    }

    const store = getLocalStore()
    const index = store.findIndex((item) => String(item.id) === String(id))
    if (index !== -1) {
      store[index].status = 'CLOSED'
      store[index].statusLabel = 'Ditutup'
      saveLocalStore(store)
      return store[index]
    }
    throw new Error('Gagal menutup request')
  },

  // 6. Hapus request (Soft delete)
  async deleteRequest(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}`, {
        method: 'DELETE',
        headers: { ...getAuthHeader() }
      })
      if (res.ok) {
        return true
      }
    } catch {
      // Fallback
    }

    const store = getLocalStore().filter((item) => String(item.id) !== String(id))
    saveLocalStore(store)
    return true
  },

  // ========== REFERENCE PHOTOS (PBI-12) ==========

  // GET /api/requests/{requestId}/reference-photos
  async getReferencePhotos(requestId) {
    try {
      const res = await fetch(`${API_BASE}/${requestId}/reference-photos`, {
        headers: { ...getAuthHeader() }
      })
      if (res.ok) {
        const body = await res.json()
        return body.data || []
      }
    } catch (err) {
      console.warn('Gagal memuat foto referensi dari API:', err.message)
    }
    return []
  },

  // POST /api/requests/{requestId}/reference-photos
  async uploadReferencePhoto(requestId, file) {
    const formData = new FormData()
    formData.append('file', file)

    try {
      const res = await fetch(`${API_BASE}/${requestId}/reference-photos`, {
        method: 'POST',
        headers: { ...getAuthHeader() },
        body: formData
      })
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
      const errBody = await res.json().catch(() => ({}))
      throw new Error(errBody.message || 'Gagal mengunggah foto referensi')
    } catch (err) {
      if (err.message.includes('Gagal mengunggah')) throw err
      throw new Error('Gagal mengunggah foto referensi: ' + err.message)
    }
  },

  // PUT /api/requests/{requestId}/reference-photos/{photoId}
  async replaceReferencePhoto(requestId, photoId, file) {
    const formData = new FormData()
    formData.append('file', file)

    try {
      const res = await fetch(`${API_BASE}/${requestId}/reference-photos/${photoId}`, {
        method: 'PUT',
        headers: { ...getAuthHeader() },
        body: formData
      })
      if (res.ok) {
        const body = await res.json()
        return body.data
      }
      const errBody = await res.json().catch(() => ({}))
      throw new Error(errBody.message || 'Gagal mengganti foto referensi')
    } catch (err) {
      if (err.message.includes('Gagal mengganti')) throw err
      throw new Error('Gagal mengganti foto referensi: ' + err.message)
    }
  },

  // DELETE /api/requests/{requestId}/reference-photos/{photoId}
  async deleteReferencePhoto(requestId, photoId) {
    try {
      const res = await fetch(`${API_BASE}/${requestId}/reference-photos/${photoId}`, {
        method: 'DELETE',
        headers: { ...getAuthHeader() }
      })
      if (res.ok) {
        return true
      }
      const errBody = await res.json().catch(() => ({}))
      throw new Error(errBody.message || 'Gagal menghapus foto referensi')
    } catch (err) {
      if (err.message.includes('Gagal menghapus')) throw err
      throw new Error('Gagal menghapus foto referensi: ' + err.message)
    }
  }
}
