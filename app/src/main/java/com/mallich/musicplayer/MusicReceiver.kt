package com.mallich.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mallich.musicplayer.ui.MusicPlayerActivity

class MusicReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action.toString()) {
            MusicService.PREV_BTN -> {
                MusicPlayerActivity.songPosition -= 1
                if (MusicPlayerActivity.songPosition < 0) {
                    MusicPlayerActivity.songPosition = MusicPlayerActivity.playList.size - 1
                }
                MusicPlayerActivity.playNextSong(context)
            }
            MusicService.PLAY_BTN -> {
                if (MusicPlayerActivity.mediaPlayer!!.isPlaying) {
                    MusicPlayerActivity.mediaPlayer!!.pause()
                } else {
                    MusicPlayerActivity.mediaPlayer!!.start()
                }
                MusicPlayerActivity.setSongDetails(context, MusicPlayerActivity.songPosition)
            }
            MusicService.NEXT_PLAY -> {
                MusicPlayerActivity.songPosition += 1
                if (MusicPlayerActivity.songPosition >= MusicPlayerActivity.playList.size) {
                    MusicPlayerActivity.songPosition = 0
                }
                MusicPlayerActivity.playNextSong(context)
            }
        }
    }
}
