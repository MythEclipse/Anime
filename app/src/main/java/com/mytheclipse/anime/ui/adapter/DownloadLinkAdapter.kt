package com.mytheclipse.anime.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.anime.data.model.DownloadLink
import com.mytheclipse.anime.databinding.ItemDownloadLinkBinding

data class DownloadItem(
    val resolution: String,
    val link: DownloadLink
)

class DownloadLinkAdapter(
    private val onItemClick: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadLinkAdapter.ViewHolder>() {
    
    private var items: List<DownloadItem> = emptyList()
    
    fun submitList(downloadUrls: Map<String, List<DownloadLink>>) {
        val newItems = mutableListOf<DownloadItem>()
        downloadUrls.forEach { (resolution, links) ->
            links.forEach { link ->
                newItems.add(DownloadItem(resolution, link))
            }
        }
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadLinkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class ViewHolder(
        private val binding: ItemDownloadLinkBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
        
        fun bind(item: DownloadItem) {
            binding.tvResolution.text = item.resolution
            binding.tvServer.text = item.link.server
        }
    }
}
