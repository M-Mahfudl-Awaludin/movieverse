package com.mahfudl.movieverse.ui.common

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.databinding.LayoutShimmerBinding
import com.mahfudl.movieverse.databinding.LayoutStateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val ARG_MOVIE_ID = "movieId"

fun View.visible(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun ImageView.loadPoster(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.bg_placeholder)
        .error(R.drawable.bg_placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .into(this)
}

/** Kumpulkan Flow hanya saat view STARTED (aman terhadap lifecycle). */
fun Fragment.collectOnStarted(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

/** Navigasi ke detail lewat deep link internal (bisa dipanggil dari dynamic feature). */
fun Fragment.openDetail(movieId: Int) {
    findNavController().navigate(Uri.parse("movieverse://detail/$movieId"))
}

fun Fragment.gridSpanCount(): Int = resources.getInteger(R.integer.span_count)

fun LayoutStateBinding.showState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    root.visibility = View.VISIBLE
    imgState.setImageResource(icon)
    tvStateTitle.text = title
    tvStateMessage.text = message
    btnRetry.visible(onRetry != null)
    if (retryLabel != null) btnRetry.text = retryLabel
    btnRetry.setOnClickListener { onRetry?.invoke() }
}

fun LayoutStateBinding.hideState() {
    root.visibility = View.GONE
}

fun LayoutShimmerBinding.showShimmer() {
    root.visibility = View.VISIBLE
    root.startShimmer()
}

fun LayoutShimmerBinding.hideShimmer() {
    root.stopShimmer()
    root.visibility = View.GONE
}
