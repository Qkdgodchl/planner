package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlbumDao {

    @Insert
    suspend fun insertAlbum(
        album: Album
    )

    @Query(
        """
        SELECT *
        FROM album
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllAlbums():
            List<Album>
}