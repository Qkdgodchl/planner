package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemAlbumHeaderImageBinding

class HeaderImageAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<HeaderImageAdapter.HeaderViewHolder>() {

    inner class HeaderViewHolder(val binding: ItemAlbumHeaderImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemAlbumHeaderImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.binding.ivHeaderImage.context)
            .load(imageUrl)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivHeaderImage)
    }

    override fun getItemCount(): Int = imageUrls.size
}
