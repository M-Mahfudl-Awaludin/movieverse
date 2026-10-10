package com.mahfudl.movieverse.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.databinding.FragmentDetailBinding
import com.mahfudl.movieverse.ui.common.ARG_MOVIE_ID
import com.mahfudl.movieverse.ui.common.collectOnStarted
import com.mahfudl.movieverse.ui.common.hideState
import com.mahfudl.movieverse.ui.common.loadPoster
import com.mahfudl.movieverse.ui.common.showState
import com.mahfudl.movieverse.ui.common.visible
import com.mahfudl.movieverse.ui.model.MovieUi
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val movieId by lazy { requireArguments().getInt(ARG_MOVIE_ID) }
    private val viewModel: DetailViewModel by viewModel { parametersOf(movieId) }

    private var currentMovie: MovieUi? = null
    private var synopsisExpanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)

        setupToolbar()
        binding.fabFavorite.setOnClickListener { onFavoriteClicked() }
        binding.btnReadMore.setOnClickListener { toggleSynopsis() }

        collectOnStarted {
            launch { viewModel.state.collect(::render) }
            launch { viewModel.isFavorite.collect(::renderFavorite) }
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_share) {
                shareMovie()
                true
            } else {
                false
            }
        }
        // Ikon putih di atas gambar, mengikuti warna tema saat toolbar menutup
        appBar.addOnOffsetChangedListener { bar, offset ->
            val fraction = abs(offset).toFloat() / bar.totalScrollRange.coerceAtLeast(1)
            val color = if (fraction > 0.65f) {
                MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorOnSurface)
            } else {
                Color.WHITE
            }
            toolbar.setNavigationIconTint(color)
            toolbar.menu.findItem(R.id.action_share)?.icon?.mutate()?.setTint(color)
        }
    }

    private fun shareMovie() {
        val movie = currentMovie ?: return
        val text = "${movie.title}\nhttps://www.themoviedb.org/movie/${movie.movieId}"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_label)))
    }

    private fun toggleSynopsis() = with(binding) {
        synopsisExpanded = !synopsisExpanded
        tvSynopsis.maxLines = if (synopsisExpanded) Int.MAX_VALUE else COLLAPSED_LINES
        btnReadMore.setText(if (synopsisExpanded) R.string.read_less else R.string.read_more)
    }

    private fun onFavoriteClicked() {
        val willBeFavorite = !viewModel.isFavorite.value
        viewModel.toggleFavorite()
        Snackbar.make(
            binding.root,
            if (willBeFavorite) R.string.fav_added else R.string.fav_removed,
            Snackbar.LENGTH_SHORT
        ).setAnchorView(binding.fabFavorite).show()

        binding.fabFavorite.animate().scaleX(1.25f).scaleY(1.25f).setDuration(120).withEndAction {
            _binding?.fabFavorite?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(120)?.start()
        }.start()
    }

    private fun renderFavorite(isFavorite: Boolean) {
        binding.fabFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    private fun render(result: Resource<MovieUi>) = with(binding) {
        when (result) {
            is Resource.Loading -> {
                progress.visible(true)
                scrollContent.visible(false)
                stateView.hideState()
                fabFavorite.hide()
            }
            is Resource.Success -> {
                progress.visible(false)
                stateView.hideState()
                bindMovie(result.data)
                scrollContent.visible(true)
                fabFavorite.show()
            }
            is Resource.Error -> {
                progress.visible(false)
                scrollContent.visible(false)
                fabFavorite.hide()
                stateView.showState(
                    R.drawable.ic_error,
                    getString(R.string.error_title),
                    result.message,
                    getString(R.string.retry)
                ) { viewModel.retry() }
            }
        }
    }

    private fun bindMovie(movie: MovieUi) = with(binding) {
        currentMovie = movie
        imgBanner.loadPoster(movie.backdropUrl.ifBlank { movie.posterUrl })
        imgPoster.loadPoster(movie.posterUrl)
        collapsingToolbar.title = movie.title
        tvTitle.text = movie.title
        tvSubtitle.text = movie.subtitle
        tvScore.text = movie.scoreText ?: "-"
        tvVotes.text = movie.votesText
        tvRuntime.text = movie.runtimeText
        tvSynopsis.text = movie.overview.ifBlank { getString(R.string.no_synopsis) }
        tvSynopsis.maxLines = if (synopsisExpanded) Int.MAX_VALUE else COLLAPSED_LINES
        tvStatus.text = movie.status
        tvRelease.text = movie.releaseText
        tvLanguage.text = movie.languageText

        // Tampilkan "Selengkapnya" hanya jika teks benar-benar terpotong
        tvSynopsis.post {
            val b = _binding ?: return@post
            val layout = b.tvSynopsis.layout
            val truncated = layout != null &&
                layout.lineCount > 0 &&
                layout.getEllipsisCount(layout.lineCount - 1) > 0
            b.btnReadMore.visible(truncated || synopsisExpanded)
        }

        chipGenres.removeAllViews()
        movie.genres.forEach { genre ->
            chipGenres.addView(
                Chip(requireContext()).apply {
                    text = genre
                    isClickable = false
                    isCheckable = false
                    setEnsureMinTouchTargetSize(false)
                }
            )
        }
        chipGenres.visible(movie.genres.isNotEmpty())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val COLLAPSED_LINES = 5
    }
}
