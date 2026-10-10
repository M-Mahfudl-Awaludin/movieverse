package com.mahfudl.movieverse.core.domain

import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.model.mapData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ResourceTest {

    @Test
    fun mapDataTransformsSuccessValue() {
        val result = Resource.Success(2).mapData { it * 10 }

        assertEquals(Resource.Success(20), result)
    }

    @Test
    fun mapDataKeepsErrorUntouched() {
        val error: Resource<Int> = Resource.Error("gagal")

        assertSame(error, error.mapData { it + 1 })
    }

    @Test
    fun mapDataKeepsLoading() {
        val loading: Resource<Int> = Resource.Loading

        assertSame(Resource.Loading, loading.mapData { it + 1 })
    }

    @Test
    fun mapDataDoesNotCallTransformForNonSuccess() {
        var calls = 0
        val states: List<Resource<Int>> = listOf(Resource.Loading, Resource.Error("x"))

        states.forEach { it.mapData { value -> calls++ + value } }

        assertEquals(0, calls)
    }
}
