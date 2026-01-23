package com.example.mind_detox.ui.apps

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mind_detox.databinding.FragmentAppSelectionBinding
import com.example.mind_detox.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionFragment : Fragment() {

    private var _binding: FragmentAppSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: AppAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppAdapter { packageName, appName, isBlocked ->
            viewModel.toggleAppBlock(packageName, appName, isBlocked)
        }

        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter

        loadApps()
    }

    private fun loadApps() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = requireContext().packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val appList = mutableListOf<AppItem>()
                
                for (app in packages) {
                    if (pm.getLaunchIntentForPackage(app.packageName) != null && app.packageName != requireContext().packageName) {
                        appList.add(
                            AppItem(
                                name = app.loadLabel(pm).toString(),
                                packageName = app.packageName,
                                icon = app.loadIcon(pm),
                                isBlocked = false
                            )
                        )
                    }
                }
                appList.sortBy { it.name }
                appList
            }

            viewModel.allBlockedApps.observe(viewLifecycleOwner) { blockedApps ->
                val updatedList = apps.map { app ->
                    app.copy(isBlocked = blockedApps.any { it.packageName == app.packageName })
                }
                adapter.submitList(updatedList)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    val isBlocked: Boolean
)
