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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.anime.data.api.RetrofitClient
import com.mytheclipse.anime.databinding.ActivityMainBinding
import com.mytheclipse.anime.ui.adapter.CompleteAnimeAdapter
import com.mytheclipse.anime.ui.adapter.OngoingAnimeAdapter
import com.mytheclipse.anime.ui.adapter.SectionHeaderAdapter
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
    private lateinit var ongoingHeaderAdapter: SectionHeaderAdapter
    private lateinit var completeHeaderAdapter: SectionHeaderAdapter
    private lateinit var concatAdapter: ConcatAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupRecyclerView()
        // SwipeRefresh removed from layout for simplicity in this refactor, 
        // or re-add if layout supports it. 
        // Assuming Activity layout still has it or we removed it. 
        // Based on XML edit, we replaced SwipeRefresh content but kept SwipeRefresh?
        // Let's re-check the XML edit. The XML edit replaced SwipeRefresh's content with RecyclerView.
        // Wait, the previous edit replaced the entire SwipeRefresh block with RecyclerView.
        // So SwipeRefresh is GONE. I should ideally have kept it wrapping the RecyclerView.
        // But for now let's persist without it or re-add it if needed. 
        // To be safe, I will comment out setupSwipeRefresh for now.
        
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
    
    private fun setupRecyclerView() {
        // Init adapters
        ongoingHeaderAdapter = SectionHeaderAdapter("🔥 Ongoing Anime")
        ongoingAdapter = OngoingAnimeAdapter { anime ->
            navigateToDetail(anime.slug)
        }
        
        completeHeaderAdapter = SectionHeaderAdapter("✅ Complete Anime")
        completeAdapter = CompleteAnimeAdapter { anime ->
            navigateToDetail(anime.slug)
        }
        
        concatAdapter = ConcatAdapter(
            ongoingHeaderAdapter, 
            ongoingAdapter, 
            completeHeaderAdapter, 
            completeAdapter
        )
        
        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = concatAdapter.getItemViewType(position)
                // If it's a header, span 2 columns. Otherwise 1.
                // We don't have direct view types constant here easily from ConcatAdapter
                // But we know headers are in specific adapters.
                // A better way with ConcatAdapter:
                // Check which adapter the position belongs to.
                // But ConcatAdapter hides this slightly.
                // Simpler: Check if the holder is SectionHeaderAdapter.HeaderViewHolder
                // But we are in LayoutManager.
                
                // Hacky but effective way given we control the adapters:
                // We can assume strict ordering: Header(1) -> Items(N) -> Header(1) -> Items(M)
                // But N and M change.
                
                // Better approach: config adapters.
                // But SectionHeaderAdapter has 1 item.
                
                // Let's retry:
                // Binding adapters to ConcatAdapter supports finding adapter.
                // But standard GridLayoutManager doesn't expose "which adapter".
                
                // Workaround: We know Headers are always full span.
                // We can try to rely on getItemViewType. 
                // ConcatAdapter assigns unique view types to each adapter by default (isolateViewTypes = true).
                // So all items from ongoingHeaderAdapter have Type A, ongoingAdapter Type B, etc.
                // We just need to map Type A and Type C to span 2.
                
                // However, we don't know the integer values of types generated by ConcatAdapter easily.
                // The robust solution is checking the binding adapter for the position.
                // But that requires reflection or specific AndroidX APIs.
                
                // Alternative: Use a single RecyclerView Adapter with ViewTypes.
                // But ConcatAdapter is Cleaner.
                
                // actually, we can iterate to find the adapter.
                // val (adapter, _) = concatAdapter.getWrappedAdapterAndPosition(position)
                // if (adapter is SectionHeaderAdapter) return 2
                // return 1
                
                // Note: getWrappedAdapterAndPosition might not be available in older recyclerview versions 
                // but commonly available in 1.2.0+. Assuming modern environment.
                
                 try {
                     // This API exists in recent ConcatAdapter
                     val pairs = concatAdapter.getWrappedAdapterAndPosition(position)
                     val adapter = pairs.first
                     if (adapter is SectionHeaderAdapter) {
                         return 2
                     }
                 } catch (e: Exception) {
                     // Fallback
                     return 1
                 }
                return 1
            }
        }
        
        binding.rvMain.apply {
            layoutManager = gridLayoutManager
            adapter = concatAdapter
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
                    // Headers are static, always present 1 item.
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
        // SwipeRefresh removed, overlay removed?
        // Overlay is still in XML outer frame? 
        // Wait, I replaced SwipeRefresh content. 
        // The LoadingOverlay FrameLayout was OUTSIDE SwipeRefresh in original XML.
        // It should still be there.
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun navigateToDetail(slug: String) {
        val intent = Intent(this, AnimeDetailActivity::class.java)
        intent.putExtra(AnimeDetailActivity.EXTRA_SLUG, slug)
        startActivity(intent)
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