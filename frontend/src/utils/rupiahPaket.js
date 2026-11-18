/**
 * Utilitas pemformatan angka ke Rupiah dan estimasi durasi hari untuk PBI-04.
 */

/**
 * Memformat nilai angka ke format Rupiah standar Indonesia.
 * Contoh: 50000 -> "Rp 50.000"
 *
 * @param {number|string|null|undefined} value Nilai nominal yang akan diformat
 * @returns {string} String terformat Rupiah atau "—" jika null/undefined
 */
export function formatRupiah(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const number = Number(value)
  if (Number.isNaN(number)) {
    return '—'
  }

  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(number)
}

/**
 * Memformat durasi dalam satuan hari.
 * Contoh: 3 -> "3 hari"
 *
 * @param {number|string|null|undefined} days Jumlah hari
 * @returns {string} String durasi terformat
 */
export function formatDurasi(days) {
  if (days === null || days === undefined || days === '') {
    return '—'
  }

  const num = Number(days)
  if (Number.isNaN(num) || num <= 0) {
    return '—'
  }

  return `${num} hari`
}

/**
 * Memformat rentang durasi pengerjaan (tercepat dan terlama).
 * Contoh: (2, 5) -> "2–5 hari", (3, 3) -> "3 hari"
 *
 * @param {number|null|undefined} tercepat Durasi tercepat dalam hari
 * @param {number|null|undefined} terlama Durasi terlama dalam hari
 * @returns {string} String rentang estimasi
 */
export function formatRentangDurasi(tercepat, terlama) {
  if (!tercepat && !terlama) {
    return '—'
  }

  if (tercepat && terlama && tercepat !== terlama) {
    return `${tercepat}–${terlama} hari`
  }

  const single = tercepat || terlama
  return formatDurasi(single)
}
