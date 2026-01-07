package com.example.bank_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bank_app.R
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private lateinit var transactionsRecyclerView: RecyclerView
    private var isBalanceVisible = true
    private var currentBalance = 0.0
    private var currentCurrency = "DT"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load user account data
        loadAccountData()

        // Load card data (for displaying card number)
        loadCardData()

        // Load transactions
        loadTransactions()

        // Setup eye icon to toggle balance visibility
        view.findViewById<View>(R.id.eyeIcon)?.setOnClickListener {
            toggleBalanceVisibility(view)
        }

        // Setup quick action buttons
        setupQuickActions(view)
    }

    private fun loadAccountData() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getAccounts("Bearer $token")

                if (response.isSuccessful) {
                    val accounts = response.body()
                    if (accounts != null && accounts.isNotEmpty()) {
                        val account = accounts[0] // Get first account
                        currentBalance = account.balance
                        currentCurrency = account.currency
                        updateBalanceUI()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load accounts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCardData() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getCards("Bearer $token")

                if (response.isSuccessful) {
                    val cards = response.body()
                    if (cards != null && cards.isNotEmpty()) {
                        // Display first card's last 4 digits
                        val firstCard = cards[0]
                        updateCardNumberUI(firstCard.cardLast4)
                    } else {
                        // No cards - show default
                        updateCardNumberUI(null)
                    }
                }
            } catch (e: Exception) {
                // Silent fail for card display - not critical
                updateCardNumberUI(null)
            }
        }
    }

    private fun updateBalanceUI() {
        view?.apply {
            val balanceAmountView = findViewById<TextView>(R.id.balanceAmount)
            val formattedBalance = String.format("%.2f", currentBalance)

            if (isBalanceVisible) {
                balanceAmountView?.text = "$currentCurrency $formattedBalance"
            } else {
                balanceAmountView?.text = "$currentCurrency ••••"
            }
        }
    }

    private fun updateCardNumberUI(cardLast4: String?) {
        view?.apply {
            val cardNumberView = findViewById<TextView>(R.id.cardNumber)
            if (cardLast4 != null) {
                cardNumberView?.text = "•••• •••• •••• $cardLast4"
            } else {
                // No card - show message or hide
                cardNumberView?.text = "No card linked"
                cardNumberView?.alpha = 0.6f
            }
        }
    }

    private fun toggleBalanceVisibility(view: View) {
        isBalanceVisible = !isBalanceVisible
        val eyeIcon = view.findViewById<android.widget.ImageView>(R.id.eyeIcon)

        // Update balance display
        updateBalanceUI()

        // Change eye icon appearance
        if (isBalanceVisible) {
            eyeIcon?.alpha = 1f
            // Optionally change icon to "eye open"
            eyeIcon?.setImageResource(R.drawable.ic_eye)
        } else {
            eyeIcon?.alpha = 0.5f
            // Optionally change icon to "eye closed"
            // eyeIcon?.setImageResource(R.drawable.ic_eye_off)
        }
    }

    private fun loadTransactions() {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getTransactions("Bearer $token")

                if (response.isSuccessful) {
                    val transactions = response.body()
                    if (transactions != null) {
                        val adapter = TransactionAdapter(transactions)
                        transactionsRecyclerView.adapter = adapter
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load transactions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading transactions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupQuickActions(view: View) {
        // Request Money
        view.findViewById<View>(R.id.requestAction)?.setOnClickListener {
            findNavController().navigate(R.id.requestMoneyFragment)
        }

        // Send Money
        view.findViewById<View>(R.id.sendAction)?.setOnClickListener {
            findNavController().navigate(R.id.sendMoneyFragment)
        }

        // Income Prediction
        view.findViewById<View>(R.id.predictAction)?.setOnClickListener {
            findNavController().navigate(R.id.incomePredictionFragment)
        }

        // More - Navigate to Cards
        view.findViewById<View>(R.id.moreAction)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_cards)
        }
    }

    // Refresh data when fragment is visible
    override fun onResume() {
        super.onResume()
        loadAccountData()
        loadCardData()
        loadTransactions()
    }
}