package com.mbs.musicon.database

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mbs.musicon.datas.Songs
import com.mbs.musicon.utility.Constraints.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class SongsDB {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getSongs(): List<Songs> {
        return try {
            songCollection.get().await().toObjects(Songs::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

}