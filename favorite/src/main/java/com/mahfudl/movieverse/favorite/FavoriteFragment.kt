package com.mahfudl.movieverse.favorite

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mahfudl.movieverse.databinding.LayoutStateBinding
import com.mahfudl.movieverse.favorite.databinding.FragmentFavoriteBinding
import com.mahfudl.movieverse.favorite.di.injectFavoriteFeature
import com.mahfudl.movieverse.ui.common.MovieRowAdapter
import com.mahfudl.movieverse.ui.common.collectOnStarted
import com.mahfudl.movieverse.ui.common.hideState
import com.mahfudl.movieverse.ui.common.openDetail
import com.mahfudl.movieverse.ui.common.showState
import com.mahfudl.movieverse.ui.common.visible
import com.mahfudl.movieverse.ui.model.MovieUi
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    // Layout "layout_state" berasal dari modul :app, sehingga View Binding di modul ini
    // hanya menghasilkan field bertipe View. Kita ikat manual ke LayoutStateBinding milik :app.
    private var _stateBinding: LayoutStateBinding? = null
    private val stateBinding get() = _stateBinding!!

    private val viewModel: FavoriteViewModel by viewModel()
    private val adapter = MovieRowAdapter { openDetail(it.movieId) }
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFavoriteFeature() // muat module Koin milik dynamic feature
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoriteBinding.bind(view)
        _stateBinding = LayoutStateBinding.bind(binding.stateView)

        binding.rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorite.adapter = adapter
        itemTouchHelper = ItemTouchHelper(swipeCallback()).also {
            it.attachToRecyclerView(binding.rvFavorite)
        }

        collectOnStarted { viewModel.favorites.collect(::render) }
    }

    private fun render(list: List<MovieUi>?) = with(binding) {
        when {
            list == null -> {
                rvFavorite.visible(false)
                tvFavCount.visible(false)
                stateBinding.hideState()
            }
            list.isEmpty() -> {
                adapter.submitList(emptyList())
                rvFavorite.visible(false)
                tvFavCount.visible(false)
                stateBinding.showState(
                    R.drawable.ic_empty_heart,
                    getString(R.string.favorite_empty_title),
                    getString(R.string.favorite_empty_message)
                )
            }
            else -> {
                stateBinding.hideState()
                tvFavCount.text = getString(R.string.favorite_count, list.size)
                tvFavCount.visible(true)
                rvFavorite.visible(true)
                adapter.submitList(list)
            }
        }
    }

    /** Geser kartu ke kiri/kanan untuk menghapus, dengan tombol Urungkan. */
    private fun swipeCallback() = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        private val density = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(requireContext(), R.color.swipe_bg)
        }
        private val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_swipe_delete)!!

        // Dipakai ulang di setiap frame agar tidak ada alokasi objek saat menggambar.
        private val background = RectF()

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = adapter.currentList.getOrNull(viewHolder.bindingAdapterPosition) ?: return
            viewModel.remove(item.movieId)
            Snackbar.make(binding.root, getString(R.string.favorite_removed_msg, item.title), Snackbar.LENGTH_LONG)
                .setAction(R.string.favorite_undo) { viewModel.undoRemove() }
                .show()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val v = viewHolder.itemView
            if (dX != 0f) {
                c.save()
                if (dX > 0) {
                    c.clipRect(v.left.toFloat(), v.top.toFloat(), v.left + dX, v.bottom.toFloat())
                } else {
                    c.clipRect(v.right + dX, v.top.toFloat(), v.right.toFloat(), v.bottom.toFloat())
                }

                val radius = 20 * density
                background.set(v.left.toFloat(), v.top.toFloat(), v.right.toFloat(), v.bottom.toFloat())
                c.drawRoundRect(background, radius, radius, paint)

                val size = (24 * density).toInt()
                val margin = (28 * density).toInt()
                val top = v.top + (v.height - size) / 2
                if (dX > 0) {
                    icon.setBounds(v.left + margin, top, v.left + margin + size, top + size)
                } else {
                    icon.setBounds(v.right - margin - size, top, v.right - margin, top + size)
                }
                icon.draw(c)
                c.restore()
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onDestroyView() {
        itemTouchHelper?.attachToRecyclerView(null)
        itemTouchHelper = null
        binding.rvFavorite.adapter = null
        _stateBinding = null
        _binding = null
        super.onDestroyView()
    }
}
