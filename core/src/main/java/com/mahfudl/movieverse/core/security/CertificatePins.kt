package com.mahfudl.movieverse.core.security

import okhttp3.CertificatePinner

/**
 * Certificate pinning untuk koneksi ke server TMDB (OkHttp [CertificatePinner]).
 *
 * Koneksi hanya diterima bila rantai sertifikat server memuat salah satu public key (SPKI
 * SHA-256) di [PINS]. Pin berupa public key CA (root) yang dipakai TMDB beserta cadangannya,
 * sehingga tetap valid saat sertifikat leaf diperpanjang atau CA penerbit berganti.
 *
 * Untuk memperbarui / memverifikasi pin dengan rantai sertifikat server yang sebenarnya,
 * jalankan `scripts/update_pins.sh` dari root proyek lalu rebuild.
 */
internal object CertificatePins {

    const val HOST = "api.themoviedb.org"

    val PINS: List<String> = listOf(
        // Amazon Root CA 1..4
        "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=",
        "sha256/f0KW/FtqTjs108NpYj42SrGvOB2PpxIVM8nWxjPqJGE=",
        "sha256/NqvDJlas/GRcYbcWE8S/IceH9cq77kg0jVhZeAPXq8k=",
        "sha256/9+ze1cZgR9KO1kZrVDxA4HQ6voHRCSVNz4RdTCx4U8U=",
        // Starfield Services Root Certificate Authority - G2 (cross-sign Amazon)
        "sha256/KwccWaCgrnaw6tsrrSO61FgLacNgG2MMLq8GE6+oP5I=",
        // ISRG Root X1 / X2 (Let's Encrypt)
        "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=",
        "sha256/diGVwiVYbubAI3RW4hB9xU8e/CH2GnkuvVFZE8zmgzI=",
        // Google Trust Services Root R1..R4
        "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=",
        "sha256/Vfd95BwDeSQo+NUYxVEEIlvkOlWY2SalKK1lPhzOx78=",
        "sha256/QXnqsnS3I0vOq8Cch4m1lQyYcWu6XOwAmlYdd2KFDm8=",
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=",
        // GlobalSign Root R1 / ECC R4
        "sha256/K87oWBWM9UZfyddvDfoxL+8lpNyoUB2ptGtn0fv6G2Q=",
        "sha256/CLOmM1/OXvSPjw5UOYbAf9GKOxImEp9hhku9W90fHMk=",
        // DigiCert Global Root CA / G2 / G3
        "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=",
        "sha256/i7WTqTvh0OioIruIfFR4kMPnBqrS2rdiVPl/s2uC/CY=",
        "sha256/uUwZgwDOxcBXrQcntwu+kYFpkiVkOaezL0WYEZ3anJc=",
        // USERTrust RSA / ECC (Sectigo)
        "sha256/x4QzPSC810K5/cMjb05Qm4k3Bw5zBn4lTdO/nEW/Td4=",
        "sha256/ICGRfpgmOUXIWcQ/HXPLQTkFPEFPoDyjvH7ohhQpjzs=",
        // Baltimore CyberTrust Root (Cloudflare lama)
        "sha256/Y9mvm0exBk1JoQ57f9Vm28jKo5lFm/woKcVxrYxu80o="
    )

    fun pinner(): CertificatePinner = CertificatePinner.Builder()
        .apply { PINS.forEach { add(HOST, it) } }
        .build()
}
