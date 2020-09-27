package com.mallich.musicplayer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.asLiveData
import com.mallich.musicplayer.data.MusicDataStore
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicReceiver : BroadcastReceiver(), AllMusicInterface {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action.toString()) {
            MusicRepository.PREV_BTN -> {
                MusicRepository.checkIfMediaPlayerIsNull(context)
                MusicRepository.decrementSongPosition()
                MusicRepository.playMusic(context)
            }
            MusicRepository.PLAY_BTN -> {
                MusicRepository.checkIfMediaPlayerIsNull(context)
                val dataStore = MusicDataStore(context)
                val songStatus = dataStore.getSongStatus().asLiveData()

//                MusicRepository.SONG_STATUS = songStatus.value.toString()

                if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
                    MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
                    MusicPlayerActivity.mediaPlayer!!.start()
                } else {
                    MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
                    MusicPlayerActivity.mediaPlayer!!.pause()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    dataStore.updateSongStatus(MusicRepository.SONG_STATUS)
                }
//                MusicRepository.updateSongStatusInDatabase(context)
            }
            MusicRepository.NEXT_PLAY -> {
                MusicRepository.checkIfMediaPlayerIsNull(context)
                MusicRepository.incrementSongPosition()
                MusicRepository.playMusic(context)
            }
        }
    }

    override fun sendSelectedSongToPlay(context: Context, position: Int) {
        TODO("Not yet implemented")
    }

}
