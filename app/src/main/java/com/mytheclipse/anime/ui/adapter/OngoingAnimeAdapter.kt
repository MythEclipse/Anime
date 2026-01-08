package com.mytheclipse.anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mytheclipse.anime.R
import com.mytheclipse.anime.data.model.OngoingAnimeItem
import android.view.View
import com.mytheclipse.anime.databinding.ItemAnimeGridBinding

class OngoingAnimeAdapter(
    private val onItemClick: (OngoingAnimeItem) -> Unit
) : ListAdapter<OngoingAnimeItem, OngoingAnimeAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnimeGridBinding.inflate(
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
        private val binding: ItemAnimeGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(item: OngoingAnimeItem) {
            binding.tvTitle.text = item.title
            binding.tvEpisode.text = "Ep ${item.currentEpisode}"
            binding.tvStatus.text = "Ongoing"
            binding.tvRating.visibility = View.GONE
            
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
    
    private class DiffCallback : DiffUtil.ItemCallback<OngoingAnimeItem>() {
        override fun areItemsTheSame(oldItem: OngoingAnimeItem, newItem: OngoingAnimeItem): Boolean {
            return oldItem.slug == newItem.slug
        }
        
        override fun areContentsTheSame(oldItem: OngoingAnimeItem, newItem: OngoingAnimeItem): Boolean {
            return oldItem == newItem
        }
    }
}
