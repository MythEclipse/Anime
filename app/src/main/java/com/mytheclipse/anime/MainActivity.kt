package com.mytheclipse.anime

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.data.model.CompleteAnimeItem
import com.mytheclipse.anime.data.model.OngoingAnimeItem
import com.mytheclipse.anime.databinding.ActivityMainBinding
import com.mytheclipse.anime.ui.adapter.CompleteAnimeAdapter
import com.mytheclipse.anime.ui.adapter.OngoingAnimeAdapter
import com.mytheclipse.anime.ui.anime.AnimeDetailActivity
import com.mytheclipse.anime.ui.anime.SearchActivity
import com.mytheclipse.anime.ui.auth.LoginActivity
import com.mytheclipse.anime.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    
    private lateinit var ongoingAdapter: OngoingAnimeAdapter
    private lateinit var completeAdapter: CompleteAnimeAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupRecyclerViews()
        setupSwipeRefresh()
        
        loadAnimeData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "🎬 Anime App"
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_profile -> {
                showProfileInfo()
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, com.mytheclipse.anime.ui.AboutActivity::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupRecyclerViews() {
        // Ongoing Anime Adapter
        ongoingAdapter = OngoingAnimeAdapter { anime ->
            navigateToDetail(anime.slug)
        }
        binding.rvOngoingAnime.adapter = ongoingAdapter
        
        // Complete Anime Adapter
        completeAdapter = CompleteAnimeAdapter { anime ->
            navigateToDetail(anime.slug)
        }
        binding.rvCompleteAnime.adapter = completeAdapter
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            loadAnimeData()
        }
    }
    
    private fun loadAnimeData() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@MainActivity)
                val response = apiService.getAnimeHome()
                
                if (response.isSuccessful && response.body() != null) {
                    val animeData = response.body()!!.data
                    
                    ongoingAdapter.submitList(animeData.ongoingAnime)
                    completeAdapter.submitList(animeData.completeAnime)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load anime: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.swipeRefresh.isRefreshing = false
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun navigateToDetail(slug: String) {
        val intent = Intent(this, AnimeDetailActivity::class.java)
        intent.putExtra(AnimeDetailActivity.EXTRA_SLUG, slug)
        startActivity(intent)
    }
    
    private fun showProfileInfo() {
        val user = sessionManager.getUser()
        val message = if (user != null) {
            """
            |Name: ${user.name ?: "N/A"}
            |Email: ${user.email ?: "N/A"}
            |Role: ${user.role}
            |Email Verified: ${if (user.emailVerified) "Yes" else "No"}
            """.trimMargin()
        } else {
            "User info not available"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        sessionManager.clearSession()
        RetrofitClient.resetClient()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}