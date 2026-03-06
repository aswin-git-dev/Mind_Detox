package com.example.mind_detox.ui.milestone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mind_detox.databinding.FragmentMilestoneBinding
import com.example.mind_detox.viewmodel.MainViewModel

class MilestoneFragment : Fragment() {

    private var _binding: FragmentMilestoneBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMilestoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe existing milestone from ViewModel
        viewModel.userMilestone.observe(viewLifecycleOwner) { milestone ->
            binding.tvSavedMilestone.text = milestone
        }

        binding.btnSaveMilestone.setOnClickListener {
            val newMilestone = binding.etMilestone.text.toString().trim()
            if (newMilestone.isNotEmpty()) {
                // Update milestone via ViewModel (which also saves to SharedPreferences)
                viewModel.setMilestone(newMilestone)
                
                binding.etMilestone.text?.clear()
                Toast.makeText(context, "Milestone saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a milestone", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
