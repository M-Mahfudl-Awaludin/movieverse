package com.mahfudl.movieverse.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mahfudl.movieverse.R
import com.mahfudl.movieverse.databinding.FragmentSettingsBinding
import com.mahfudl.movieverse.ui.common.collectOnStarted
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val selected: Boolean? = when (checkedId) {
                R.id.btnThemeLight -> false
                R.id.btnThemeDark -> true
                else -> null
            }
            if (selected != viewModel.darkMode.value) viewModel.setDarkMode(selected)
        }

        collectOnStarted {
            viewModel.darkMode.collect { saved ->
                val id = when (saved) {
                    true -> R.id.btnThemeDark
                    false -> R.id.btnThemeLight
                    null -> R.id.btnThemeSystem
                }
                if (binding.toggleTheme.checkedButtonId != id) binding.toggleTheme.check(id)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
