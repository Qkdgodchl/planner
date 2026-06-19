package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlbumDao {

    @Insert
    suspend fun insertAlbum(album: Album)

    @Update
    suspend fun updateAlbum(album: Album)

    @Delete
    suspend fun deleteAlbum(album: Album)

    @Query("SELECT * FROM album WHERE id = :id")
    suspend fun getAlbumById(id: Int): Album?

    @Query(
        """
        SELECT *
        FROM album
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllAlbums(): List<Album>
}
