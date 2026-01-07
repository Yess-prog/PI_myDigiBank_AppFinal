package com.example.bank_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bank_app.api.RetrofitClient
import com.example.bank_app.databinding.ActivitySignupBinding
import com.example.bank_app.utils.PreferencesHelper
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            performSignup()
        }
    }

    private fun performSignup() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPass.text.toString().trim()
        val confirmPassword = binding.etConfirm.text.toString().trim()

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Creating account..."

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val registerRequest = com.example.bank_app.models.RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone.ifEmpty { null },
                    password = password
                )

                val response = apiService.register(registerRequest)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null && registerResponse.token.isNotEmpty()) {
                        // Save login data
                        PreferencesHelper.saveLoginData(
                            context = this@SignupActivity,
                            token = registerResponse.token,
                            userId = registerResponse.userId,
                            email = registerResponse.email,
                            name = "${registerResponse.firstName} ${registerResponse.lastName}"
                        )

                        Toast.makeText(
                            this@SignupActivity,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to MainActivity
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@SignupActivity,
                            registerResponse?.message ?: "Registration failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@SignupActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "S'inscrire"
            }
        }
    }
}