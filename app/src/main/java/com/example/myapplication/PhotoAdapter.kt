package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemPhotoBinding

data class PhotoItem(
    val uri: String,
    val category: String // "관광지", "음식", "자연경관"
)

class PhotoAdapter(private val photos: List<PhotoItem>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        // Glide를 사용하여 이미지 로드 (지금은 샘플 URI나 리소스를 사용하게 됩니다)
        Glide.with(holder.binding.ivPhoto.context)
            .load(photo.uri)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivPhoto)
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)
}