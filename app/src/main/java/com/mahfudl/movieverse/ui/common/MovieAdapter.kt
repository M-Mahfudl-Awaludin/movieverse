package com.mahfudl.movieverse.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mahfudl.movieverse.databinding.ItemMovieBinding
import com.mahfudl.movieverse.ui.model.MovieUi

class MovieAdapter(private val onItemClick: (MovieUi) -> Unit) : ListAdapter<MovieUi, MovieAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MovieUi) = with(binding) {
            tvTitle.text = item.title
            tvMeta.text = item.metaText
            tvScore.text = item.scoreText
            tvScore.visible(item.scoreText != null)
            imgPoster.loadPoster(item.posterUrl)
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<MovieUi>() {
            override fun areItemsTheSame(old: MovieUi, new: MovieUi) = old.movieId == new.movieId
            override fun areContentsTheSame(old: MovieUi, new: MovieUi) = old == new
        }
    }
}
