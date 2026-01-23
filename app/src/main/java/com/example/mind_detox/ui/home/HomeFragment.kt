package com.example.mind_detox.ui.home

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mind_detox.databinding.FragmentHomeBinding
import com.example.mind_detox.service.FocusService
import com.example.mind_detox.viewmodel.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFocusToggle.setOnClickListener {
            if (hasUsageStatsPermission()) {
                toggleFocusMode()
            } else {
                requestUsageStatsPermission()
            }
        }

        viewModel.totalFocusTime.observe(viewLifecycleOwner) { minutes ->
            binding.tvFocusTime.text = "${(minutes ?: 0) / 60}h ${(minutes ?: 0) % 60}m"
        }

        viewModel.allBlockedApps.observe(viewLifecycleOwner) { apps ->
            binding.tvAppsBlocked.text = apps.size.toString()
        }
    }

    private fun toggleFocusMode() {
        val intent = Intent(requireContext(), FocusService::class.java)
        if (isServiceRunning(FocusService::class.java)) {
            requireContext().stopService(intent)
            binding.tvToggleLabel.text = "START FOCUS"
            binding.tvStatus.text = "Mind is currently free"
            Toast.makeText(context, "Focus Mode Stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
            binding.tvToggleLabel.text = "STOP FOCUS"
            binding.tvStatus.text = "Mind Detox Active"
            Toast.makeText(context, "Focus Mode Started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), requireContext().packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), requireContext().packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        Toast.makeText(context, "Please enable Usage Access to monitor apps", Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
