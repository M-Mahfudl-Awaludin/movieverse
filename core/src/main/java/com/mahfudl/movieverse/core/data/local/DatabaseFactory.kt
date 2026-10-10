package com.mahfudl.movieverse.core.data.local

import android.content.Context
import androidx.room.Room
import com.mahfudl.movieverse.core.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Membuat [MovieDatabase] yang seluruh isinya terenkripsi dengan SQLCipher (AES-256).
 * Passphrase diambil dari [DatabasePassphraseProvider] (dilindungi Android Keystore).
 */
internal object DatabaseFactory {

    private const val DATABASE_NAME = "movieverse_encrypted.db"

    /** Nama database lama (tanpa enkripsi) dari versi sebelumnya; dihapus agar tidak tertinggal. */
    private const val LEGACY_DATABASE_NAME = "movieverse.db"

    fun create(context: Context, passphraseProvider: DatabasePassphraseProvider): MovieDatabase {
        System.loadLibrary("sqlcipher")

        context.deleteDatabase(LEGACY_DATABASE_NAME)
        val passphrase = passphraseProvider.getOrCreate()
        if (passphrase.isNew) {
            // Berkas lama tidak bisa dibuka dengan passphrase baru; hapus agar tidak error.
            context.deleteDatabase(DATABASE_NAME)
        }

        return Room.databaseBuilder(context, MovieDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(SupportOpenHelperFactory(passphrase.value))
            .fallbackToDestructiveMigration()
            .build()
    }
}
