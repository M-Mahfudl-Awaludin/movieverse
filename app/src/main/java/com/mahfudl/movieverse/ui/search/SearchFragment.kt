package com.mahfudl.movieverse.ui.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.databinding.FragmentSearchBinding
import com.mahfudl.movieverse.ui.common.MovieRowAdapter
import com.mahfudl.movieverse.ui.common.collectOnStarted
import com.mahfudl.movieverse.ui.common.hideShimmer
import com.mahfudl.movieverse.ui.common.hideState
import com.mahfudl.movieverse.ui.common.openDetail
import com.mahfudl.movieverse.ui.common.showShimmer
import com.mahfudl.movieverse.ui.common.showState
import com.mahfudl.movieverse.ui.common.visible
import com.mahfudl.movieverse.ui.model.MovieUi
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private val adapter = MovieRowAdapter { openDetail(it.movieId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        binding.rvResult.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResult.adapter = adapter

        binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

        collectOnStarted { viewModel.state.collect(::render) }
    }

    private fun render(result: Resource<List<MovieUi>>?) = with(binding) {
        when (result) {
            null -> {
                shimmerView.hideShimmer()
                rvResult.visible(false)
                adapter.submitList(emptyList())
                stateView.showState(
                    R.drawable.ic_search,
                    getString(R.string.search_idle_title),
                    getString(R.string.search_idle_message)
                )
            }
            is Resource.Loading -> {
                shimmerView.showShimmer()
                rvResult.visible(false)
                stateView.hideState()
            }
            is Resource.Success -> {
                shimmerView.hideShimmer()
                adapter.submitList(result.data) { _binding?.rvResult?.scheduleLayoutAnimation() }
                if (result.data.isEmpty()) {
                    rvResult.visible(false)
                    stateView.showState(
                        R.drawable.ic_movie,
                        getString(R.string.empty_title),
                        getString(R.string.empty_message)
                    )
                } else {
                    stateView.hideState()
                    rvResult.visible(true)
                    rvResult.scrollToPosition(0)
                }
            }
            is Resource.Error -> {
                shimmerView.hideShimmer()
                rvResult.visible(false)
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
        binding.rvResult.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
