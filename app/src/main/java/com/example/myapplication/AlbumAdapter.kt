package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Album
import com.example.myapplication.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val albums: List<Album>,
    private val onAddClick: () -> Unit,
    private val onAlbumClick: (String) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun getItemCount(): Int = albums.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AlbumViewHolder,
        position: Int
    ) {

        if (position == albums.size) {

            holder.binding.tvAlbumTitle.text =
                "+ 새 앨범"

            holder.binding.ivAlbumCover
                .setImageResource(android.R.drawable.ic_input_add)

            holder.itemView.setOnClickListener {
                onAddClick()
            }

        } else {

            val album = albums[position]

            holder.binding.tvAlbumTitle.text =
                album.title

            holder.itemView.setOnClickListener {
                onAlbumClick(album.title)
            }
        }
    }

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)
}