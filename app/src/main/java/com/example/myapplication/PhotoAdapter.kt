package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.data.Photo
import com.example.myapplication.databinding.ItemPhotoBinding

class PhotoAdapter(
    private val photos: List<Photo>,
    private val onDeleteClick: (Photo) -> Unit,
    private val onSetCoverClick: (Photo) -> Unit
) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        Glide.with(holder.binding.ivPhoto.context)
            .load(photo.uri)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivPhoto)

        holder.binding.btnDeletePhoto.setOnClickListener {
            onDeleteClick(photo)
        }

        holder.binding.btnSetCover.setOnClickListener {
            onSetCoverClick(photo)
        }
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)
}
