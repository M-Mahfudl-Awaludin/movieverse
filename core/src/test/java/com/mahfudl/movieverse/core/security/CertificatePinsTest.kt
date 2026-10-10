package com.mahfudl.movieverse.core.security

import java.security.cert.Certificate
import java.util.Base64
import javax.net.ssl.SSLPeerUnverifiedException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificatePinsTest {

    @Test
    fun pinnedHostIsTmdbApi() {
        assertEquals("api.themoviedb.org", CertificatePins.HOST)
    }

    @Test
    fun everyPinIsAWellFormedSha256Hash() {
        CertificatePins.PINS.forEach { pin ->
            assertTrue("Format pin salah: $pin", pin.startsWith("sha256/"))
            val hash = Base64.getDecoder().decode(pin.removePrefix("sha256/"))
            assertEquals("Panjang hash salah: $pin", 32, hash.size)
        }
    }

    @Test
    fun pinsAreUniqueAndIncludeBackups() {
        assertEquals(CertificatePins.PINS.size, CertificatePins.PINS.toSet().size)
        assertTrue("Butuh minimal satu pin cadangan", CertificatePins.PINS.size >= 2)
    }

    @Test(expected = SSLPeerUnverifiedException::class)
    fun pinnedHostRejectsChainWithoutMatchingPin() {
        CertificatePins.pinner().check(CertificatePins.HOST, emptyList<Certificate>())
    }

    @Test
    fun otherHostsAreNotPinned() {
        CertificatePins.pinner().check("example.com", emptyList<Certificate>())
    }
}
