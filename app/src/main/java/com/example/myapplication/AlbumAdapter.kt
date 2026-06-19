package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.data.Album
import com.example.myapplication.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val albums: List<Album>,
    private val onAddClick: () -> Unit,
    private val onAlbumClick: (Int, String) -> Unit,
    private val onDeleteClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun getItemCount(): Int = albums.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        if (position == albums.size) {
            holder.binding.tvAlbumTitle.text = "+ 새 앨범"
            holder.binding.ivAlbumCover.setImageResource(android.R.drawable.ic_input_add)
            holder.binding.ivAlbumCover.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            holder.binding.btnDeleteAlbum.visibility = View.GONE
            holder.binding.vTitleBackground.visibility = View.GONE
            
            holder.itemView.setOnClickListener {
                onAddClick()
            }
        } else {
            val album = albums[position]
            holder.binding.tvAlbumTitle.text = album.title
            holder.binding.btnDeleteAlbum.visibility = View.VISIBLE
            holder.binding.vTitleBackground.visibility = View.VISIBLE
            
            // 썸네일 이미지 설정
            if (album.coverImagePath.isNotEmpty()) {
                holder.binding.ivAlbumCover.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                Glide.with(holder.binding.ivAlbumCover.context)
                    .load(album.coverImagePath)
                    .centerCrop()
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivAlbumCover)
            } else {
                holder.binding.ivAlbumCover.setImageResource(android.R.drawable.ic_menu_gallery)
                holder.binding.ivAlbumCover.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            }

            holder.itemView.setOnClickListener {
                onAlbumClick(album.id, album.title)
            }

            holder.binding.btnDeleteAlbum.setOnClickListener {
                onDeleteClick(album)
            }
        }
    }

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)
}
