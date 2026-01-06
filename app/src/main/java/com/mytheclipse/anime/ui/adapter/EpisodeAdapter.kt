package com.mytheclipse.anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.anime.data.model.EpisodeList
import com.mytheclipse.anime.databinding.ItemEpisodeBinding

class EpisodeAdapter(
    private val onItemClick: (EpisodeList) -> Unit
) : ListAdapter<EpisodeList, EpisodeAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(
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
        private val binding: ItemEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(item: EpisodeList) {
            binding.tvEpisode.text = item.episode
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<EpisodeList>() {
        override fun areItemsTheSame(oldItem: EpisodeList, newItem: EpisodeList): Boolean {
            return oldItem.slug == newItem.slug
        }
        
        override fun areContentsTheSame(oldItem: EpisodeList, newItem: EpisodeList): Boolean {
            return oldItem == newItem
        }
    }
}
