package com.mallich.musicplayer.data

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import com.mallich.musicplayer.repositories.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCE_NAME = "SongDetails"

class MusicDataStore(val context: Context) {

    private val dataStore = context.createDataStore(
        name = PREFERENCE_NAME
    )

    private object PreferenceKeys {
        val SONG_TITLE = preferencesKey<String>("TITLE")
        val SONG_ALBUM = preferencesKey<String>("ALBUM")
        val SONG_ARTIST = preferencesKey<String>("ARTIST")
        val SONG_POSITION = preferencesKey<Int>("POSITION")
        val SONG_TYPE = preferencesKey<String>("TYPE")
        val SONG_ALBUM_ART = preferencesKey<String>("ALBUM_ART")
        val SONG_STATUS = preferencesKey<String>("STATUS")
    }

    suspend fun updateCurrentSongDetails(
        title: String,
        album: String,
        artist: String,
        position: Int,
        type: String,
        albumArt: String
    ) {
        dataStore.edit { songDetails ->
            songDetails[PreferenceKeys.SONG_TITLE] = title
            songDetails[PreferenceKeys.SONG_ALBUM] = album
            songDetails[PreferenceKeys.SONG_ARTIST] = artist
            songDetails[PreferenceKeys.SONG_POSITION] = position
            songDetails[PreferenceKeys.SONG_TYPE] = type
            songDetails[PreferenceKeys.SONG_ALBUM_ART] = albumArt
        }
    }

    suspend fun updateSongStatus(status: String) {
        dataStore.edit { songStatus ->
            songStatus[PreferenceKeys.SONG_STATUS] = status
        }
    }

    fun readCurrentSongDetails(): Flow<MutableList<String>> = dataStore.data
        .map { songDetails ->
            val songDetailsList = mutableListOf<String>()

            // 0
            val title =
                songDetails[PreferenceKeys.SONG_TITLE] ?: MusicRepository.LETS_START_MUSIC
            songDetailsList.add(title)
            // 1
            val album = songDetails[PreferenceKeys.SONG_ALBUM] ?: ""
            songDetailsList.add(album)
            // 2
            val artist = songDetails[PreferenceKeys.SONG_ARTIST] ?: MusicRepository.PLAY_NOW
            songDetailsList.add(artist)
            // 3
            val position = songDetails[PreferenceKeys.SONG_POSITION] ?: 0
            songDetailsList.add(position.toString())
            // 4
            val type = songDetails[PreferenceKeys.SONG_TYPE] ?: MusicRepository.ALL_SONGS
            songDetailsList.add(type)
            // 5
            val albumArt = songDetails[PreferenceKeys.SONG_ALBUM_ART] ?: ""
            songDetailsList.add(albumArt)

            songDetailsList
        }

    fun getSongStatus(): Flow<String> = dataStore.data
        .map { songStatus ->
            val status =
                songStatus[PreferenceKeys.SONG_STATUS] ?: MusicRepository.SONG_STATUS
            status
        }
}
