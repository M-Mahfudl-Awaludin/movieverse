package com.mahfudl.movieverse.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.databinding.ItemHeroBinding
import com.mahfudl.movieverse.ui.common.loadPoster
import com.mahfudl.movieverse.ui.common.visible
import com.mahfudl.movieverse.ui.model.MovieUi

/** Kartu besar untuk carousel "spotlight" di beranda. */
class HeroAdapter(private val onItemClick: (MovieUi) -> Unit) : ListAdapter<MovieUi, HeroAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemHeroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MovieUi, position: Int) = with(binding) {
            tvRank.text = root.context.getString(R.string.hero_rank, position + 1)
            tvTitle.text = item.title
            tvMeta.text = item.metaText
            tvScore.text = item.scoreText
            tvScore.visible(item.scoreText != null)
            imgHero.loadPoster(item.backdropUrl.ifBlank { item.posterUrl })
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemHeroBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), position)

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<MovieUi>() {
            override fun areItemsTheSame(old: MovieUi, new: MovieUi) = old.movieId == new.movieId
            override fun areContentsTheSame(old: MovieUi, new: MovieUi) = old == new
        }
    }
}
