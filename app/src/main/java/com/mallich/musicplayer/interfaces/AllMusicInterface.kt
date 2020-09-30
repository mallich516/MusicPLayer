package com.mallich.musicplayer.interfaces

import android.content.Context
import android.view.View
import com.mallich.musicplayer.models.SongDataModel

interface AllMusicInterface {

    fun sendSelectedSongToPlay(context: Context, position: Int)

    fun optionClicked(context: Context, songData: SongDataModel, view: View)

}