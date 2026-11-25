import assert from 'node:assert'
import { formatRupiah, formatDurasi, formatRentangDurasi } from './rupiahPaket.js'

// 1. Pengujian formatRupiah
assert.strictEqual(formatRupiah(50000), 'Rp\u00a050.000', 'Format Rupiah 50000')
assert.strictEqual(formatRupiah(null), '—', 'Format Rupiah null')
assert.strictEqual(formatRupiah(undefined), '—', 'Format Rupiah undefined')

// 2. Pengujian formatDurasi
assert.strictEqual(formatDurasi(3), '3 hari', 'Durasi 3 hari')
assert.strictEqual(formatDurasi('5'), '5 hari', 'Durasi string 5')
assert.strictEqual(formatDurasi(null), '—', 'Durasi null')
assert.strictEqual(formatDurasi(undefined), '—', 'Durasi undefined')
assert.strictEqual(formatDurasi(''), '—', 'Durasi string kosong')
assert.strictEqual(formatDurasi(0), '—', 'Durasi 0')
assert.strictEqual(formatDurasi(-2), '—', 'Durasi negatif')

// 3. Pengujian formatRentangDurasi (PBI-04 Revisi)
// Kasus A: Belum ada varian / durasi kosong
assert.strictEqual(formatRentangDurasi(null, null), 'Belum ada estimasi', 'Tanpa varian (null, null)')
assert.strictEqual(formatRentangDurasi(undefined, undefined), 'Belum ada estimasi', 'Tanpa varian (undefined)')
assert.strictEqual(formatRentangDurasi(0, 0), 'Belum ada estimasi', 'Tanpa varian (0, 0)')
assert.strictEqual(formatRentangDurasi('', ''), 'Belum ada estimasi', 'Tanpa varian string kosong')

// Kasus B: Durasi semua varian sama (satu angka "3 hari")
assert.strictEqual(formatRentangDurasi(3, 3), '3 hari', 'Durasi sama (3, 3)')
assert.strictEqual(formatRentangDurasi('3', '3'), '3 hari', 'Durasi sama string ("3", "3")')
assert.strictEqual(formatRentangDurasi(1, 1), '1 hari', 'Durasi sama (1, 1)')

// Kasus C: Durasi berbeda (rentang "3–7 hari")
assert.strictEqual(formatRentangDurasi(3, 7), '3–7 hari', 'Rentang durasi (3, 7)')
assert.strictEqual(formatRentangDurasi('3', '7'), '3–7 hari', 'Rentang durasi string ("3", "7")')
assert.strictEqual(formatRentangDurasi(1, 14), '1–14 hari', 'Rentang durasi (1, 14)')

// Kasus D: Salah satu durasi null / undefined
assert.strictEqual(formatRentangDurasi(3, null), '3 hari', 'Durasi (3, null)')
assert.strictEqual(formatRentangDurasi(null, 5), '5 hari', 'Durasi (null, 5)')

console.log('Seluruh pengujian unit rupiahPaket (PBI-04) BERHASIL!')
