package com.mytheclipse.anime.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.anime.MainActivity
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.data.model.RegisterRequest
import com.mytheclipse.anime.databinding.ActivityRegisterBinding
import com.mytheclipse.anime.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnRegister.setOnClickListener {
            performRegister()
        }
        
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        
        // Validate input
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            return
        }
        binding.tilUsername.error = null
        
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return
        }
        binding.tilEmail.error = null
        
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return
        }
        binding.tilPassword.error = null
        
        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            return
        }
        binding.tilConfirmPassword.error = null
        
        // Show loading
        setLoading(true)
        hideError()
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@RegisterActivity)
                val request = RegisterRequest(
                    email = email,
                    username = username,
                    password = password,
                    fullName = fullName.ifEmpty { null },
                    passwordConfirmation = confirmPassword
                )
                
                val response = apiService.register(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    
                    if (registerResponse.success) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Navigate back to login
                        finish()
                    } else {
                        showError(registerResponse.message)
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        409 -> "Email or username already exists"
                        400 -> "Invalid registration data"
                        else -> "Registration failed: ${response.message()}"
                    }
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                showError("Network error: ${e.localizedMessage}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "" else "Register"
    }
    
    private fun showError(message: String) {
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }
    
    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}
