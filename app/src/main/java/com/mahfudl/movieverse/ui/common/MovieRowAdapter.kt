package com.mahfudl.movieverse.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mahfudl.movieverse.databinding.ItemMovieRowBinding
import com.mahfudl.movieverse.ui.model.MovieUi

/** Adapter bergaya baris (poster di kiri) untuk pencarian dan favorit. */
class MovieRowAdapter(private val onItemClick: (MovieUi) -> Unit) :
    ListAdapter<MovieUi, MovieRowAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemMovieRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MovieUi) = with(binding) {
            tvTitle.text = item.title
            tvMeta.text = item.metaText
            tvGenres.text = item.genres.take(3).joinToString(" • ")
            tvGenres.visible(item.genres.isNotEmpty())
            tvScore.text = item.scoreText
            tvScore.visible(item.scoreText != null)
            imgPoster.loadPoster(item.posterUrl)
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMovieRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<MovieUi>() {
            override fun areItemsTheSame(old: MovieUi, new: MovieUi) = old.movieId == new.movieId
            override fun areContentsTheSame(old: MovieUi, new: MovieUi) = old == new
        }
    }
}
