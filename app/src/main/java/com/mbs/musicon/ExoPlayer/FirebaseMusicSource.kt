package com.mbs.musicon.ExoPlayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.mbs.musicon.database.SongsDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
        private val musicDatabase: SongsDB
) {

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING
        val allSongs = musicDatabase.getSongs()
        songs = allSongs.map{ songs ->
            MediaMetadataCompat.Builder()
                    .putString(METADATA_KEY_ARTIST, songs.subtitle)
                    .putString(METADATA_KEY_MEDIA_ID, songs.mediaid)
                    .putString(METADATA_KEY_TITLE, songs.title)
                    .putString(METADATA_KEY_DISPLAY_TITLE, songs.title)
                    .putString(METADATA_KEY_DISPLAY_ICON_URI, songs.imageUrl)
                    .putString(METADATA_KEY_MEDIA_URI, songs.songUrl)
                    .putString(METADATA_KEY_ALBUM_ART_URI, songs.imageUrl)
                    .putString(METADATA_KEY_DISPLAY_SUBTITLE, songs.subtitle)
                    .putString(METADATA_KEY_DISPLAY_DESCRIPTION, songs.subtitle)
                    .build()
        }
        state = State.STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach{ song->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)

        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
                .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
                .setTitle(song.description.title)
                .setSubtitle(song.description.subtitle)
                .setMediaId(song.description.mediaId)
                .setIconUri(song.description.iconUri)
                .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state = State.STATE_CREATED
        set(value) {
            if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach {
                        listner -> listner(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListener += action
            return false
        } else {
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }

}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}

