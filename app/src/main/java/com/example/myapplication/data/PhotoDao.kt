package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insertPhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photos WHERE albumId = :albumId")
    suspend fun getPhotosByAlbumId(albumId: Int): List<Photo>

    @Query("SELECT * FROM photos WHERE albumId = :albumId ORDER BY id ASC LIMIT 1")
    suspend fun getFirstPhotoByAlbumId(albumId: Int): Photo?

    @Query("SELECT COUNT(*) FROM photos WHERE albumId = :albumId AND category = :category")
    suspend fun getPhotoCountByCategory(albumId: Int, category: String): Int
}
