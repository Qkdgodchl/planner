package com.example.myapplication // 본인 패키지명에 맞게 수정하세요

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val albums: List<String>,
    private val onAddClick: () -> Unit,
    private val onAlbumClick: (String) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    // 아이템 총 개수 (실제 보관된 앨범 개수 + 맨 끝에 '추가하기' 버튼 1개)
    override fun getItemCount(): Int = albums.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        // 마지막 아이템일 경우 "앨범 추가하기" 카드로 변신
        if (position == albums.size) {
            holder.binding.tvAlbumTitle.text = "+ 앨범 추가"
            holder.binding.ivAlbumCover.setImageResource(android.R.drawable.ic_input_add) // 임시 더하기 아이콘
            holder.itemView.setOnClickListener { onAddClick() }
        } else {
            // 일반 여행 앨범일 경우
            val albumName = albums[position]
            holder.binding.tvAlbumTitle.text = albumName

            // TODO: 평가기준 20점! 나중에 이 부분에 Glide를 이용해 외부 사진 띄우기
            holder.itemView.setOnClickListener { onAlbumClick(albumName) }
        }
    }

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)
}