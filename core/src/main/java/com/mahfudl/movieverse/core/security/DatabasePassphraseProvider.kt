package com.mahfudl.movieverse.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Passphrase database beserta informasi apakah passphrase tersebut baru dibuat. */
internal class DatabasePassphrase(val value: ByteArray, val isNew: Boolean)

/**
 * Menyediakan passphrase untuk enkripsi database (SQLCipher).
 *
 * Teknik: passphrase acak 32 byte dibuat sekali, lalu dienkripsi AES-256/GCM memakai kunci
 * yang tersimpan di **Android Keystore** (kunci tidak bisa diekspor dari perangkat). Hanya
 * ciphertext yang disimpan di SharedPreferences privat, sehingga passphrase tidak pernah
 * tertulis sebagai teks biasa di penyimpanan.
 */
internal class DatabasePassphraseProvider(context: Context) {

    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * Mengembalikan passphrase tersimpan. Bila belum ada, atau tidak bisa didekripsi lagi
     * (mis. kunci Keystore hilang setelah perangkat di-restore), dibuat passphrase baru dan
     * [DatabasePassphrase.isNew] bernilai true.
     */
    fun getOrCreate(): DatabasePassphrase {
        readStored()?.let { return DatabasePassphrase(it, isNew = false) }
        return DatabasePassphrase(createAndStore(), isNew = true)
    }

    private fun readStored(): ByteArray? {
        val stored = preferences.getString(KEY_PASSPHRASE, null) ?: return null
        return try {
            val blob = Base64.decode(stored, Base64.NO_WRAP)
            val iv = blob.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val cipherText = blob.copyOfRange(GCM_IV_LENGTH_BYTES, blob.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            // Data rusak / kunci tidak valid lagi: anggap tidak ada, lalu buat yang baru.
            null
        }
    }

    private fun createAndStore(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val blob = cipher.iv + cipher.doFinal(passphrase)
        preferences.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(blob, Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "movieverse_db_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PREFERENCES_NAME = "movieverse_secure_prefs"
        const val KEY_PASSPHRASE = "db_passphrase"
        const val KEY_SIZE_BITS = 256
        const val PASSPHRASE_LENGTH_BYTES = 32
        const val GCM_IV_LENGTH_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
