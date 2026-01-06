package com.mytheclipse.anime.ui.anime

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.databinding.ActivitySearchBinding
import com.mytheclipse.anime.ui.adapter.SearchResultAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: SearchResultAdapter
    
    private var searchJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSearchInput()
        setupAdapter()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupSearchInput() {
        // Search on keyboard action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
        
        // Search icon click
        binding.tilSearch.setEndIconOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }
    }
    
    private fun setupAdapter() {
        searchAdapter = SearchResultAdapter { anime ->
            navigateToDetail(anime.slug)
        }
        binding.rvSearchResults.adapter = searchAdapter
    }
    
    private fun performSearch(query: String) {
        // Cancel previous search
        searchJob?.cancel()
        
        searchJob = lifecycleScope.launch {
            setLoading(true)
            hideEmptyState()
            
            try {
                val apiService = RetrofitClient.getApiService(this@SearchActivity)
                val response = apiService.searchAnime(query)
                
                if (response.isSuccessful && response.body() != null) {
                    val results = response.body()!!.data
                    
                    if (results.isEmpty()) {
                        showEmptyState("No results found for \"$query\"")
                        searchAdapter.submitList(emptyList())
                    } else {
                        hideEmptyState()
                        searchAdapter.submitList(results)
                        binding.rvSearchResults.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@SearchActivity,
                        "Search failed: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@SearchActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.llEmptyState.visibility = View.GONE
        }
    }
    
    private fun showEmptyState(message: String) {
        binding.llEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
        binding.rvSearchResults.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.llEmptyState.visibility = View.GONE
        binding.rvSearchResults.visibility = View.VISIBLE
    }
    
    private fun navigateToDetail(slug: String) {
        val intent = Intent(this, AnimeDetailActivity::class.java)
        intent.putExtra(AnimeDetailActivity.EXTRA_SLUG, slug)
        startActivity(intent)
    }
}
