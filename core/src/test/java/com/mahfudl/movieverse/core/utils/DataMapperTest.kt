package com.mahfudl.movieverse.core.utils

import com.mahfudl.movieverse.core.data.remote.response.NamedDto
import com.mahfudl.movieverse.core.testDto
import com.mahfudl.movieverse.core.testEntity
import com.mahfudl.movieverse.core.testMovie
import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DataMapperTest {

    private fun httpError(code: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }

    @Test
    fun dtoWithoutIdIsDropped() {
        assertNull(testDto(id = null).toDomain())
    }

    @Test
    fun blankTitleFallsBackToPlaceholder() {
        assertEquals("Tanpa judul", testDto(title = "   ").toDomain()?.title)
        assertEquals("Tanpa judul", testDto(title = null).toDomain()?.title)
    }

    @Test
    fun imagePathsGetTmdbBaseUrl() {
        val movie = testDto().toDomain()

        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", movie?.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", movie?.backdropUrl)
    }

    @Test
    fun missingImagePathsBecomeEmptyStrings() {
        val movie = testDto(posterPath = null, backdropPath = null).toDomain()

        assertEquals("", movie?.posterUrl)
        assertEquals("", movie?.backdropUrl)
    }

    @Test
    fun detailGenreNamesTakePriorityOverGenreIds() {
        val movie = testDto(genres = listOf(NamedDto("Thriller")), genreIds = listOf(28)).toDomain()

        assertEquals(listOf("Thriller"), movie?.genres)
    }

    @Test
    fun genreIdsAreResolvedWhenNamesAreMissing() {
        val movie = testDto(genreIds = listOf(28, 878, 999_999)).toDomain()

        assertEquals(listOf("Action", "Science Fiction"), movie?.genres)
    }

    @Test
    fun companyIsFirstNonNullName() {
        val companies = listOf(NamedDto(null), NamedDto("Warner"), NamedDto("Legendary"))

        assertEquals("Warner", testDto(companies = companies).toDomain()?.company)
        assertEquals("", testDto(companies = null).toDomain()?.company)
    }

    @Test
    fun dtoListDropsInvalidAndDuplicateItems() {
        val result = listOf(testDto(1), testDto(null), testDto(2), testDto(1)).toDomain()

        assertEquals(listOf(1, 2), result.map { it.movieId })
    }

    @Test
    fun movieToEntityJoinsGenresAndKeepsFavoritedAt() {
        val entity = testMovie(favoritedAt = 5_000L).toEntity()

        assertEquals("Action|Drama", entity.genres)
        assertEquals(5_000L, entity.addedAt)
    }

    @Test
    fun movieWithoutFavoritedAtGetsCurrentTimestamp() {
        val before = System.currentTimeMillis()

        val entity = testMovie(favoritedAt = 0L).toEntity()

        assertTrue(entity.addedAt >= before)
    }

    @Test
    fun entityToDomainSplitsGenresAndRestoresFavoritedAt() {
        val movie = testEntity(addedAt = 7_000L).toDomain()

        assertEquals(listOf("Action", "Drama"), movie.genres)
        assertEquals(7_000L, movie.favoritedAt)
    }

    @Test
    fun blankGenresInEntityBecomeEmptyList() {
        val movie = testEntity().copy(genres = "").toDomain()

        assertTrue(movie.genres.isEmpty())
    }

    @Test
    fun domainSurvivesEntityRoundTrip() {
        val original = testMovie(id = 9, favoritedAt = 42L)

        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun unauthorizedMessageMentionsApiKey() {
        assertTrue(httpError(401).toUserMessage().contains("TMDB_API_KEY"))
    }

    @Test
    fun rateLimitMessageAsksToWait() {
        assertTrue(httpError(429).toUserMessage().contains("Terlalu banyak"))
    }

    @Test
    fun otherHttpErrorsIncludeStatusCode() {
        assertTrue(httpError(500).toUserMessage().contains("500"))
    }

    @Test
    fun ioErrorsMapToOfflineMessage() {
        assertTrue(IOException().toUserMessage().contains("internet"))
        assertTrue(SocketTimeoutException().toUserMessage().contains("internet"))
    }

    @Test
    fun unknownErrorsUseTheirOwnMessageOrAGenericOne() {
        assertEquals("boom", IllegalStateException("boom").toUserMessage())
        assertEquals("Terjadi kesalahan tak terduga.", IllegalStateException().toUserMessage())
    }
}
