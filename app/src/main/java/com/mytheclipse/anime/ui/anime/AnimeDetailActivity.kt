package com.mytheclipse.anime.ui.anime

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mytheclipse.anime.R
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.data.model.EpisodeList
import com.mytheclipse.anime.data.model.Recommendation
import com.mytheclipse.anime.databinding.ActivityAnimeDetailBinding
import com.mytheclipse.anime.ui.adapter.EpisodeAdapter
import com.mytheclipse.anime.ui.adapter.RecommendationAdapter
import kotlinx.coroutines.launch

class AnimeDetailActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_SLUG = "extra_slug"
    }
    
    private lateinit var binding: ActivityAnimeDetailBinding
    
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter
    
    private var currentSlug: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentSlug = intent.getStringExtra(EXTRA_SLUG)
        
        if (currentSlug.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid anime", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupAdapters()
        loadAnimeDetail()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupAdapters() {
        episodeAdapter = EpisodeAdapter { episode ->
            navigateToPlayer(episode.slug)
        }
        binding.rvEpisodes.adapter = episodeAdapter
        
        recommendationAdapter = RecommendationAdapter { recommendation ->
            navigateToDetail(recommendation.slug)
        }
        binding.rvRecommendations.adapter = recommendationAdapter
    }
    
    private fun loadAnimeDetail() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@AnimeDetailActivity)
                val response = apiService.getAnimeDetail(currentSlug!!)
                
                if (response.isSuccessful && response.body() != null) {
                    val detail = response.body()!!.data
                    
                    // Populate UI
                    binding.tvTitle.text = detail.title
                    binding.tvAlternativeTitle.text = detail.alternativeTitle
                    binding.tvSynopsis.text = detail.synopsis
                    binding.tvStudio.text = "Studio: ${detail.studio}"
                    binding.tvReleaseDate.text = "Released: ${detail.releaseDate}"
                    
                    binding.tvStatus.text = detail.status ?: "N/A"
                    binding.tvType.text = detail.type ?: "N/A"
                    
                    // Genres
                    val genres = detail.genres?.joinToString(", ") { it.name } ?: "N/A"
                    binding.tvGenres.text = genres
                    
                    // Poster
                    Glide.with(this@AnimeDetailActivity)
                        .load(detail.poster)
                        .placeholder(R.drawable.placeholder_anime)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.ivPoster)
                    
                    // Episodes
                    episodeAdapter.submitList(detail.episodeLists)
                    
                    // Recommendations
                    recommendationAdapter.submitList(detail.recommendations)
                    
                    // Show/hide recommendations label
                    binding.tvRecommendationsLabel.visibility = 
                        if (detail.recommendations.isNotEmpty()) View.VISIBLE else View.GONE
                    
                } else {
                    Toast.makeText(
                        this@AnimeDetailActivity,
                        "Failed to load detail: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AnimeDetailActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun navigateToPlayer(slug: String) {
        val intent = Intent(this, AnimePlayerActivity::class.java)
        intent.putExtra(AnimePlayerActivity.EXTRA_SLUG, slug)
        startActivity(intent)
    }
    
    private fun navigateToDetail(slug: String) {
        val intent = Intent(this, AnimeDetailActivity::class.java)
        intent.putExtra(EXTRA_SLUG, slug)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::episodeAdapter.isInitialized) {
            episodeAdapter.resetSelection()
        }
        if (::recommendationAdapter.isInitialized) {
            recommendationAdapter.resetSelection()
        }
    }
}
