package com.mytheclipse.anime.ui.anime

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.databinding.ActivityAnimePlayerBinding
import com.mytheclipse.anime.ui.adapter.DownloadLinkAdapter
import kotlinx.coroutines.launch

class AnimePlayerActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_SLUG = "extra_slug"
    }
    
    private lateinit var binding: ActivityAnimePlayerBinding
    private lateinit var downloadAdapter: DownloadLinkAdapter
    
    private var currentSlug: String? = null
    private var nextEpisodeSlug: String? = null
    private var previousEpisodeSlug: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentSlug = intent.getStringExtra(EXTRA_SLUG)
        
        if (currentSlug.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid episode", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupWebView()
        setupDownloadAdapter()
        setupNavigationButtons()
        loadEpisodeData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        binding.webViewPlayer.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(WebAppInterface(), "Android")
            
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                private var customView: View? = null
                private var customViewCallback: CustomViewCallback? = null
                private var originalSystemUiVisibility: Int = 0

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) {
                        onHideCustomView()
                        return
                    }

                    customView = view
                    customViewCallback = callback
                    originalSystemUiVisibility = window.decorView.systemUiVisibility

                    binding.fullscreenContainer.addView(
                        customView,
                        android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )
                    
                    binding.fullscreenContainer.visibility = View.VISIBLE
                    binding.webViewPlayer.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                    binding.llEpisodeInfo.visibility = View.GONE
                    binding.llNavigation.visibility = View.GONE
                    binding.tvDownloadLabel.visibility = View.GONE
                    binding.rvDownloadLinks.visibility = View.GONE

                    // Immersive mode
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                }

                override fun onHideCustomView() {
                    binding.fullscreenContainer.visibility = View.GONE
                    if (customView != null) {
                        binding.fullscreenContainer.removeView(customView)
                    }
                    customView = null
                    
                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null

                    binding.webViewPlayer.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                    binding.llEpisodeInfo.visibility = View.VISIBLE
                    binding.llNavigation.visibility = View.VISIBLE
                    
                    // Restore download section visibility if data exists
                    if (downloadAdapter.itemCount > 0) {
                        binding.tvDownloadLabel.visibility = View.VISIBLE
                        binding.rvDownloadLinks.visibility = View.VISIBLE
                    }


                    window.decorView.systemUiVisibility = originalSystemUiVisibility
                }
            }
        }
    }

    private inner class WebAppInterface {
        @android.webkit.JavascriptInterface
        fun resize(height: Float) {
            runOnUiThread {
                val params = binding.webViewPlayer.layoutParams
                params.height = (height * resources.displayMetrics.density).toInt()
                binding.webViewPlayer.layoutParams = params
            }
        }
    }
    
    private fun setupDownloadAdapter() {
        downloadAdapter = DownloadLinkAdapter { downloadItem ->
            // Open download link in browser
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadItem.link.url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvDownloadLinks.adapter = downloadAdapter
    }
    
    private fun setupNavigationButtons() {
        binding.btnPrevious.setOnClickListener {
            previousEpisodeSlug?.let { slug ->
                navigateToEpisode(slug)
            }
        }
        
        binding.btnNext.setOnClickListener {
            nextEpisodeSlug?.let { slug ->
                navigateToEpisode(slug)
            }
        }
    }
    
    private fun loadEpisodeData() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@AnimePlayerActivity)
                val response = apiService.getAnimeFull(currentSlug!!)
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    
                    // Update UI
                    binding.tvEpisodeTitle.text = "Episode ${data.episodeNumber}"
                    binding.tvAnimeTitle.text = data.episode
                    
                    // Load video player
                    if (data.streamUrl.isNotEmpty()) {
                        loadVideoPlayer(data.streamUrl)
                    }
                    
                    // Navigation buttons
                    nextEpisodeSlug = data.nextEpisode?.slug
                    previousEpisodeSlug = data.previousEpisode?.slug
                    
                    binding.btnNext.isEnabled = data.hasNextEpisode
                    binding.btnPrevious.isEnabled = data.hasPreviousEpisode
                    
                    binding.btnNext.alpha = if (data.hasNextEpisode) 1f else 0.5f
                    binding.btnPrevious.alpha = if (data.hasPreviousEpisode) 1f else 0.5f
                    
                    // Download links
                    downloadAdapter.submitList(data.downloadUrls)
                    
                    binding.tvDownloadLabel.visibility = 
                        if (data.downloadUrls.isNotEmpty()) View.VISIBLE else View.GONE
                    
                } else {
                    Toast.makeText(
                        this@AnimePlayerActivity,
                        "Failed to load episode: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AnimePlayerActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }
    
    private fun loadVideoPlayer(streamUrl: String) {
        // Create HTML with embedded iframe for video player
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { margin: 0; padding: 0; background: #000; overflow: hidden; }
                    iframe { 
                        display: block;
                        width: 100%; 
                        border: none; 
                    }
                </style>
            </head>
            <body>
                <iframe src="$streamUrl" allowfullscreen></iframe>
                <script type="text/javascript">
                    // Inform Android app about the content height
                    function resize() {
                        var height = document.body.scrollHeight;
                        Android.resize(height);
                    }
                    window.onload = resize;
                    window.onresize = resize;
                </script>
            </body>
            </html>
        """.trimIndent()
        
        binding.webViewPlayer.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun navigateToEpisode(slug: String) {
        val intent = Intent(this, AnimePlayerActivity::class.java)
        intent.putExtra(EXTRA_SLUG, slug)
        startActivity(intent)
        finish()
    }
    
    override fun onPause() {
        super.onPause()
        binding.webViewPlayer.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        binding.webViewPlayer.onResume()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webViewPlayer.webChromeClient != null) {
            val client = binding.webViewPlayer.webChromeClient
            // Check if we need to exit fullscreen (custom view is visible)
            if (binding.fullscreenContainer.visibility == View.VISIBLE) {
                client?.onHideCustomView()
                return
            }
        }
        super.onBackPressed()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.webViewPlayer.destroy()
    }
}
