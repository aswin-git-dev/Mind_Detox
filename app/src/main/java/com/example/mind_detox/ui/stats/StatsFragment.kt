package com.example.mind_detox.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mind_detox.databinding.FragmentStatsBinding
import com.example.mind_detox.viewmodel.MainViewModel

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // For now, just showing that it's connected. 
        // In a full implementation, we'd have a SessionAdapter here.
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        
        // viewModel.allFocusSessions.observe(viewLifecycleOwner) { sessions ->
        //     adapter.submitList(sessions)
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
