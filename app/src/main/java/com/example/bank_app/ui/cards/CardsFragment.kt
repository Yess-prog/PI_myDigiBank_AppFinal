package com.example.bank_app.ui.cards

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bank_app.R
import com.example.bank_app.api.Card
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch

class CardsFragment : Fragment() {

    private var cardContainer: CardView? = null
    private var emptyStateView: View? = null
    private var addCardButton: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        cardContainer = view.findViewById(R.id.cardContainer)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        addCardButton = view.findViewById(R.id.addCardButton)

        // Setup add card button click listener
        addCardButton?.setOnClickListener {
            showAddCardDialog()
        }

        // Load cards from API
        loadCards()
    }

    private fun loadCards() {
        val token = PreferencesHelper.getToken(requireContext())

        Log.d("CardsFragment", "Loading cards with token: ${token?.take(20)}...")

        if (token.isNullOrEmpty()) {
            Log.e("CardsFragment", "Token is null or empty")
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            showEmptyState()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val response = apiService.getCards("Bearer $token")

                Log.d("CardsFragment", "Response code: ${response.code()}")
                Log.d("CardsFragment", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val cards = response.body()
                    Log.d("CardsFragment", "Cards received: ${cards?.size ?: 0}")

                    if (cards != null && cards.isNotEmpty()) {
                        Log.d("CardsFragment", "Displaying first card: ${cards[0]}")
                        // Display first card details
                        displayCardDetails(cards[0])
                        showCardView()
                    } else {
                        Log.d("CardsFragment", "No cards found - showing empty state")
                        // No cards found - show empty state
                        showEmptyState()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CardsFragment", "Failed to load cards: ${response.message()}, Error: $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "Failed to load cards: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
            } catch (e: Exception) {
                Log.e("CardsFragment", "Error loading cards", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading cards: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState()
            }
        }
    }

    private fun displayCardDetails(card: Card) {
        Log.d("CardsFragment", "Displaying card details: holder=${card.cardHolderName}, last4=${card.cardLast4}")

        view?.apply {
            // Set card type
            findViewById<TextView>(R.id.cardType)?.apply {
                text = card.cardType
                Log.d("CardsFragment", "Set card type: ${card.cardType}")
            }

            // Set card number (masked)
            findViewById<TextView>(R.id.cardNumber)?.apply {
                text = "**** **** **** ${card.cardLast4}"
                Log.d("CardsFragment", "Set card number: **** **** **** ${card.cardLast4}")
            }

            // Set card holder name
            findViewById<TextView>(R.id.cardHolder)?.apply {
                text = card.cardHolderName.uppercase()
                Log.d("CardsFragment", "Set card holder: ${card.cardHolderName.uppercase()}")
            }

            // Set expiry date
            val expiryText = String.format("%02d/%02d", card.expiryMonth, card.expiryYear % 100)
            findViewById<TextView>(R.id.expiryDate)?.apply {
                text = expiryText
                Log.d("CardsFragment", "Set expiry: $expiryText")
            }

            // Update card status color based on status
            val cardBackground = findViewById<View>(R.id.cardBackground)
            when (card.status.lowercase()) {
                "active" -> {
                    cardBackground?.alpha = 1.0f
                    Log.d("CardsFragment", "Card status: active")
                }
                "blocked", "suspended" -> {
                    cardBackground?.alpha = 0.5f
                    Log.d("CardsFragment", "Card status: ${card.status}")
                }
                "expired" -> {
                    cardBackground?.alpha = 0.3f
                    Log.d("CardsFragment", "Card status: expired")
                }
            }
        }
    }

    private fun showCardView() {
        Log.d("CardsFragment", "Showing card view")
        cardContainer?.visibility = View.VISIBLE
        emptyStateView?.visibility = View.GONE
    }

    private fun showEmptyState() {
        Log.d("CardsFragment", "Showing empty state")
        cardContainer?.visibility = View.GONE
        emptyStateView?.visibility = View.VISIBLE
    }

    private fun showAddCardDialog() {
        // Create dialog for adding a new card
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_card, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Setup dialog views
        val cardHolderInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.cardHolderInput)
        val cardNumberInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.cardNumberInput)
        val expiryMonthInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.expiryMonthInput)
        val expiryYearInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.expiryYearInput)
        val cvvInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.cvvInput)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<android.widget.Button>(R.id.btnAdd)

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnAdd?.setOnClickListener {
            val cardHolder = cardHolderInput?.text.toString().trim()
            val cardNumber = cardNumberInput?.text.toString().trim()
            val expiryMonth = expiryMonthInput?.text.toString().trim()
            val expiryYear = expiryYearInput?.text.toString().trim()
            val cvv = cvvInput?.text.toString().trim()

            // Validation
            if (cardHolder.isEmpty() || cardNumber.isEmpty() ||
                expiryMonth.isEmpty() || expiryYear.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length != 16) {
                Toast.makeText(requireContext(), "Card number must be 16 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expiryMonth.toIntOrNull() == null || expiryMonth.toInt() !in 1..12) {
                Toast.makeText(requireContext(), "Invalid expiry month (1-12)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvv.length != 3) {
                Toast.makeText(requireContext(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add card via API
            addCard(cardHolder, cardNumber, expiryMonth.toInt(), expiryYear.toInt(), cvv)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addCard(
        cardHolder: String,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String
    ) {
        val token = PreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("CardsFragment", "Adding card for: $cardHolder")

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance

                val addCardRequest = com.example.bank_app.api.AddCardRequest(
                    cardHolderName = cardHolder,
                    cardNumber = cardNumber,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    cvv = cvv
                )

                Log.d("CardsFragment", "Sending add card request...")
                val response = apiService.addCard("Bearer $token", addCardRequest)

                Log.d("CardsFragment", "Add card response: ${response.code()}")

                if (response.isSuccessful) {
                    val addCardResponse = response.body()
                    Log.d("CardsFragment", "Card added successfully: ${addCardResponse?.cardLast4}")

                    Toast.makeText(
                        requireContext(),
                        "Card added successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    // ✅ CRITICAL: Reload cards after adding
                    Log.d("CardsFragment", "Reloading cards after successful add...")
                    loadCards()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CardsFragment", "Failed to add card: $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "Failed to add card: $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CardsFragment", "Error adding card", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("CardsFragment", "onResume - reloading cards")
        loadCards()
    }
}