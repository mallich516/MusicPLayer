package com.mallich.musicplayer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mallich.musicplayer.data.MusicDataStore
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action.toString()) {
            MusicService.PREV_BTN -> {
                MusicPlayerActivity.songPosition -= 1
                if (MusicPlayerActivity.songPosition < 0) {
                    MusicPlayerActivity.songPosition = MusicPlayerActivity.playList.size - 1
                }
                updateSongInDataStore(context)
                MusicPlayerActivity.playNextSong(context)
                settingPlayerViews()
            }
            MusicService.PLAY_BTN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    updateSongStatusInDatabase(context)
                    settingPlayerViews()
                    // Start Notification Service
                    val intentService = Intent(context, MusicService::class.java)
                    intentService.putExtra(
                        "name", MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].name
                    )
                    intentService.putExtra(
                        "album",
                        MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].album
                    )
                    context.startForegroundService(intentService)
                }
            }
            MusicService.NEXT_PLAY -> {
                MusicPlayerActivity.songPosition += 1
                if (MusicPlayerActivity.songPosition >= MusicPlayerActivity.playList.size) {
                    MusicPlayerActivity.songPosition = 0
                }

                updateSongInDataStore(context)
                MusicPlayerActivity.playNextSong(context)
                settingPlayerViews()
            }
        }
    }

    private suspend fun updateSongStatusInDatabase(context: Context) {
        val musicDataStore = MusicDataStore(context)
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            MusicPlayerActivity.mediaPlayer!!.start()
        } else {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
            MusicPlayerActivity.mediaPlayer!!.pause()
        }
        musicDataStore.updateSongStatus(MusicRepository.SONG_STATUS)

    }

    companion object {
        fun updateSongInDataStore(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val musicDataStore = MusicDataStore(context)
                musicDataStore.updateCurrentSongDetails(
                    MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].name,
                    MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].album,
                    MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].artist,
                    MusicPlayerActivity.songPosition,
                    MusicPlayerActivity.albumType,
                    MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].albumArt
                )
            }
        }

        private fun settingPlayerViews() {
            if (MusicPlayerActivity.ACTIVE) {
                if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
                    MusicPlayerActivity.playBtn.setImageResource(R.drawable.play_icon)
                } else {
                    MusicPlayerActivity.playBtn.setImageResource(R.drawable.pause_icon)
                }
            } else if (MainActivity.ACTIVE) {
                if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
                    MainActivity.playBtn.setImageResource(R.drawable.play_icon)
                } else {
                    MainActivity.playBtn.setImageResource(R.drawable.pause_icon)
                }
            }
        }
    }
}
