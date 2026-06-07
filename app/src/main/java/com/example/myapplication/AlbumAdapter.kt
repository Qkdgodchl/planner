package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val albums: List<String>,
    private val onAddClick: () -> Unit,
    private val onAlbumClick: (String) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun getItemCount(): Int = albums.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val albumName = albums[position]

        if (albumName.isEmpty()) {
            // ⬜ 빈 앨범 (공백 네모칸) 상태
            holder.binding.tvAlbumTitle.text = "+ 새 앨범"
            holder.binding.ivAlbumCover.setImageResource(0) // 배경색만 보이게
            holder.binding.ivAlbumCover.setBackgroundColor(0xFFE0E0E0.toInt()) 
            
            holder.itemView.setOnClickListener { onAddClick() }
        } else {
            // 🖼️ 만들어진 앨범 상태
            holder.binding.tvAlbumTitle.text = albumName
            holder.binding.ivAlbumCover.setBackgroundColor(0xFF374151.toInt()) // 임시 구분 색상

            holder.itemView.setOnClickListener { onAlbumClick(albumName) }
        }
    }

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)
}