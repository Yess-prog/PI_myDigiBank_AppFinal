package com.example.bank_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bank_app.databinding.FragmentHomeBinding
import com.example.bank_app.ui.TransactionAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        val transactions = listOf(
            TransactionAdapter.Transaction("Restaurant", -45.5),
            TransactionAdapter.Transaction("Salaire", +500.0),
            TransactionAdapter.Transaction("Essence", -120.0)
        )

        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = TransactionAdapter(transactions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}