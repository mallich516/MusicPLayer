package com.mallich.musicplayer.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val musicDataStore = MusicDataStore(application)

    val currentSongDetails = musicDataStore.readCurrentSongDetails().asLiveData()

    val songStatus = musicDataStore.getSongStatus().asLiveData()

    fun updateCurrentSong(
        title: String,
        album: String,
        artist: String,
        position: Int,
        type: String,
        albumArt: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDataStore.updateCurrentSongDetails(title, album, artist, position, type, albumArt)
        }
    }

    fun updateSongStatus(songStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            musicDataStore.updateSongStatus(songStatus)
        }
    }



}