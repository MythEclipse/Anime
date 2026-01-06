package com.mytheclipse.anime.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mytheclipse.anime.R
import com.mytheclipse.anime.databinding.ActivityAboutBinding
import com.mytheclipse.anime.utils.SessionManager

class AboutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAboutBinding
    private lateinit var sessionManager: SessionManager
    
    companion object {
        const val EXTRA_USERNAME = "extra_username"
        private const val PROFILE_IMAGE_URL = "https://asepharyana.tech/profil.avif"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupClickListeners()
        loadUserInfo()
        loadProfileImage()
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun loadUserInfo() {
        val user = sessionManager.getUser()
        
        if (user != null) {
            binding.tvName.text = user.name ?: user.email ?: "Asep Haryana Saputra"
            binding.tvEmail.text = user.email ?: "superaseph@gmail.com"
            binding.tvNim.text = "NIM: 20230810043"
        } else {
            // Use default values or username from intent
            val username = intent.getStringExtra(EXTRA_USERNAME)
            binding.tvName.text = username ?: "Asep Haryana Saputra"
            binding.tvEmail.text = "asep@email.com"
            binding.tvNim.text = "NIM: 20230810043"
        }
    }
    
    private fun loadProfileImage() {
        Glide.with(this)
            .load(PROFILE_IMAGE_URL)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .circleCrop()
            .into(binding.ivProfile)
    }
}

