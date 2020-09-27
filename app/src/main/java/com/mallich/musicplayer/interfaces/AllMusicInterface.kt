package com.mallich.musicplayer.interfaces

import android.content.Context

interface AllMusicInterface {

    fun sendSelectedSongToPlay(context: Context, position: Int)

}