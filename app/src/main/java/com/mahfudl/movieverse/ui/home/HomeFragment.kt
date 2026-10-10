package com.mahfudl.movieverse.ui.home

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.databinding.FragmentHomeBinding
import com.mahfudl.movieverse.ui.common.MovieAdapter
import com.mahfudl.movieverse.ui.common.collectOnStarted
import com.mahfudl.movieverse.ui.common.gridSpanCount
import com.mahfudl.movieverse.ui.common.hideShimmer
import com.mahfudl.movieverse.ui.common.hideState
import com.mahfudl.movieverse.ui.common.openDetail
import com.mahfudl.movieverse.ui.common.showShimmer
import com.mahfudl.movieverse.ui.common.showState
import com.mahfudl.movieverse.ui.common.visible
import com.mahfudl.movieverse.ui.model.MovieUi
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private val adapter = MovieAdapter { openDetail(it.movieId) }
    private val heroAdapter = HeroAdapter { openDetail(it.movieId) }

    private var tabMediator: TabLayoutMediator? = null
    private var lastInteraction = 0L

    // Disimpan sebagai properti agar bisa dilepas di onDestroyView (mencegah memory leak).
    private val heroPageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) lastInteraction = SystemClock.elapsedRealtime()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupHero()
        binding.rvMovie.layoutManager = GridLayoutManager(requireContext(), gridSpanCount())
        binding.rvMovie.adapter = adapter

        binding.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            val category = when (ids.firstOrNull()) {
                R.id.chipNowPlaying -> MovieCategory.NOW_PLAYING
                R.id.chipUpcoming -> MovieCategory.UPCOMING
                R.id.chipTopRated -> MovieCategory.TOP_RATED
                else -> MovieCategory.POPULAR
            }
            viewModel.setCategory(category)
        }

        collectOnStarted {
            launch {
                viewModel.selectedCategory.collect { category ->
                    val chipId = when (category) {
                        MovieCategory.POPULAR -> R.id.chipPopular
                        MovieCategory.NOW_PLAYING -> R.id.chipNowPlaying
                        MovieCategory.UPCOMING -> R.id.chipUpcoming
                        MovieCategory.TOP_RATED -> R.id.chipTopRated
                    }
                    if (binding.chipGroup.checkedChipId != chipId) binding.chipGroup.check(chipId)
                }
            }
            launch { viewModel.state.collect(::render) }
            launch { autoScrollHero() }
        }
    }

    private fun setupHero() = with(binding) {
        val density = resources.displayMetrics.density
        heroPager.adapter = heroAdapter
        heroPager.offscreenPageLimit = 3
        (heroPager.getChildAt(0) as RecyclerView).apply {
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        heroPager.setPageTransformer(
            CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer((12 * density).toInt()))
                addTransformer { page, position ->
                    page.scaleY = 0.9f + (1 - abs(position)).coerceIn(0f, 1f) * 0.1f
                }
            }
        )
        heroPager.registerOnPageChangeCallback(heroPageCallback)
        tabMediator = TabLayoutMediator(heroDots, heroPager) { _, _ -> }.also { it.attach() }
    }

    /** Geser carousel otomatis, berhenti sementara saat pengguna menyentuhnya. */
    private suspend fun autoScrollHero() {
        while (true) {
            delay(4_500)
            val count = heroAdapter.itemCount
            if (count > 1 && SystemClock.elapsedRealtime() - lastInteraction > 6_000) {
                val pager = binding.heroPager
                pager.setCurrentItem((pager.currentItem + 1) % count, true)
            }
        }
    }

    private fun render(result: Resource<List<MovieUi>>) = with(binding) {
        when (result) {
            is Resource.Loading -> {
                shimmerView.showShimmer()
                rvMovie.visible(false)
                stateView.hideState()
            }
            is Resource.Success -> {
                shimmerView.hideShimmer()
                heroAdapter.submitList(result.data.take(5))
                heroContainer.visible(result.data.isNotEmpty())
                adapter.submitList(result.data) { _binding?.rvMovie?.scheduleLayoutAnimation() }
                if (result.data.isEmpty()) {
                    rvMovie.visible(false)
                    stateView.showState(
                        R.drawable.ic_movie,
                        getString(R.string.empty_title),
                        getString(R.string.empty_message)
                    )
                } else {
                    stateView.hideState()
                    rvMovie.visible(true)
                    rvMovie.scrollToPosition(0)
                }
            }
            is Resource.Error -> {
                shimmerView.hideShimmer()
                rvMovie.visible(false)
                heroContainer.visible(false)
                stateView.showState(
                    R.drawable.ic_error,
                    getString(R.string.error_title),
                    result.message,
                    getString(R.string.retry)
                ) { viewModel.retry() }
            }
        }
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        tabMediator = null
        binding.heroPager.unregisterOnPageChangeCallback(heroPageCallback)
        binding.heroPager.adapter = null
        binding.rvMovie.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
