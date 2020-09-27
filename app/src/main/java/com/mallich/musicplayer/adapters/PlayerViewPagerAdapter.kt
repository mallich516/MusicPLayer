package com.mallich.musicplayer.adapters

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ReportFragment
import com.bumptech.glide.Glide
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.fragments.MusicPlayerFragment
import com.mallich.musicplayer.interfaces.ViewPagerInterface
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity
import java.lang.Exception

class PlayerViewPagerAdapter(
    val application: Application,
    fragmentManager: FragmentManager,
    private val playerInterface: ViewPagerInterface,
    private val list: MutableList<SongDataModel>
) :
    FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun getItem(position: Int): Fragment {
        val musicPlayerFragment = MusicPlayerFragment()
//        val bundle: Bundle = Bundle()
//        bundle.putString("image", list[position].albumArt)
//        bundle.putString("album", list[position].album)
//        bundle.putInt("position", position)
//        musicPlayerFragment.arguments = bundle
//        playerInterface.playSong(list[position], position)
        return MusicPlayerFragment()
    }

}