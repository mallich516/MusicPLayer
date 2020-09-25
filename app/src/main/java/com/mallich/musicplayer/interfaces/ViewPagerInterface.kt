package com.mallich.musicplayer.interfaces

import android.app.Application
import com.mallich.musicplayer.models.SongDataModel

interface ViewPagerInterface {

    fun playSong(songDataModel: SongDataModel, position: Int)

}