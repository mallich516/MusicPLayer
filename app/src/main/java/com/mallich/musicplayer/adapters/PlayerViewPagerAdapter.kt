package com.mallich.musicplayer.adapters

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.bumptech.glide.Glide
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.fragments.MusicPlayerFragment
import com.mallich.musicplayer.interfaces.PlayerInterface
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity
import java.lang.Exception

class PlayerViewPagerAdapter(
    val application: Application,
    fragmentManager: FragmentManager,
    private val playerInterface: PlayerInterface,
    private val list: MutableList<SongDataModel>
) :
    FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun getItem(position: Int): Fragment {
        val musicPlayerFragment = MusicPlayerFragment()
        val bundle: Bundle = Bundle()
        bundle.putString("image", list[position].albumArt)
        bundle.putString("album", list[position].album)
        bundle.putInt("position", position)
        musicPlayerFragment.arguments = bundle
        playerInterface.playSong(list[position], position)
//        Toast.makeText(application, "${list[position].album} $position", Toast.LENGTH_SHORT).show()

        MusicPlayerActivity.newAlbum = list[position].album
        MusicPlayerActivity.newSongPosition = position

        if (MusicPlayerActivity.newAlbumType == "SingleAlbum") {
            MusicPlayerActivity.playList.clear()
            MusicPlayerActivity.playList.addAll(MusicRepository.getSingleAlbum(application, MusicPlayerActivity.newAlbum!!))
        } else {
            MusicPlayerActivity.playList.clear()
            MusicPlayerActivity.playList.addAll(MusicRepository.getAllSongs(application))
        }


        if (MusicPlayerActivity.mediaPlayer == null) {
            MusicPlayerActivity.songPosition = MusicPlayerActivity.newSongPosition
            MusicPlayerActivity.albumType = MusicPlayerActivity.newAlbumType
            MusicPlayerActivity.album = MusicPlayerActivity.newAlbum
            playNewSong()
        } else if (MusicPlayerActivity.mediaPlayer != null) {
            if (MusicPlayerActivity.album != MusicPlayerActivity.newAlbum || MusicPlayerActivity.albumType != MusicPlayerActivity.newAlbumType || MusicPlayerActivity.newSongPosition != MusicPlayerActivity.songPosition) {
                MusicPlayerActivity.album = MusicPlayerActivity.newAlbum
                MusicPlayerActivity.albumType = MusicPlayerActivity.newAlbumType
                MusicPlayerActivity.songPosition = MusicPlayerActivity.newSongPosition
                playNextSong()
            } else {
                setSongDetails(MusicPlayerActivity.songPosition)
                initializeSeekBar()
                MusicPlayerActivity.playBtn.setImageResource(R.drawable.pause_icon)
            }
        }

        return musicPlayerFragment
    }

    private fun playNextSong() {
        setSongDetails(MusicPlayerActivity.songPosition)
        val selectedSong =
            Uri.parse(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].fileData.toString())
        MusicPlayerActivity.mediaPlayer?.stop()
        MusicPlayerActivity.mediaPlayer?.release()
        MusicPlayerActivity.mediaPlayer = MediaPlayer.create(application, selectedSong)
        MusicPlayerActivity.mediaPlayer?.start()
        initializeSeekBar()
        MusicPlayerActivity.playBtn.setImageResource(R.drawable.pause_icon)
    }

    private fun playNewSong() {
        setSongDetails(MusicPlayerActivity.songPosition)
        val selectedSong =
            Uri.parse(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].fileData.toString())
        MusicPlayerActivity.mediaPlayer?.stop()
        MusicPlayerActivity.mediaPlayer?.release()
        MusicPlayerActivity.mediaPlayer = MediaPlayer.create(application, selectedSong)
        MusicPlayerActivity.mediaPlayer?.start()
        initializeSeekBar()
        MusicPlayerActivity.playBtn.setImageResource(R.drawable.pause_icon)
    }

    private fun initializeSeekBar() {
        MusicPlayerActivity.seekBar.max = MusicPlayerActivity.mediaPlayer!!.duration
        MusicPlayerActivity.endTime.text =
            MusicRepository.getTimeString(MusicPlayerActivity.mediaPlayer!!.duration)
        MusicPlayerActivity.handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    MusicPlayerActivity.seekBar.progress =
                        MusicPlayerActivity.mediaPlayer!!.currentPosition
                    val currentTime =
                        MusicRepository.getTimeString(MusicPlayerActivity.mediaPlayer!!.currentPosition)
                    MusicPlayerActivity.startTime.text = currentTime
                    MusicPlayerActivity.handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    MusicPlayerActivity.seekBar.progress = 0
                }
            }
        }, 0)
    }

    private fun setSongDetails(songPosition: Int) {
        MusicPlayerActivity.titleTextView.text = MusicPlayerActivity.playList[songPosition].name
        Glide.with(application.applicationContext)
            .load(MusicPlayerActivity.playList[songPosition].albumArt)
            .error(R.drawable.music_logo)
            .into(MusicPlayerActivity.imageView)

        MainActivity.titleTextView.text = MusicPlayerActivity.playList[songPosition].name
        Glide.with(MainActivity.imageView)
            .load(MusicPlayerActivity.playList[songPosition].albumArt)
            .error(R.drawable.music_logo)
            .into(MainActivity.imageView)
        MainActivity.artist.text = MusicPlayerActivity.playList[songPosition].artist
        MainActivity.playBtn.setImageResource(R.drawable.pause_icon)
    }

}