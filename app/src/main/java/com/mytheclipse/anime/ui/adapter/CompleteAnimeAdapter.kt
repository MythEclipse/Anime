package com.mytheclipse.anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mytheclipse.anime.R
import com.mytheclipse.anime.data.model.CompleteAnimeItem
import com.mytheclipse.anime.databinding.ItemAnimeHorizontalBinding

class CompleteAnimeAdapter(
    private val onItemClick: (CompleteAnimeItem) -> Unit
) : ListAdapter<CompleteAnimeItem, CompleteAnimeAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnimeHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemAnimeHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(item: CompleteAnimeItem) {
            binding.tvTitle.text = item.title
            binding.tvEpisode.text = "${item.episodeCount} Episodes"
            
            // Load poster image with Glide
            Glide.with(binding.ivPoster.context)
                .load(item.poster)
                .placeholder(R.drawable.placeholder_anime)
                .error(R.drawable.placeholder_anime)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.ivPoster)
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<CompleteAnimeItem>() {
        override fun areItemsTheSame(oldItem: CompleteAnimeItem, newItem: CompleteAnimeItem): Boolean {
            return oldItem.slug == newItem.slug
        }
        
        override fun areContentsTheSame(oldItem: CompleteAnimeItem, newItem: CompleteAnimeItem): Boolean {
            return oldItem == newItem
        }
    }
}
