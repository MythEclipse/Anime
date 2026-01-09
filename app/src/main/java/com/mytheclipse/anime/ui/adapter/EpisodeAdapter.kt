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

    private var clickedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == clickedPosition)
    }

    inner class ViewHolder(
        private val binding: ItemEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && clickedPosition == RecyclerView.NO_POSITION) {
                    clickedPosition = position
                    notifyItemChanged(position)
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: EpisodeList, isLoading: Boolean) {
            // Extract "Episode X"
            val regex = Regex("Episode\\s+\\d+", RegexOption.IGNORE_CASE)
            val match = regex.find(item.episode)
            binding.tvEpisode.text = match?.value ?: item.episode

            // Loading state
            if (isLoading) {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.tvEpisode.visibility = android.view.View.INVISIBLE
            } else {
                binding.progressBar.visibility = android.view.View.GONE
                binding.tvEpisode.visibility = android.view.View.VISIBLE
            }
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

    fun resetSelection() {
        val previous = clickedPosition
        clickedPosition = RecyclerView.NO_POSITION
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous)
        }
    }
}
