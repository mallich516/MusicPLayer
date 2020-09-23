package com.mallich.musicplayer.interfaces

import android.content.Context
import com.mallich.musicplayer.models.SongDataModel

interface MusicInterface {

    fun setMusic(context: Context, position: Int)

}