package com.mahfudl.movieverse.ui.model

import com.mahfudl.movieverse.testMovie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiMapperTest {

    @Test
    fun mapsAllPresentationFields() {
        val ui = testMovie(id = 3).toUi()

        assertEquals(3, ui.movieId)
        assertEquals("Movie 3", ui.title)
        assertEquals("2024 • Action", ui.metaText)
        assertEquals("2024 • 2j 1m • Studio", ui.subtitle)
        assertEquals("7.2", ui.scoreText)
        assertEquals("1.2K", ui.votesText)
        assertEquals("2j 1m", ui.runtimeText)
        assertEquals("Released", ui.status)
        assertEquals("2024-05-01", ui.releaseText)
        assertEquals("EN", ui.languageText)
        assertEquals(listOf("Action", "Drama"), ui.genres)
    }

    @Test
    fun scoreIsHiddenWhenMissingOrZero() {
        assertNull(testMovie(voteAverage = null).toUi().scoreText)
        assertNull(testMovie(voteAverage = 0.0).toUi().scoreText)
    }

    @Test
    fun scoreUsesOneDecimalWithDot() {
        assertEquals("8.0", testMovie(voteAverage = 8.0).toUi().scoreText)
    }

    @Test
    fun voteCountIsAbbreviated() {
        assertEquals("999", testMovie(voteCount = 999).toUi().votesText)
        assertEquals("1.2K", testMovie(voteCount = 1_234).toUi().votesText)
        assertEquals("1.5M", testMovie(voteCount = 1_500_000).toUi().votesText)
        assertEquals("-", testMovie(voteCount = null).toUi().votesText)
    }

    @Test
    fun runtimeIsFormattedAsHoursAndMinutes() {
        assertEquals("2j 1m", testMovie(runtime = 121).toUi().runtimeText)
        assertEquals("0j 45m", testMovie(runtime = 45).toUi().runtimeText)
        assertEquals("-", testMovie(runtime = 0).toUi().runtimeText)
        assertEquals("-", testMovie(runtime = null).toUi().runtimeText)
    }

    @Test
    fun emptyFieldsFallBackToDash() {
        val ui = testMovie(
            releaseDate = "",
            genres = emptyList(),
            company = "",
            language = "",
            status = "",
            runtime = null
        ).toUi()

        assertEquals("-", ui.metaText)
        assertEquals("-", ui.subtitle)
        assertEquals("-", ui.status)
        assertEquals("-", ui.releaseText)
        assertEquals("-", ui.languageText)
    }

    @Test
    fun shortReleaseDateHasNoYear() {
        val ui = testMovie(releaseDate = "20", genres = listOf("Drama")).toUi()

        assertEquals("Drama", ui.metaText)
    }
}
