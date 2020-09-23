package com.mallich.musicplayer.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.chinodev.androidneomorphframelayout.NeomorphFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.MusicService
import com.mallich.musicplayer.R
import com.mallich.musicplayer.interfaces.PlayerInterface
import com.mallich.musicplayer.models.SongDataModel
import java.lang.Exception

class MusicPlayerActivity : AppCompatActivity(), PlayerInterface {

    companion object {
        lateinit var imageView: RoundedImageView
        lateinit var titleTextView: TextView
        lateinit var startTime: TextView
        lateinit var endTime: TextView
        lateinit var seekBar: SeekBar
        lateinit var prevBtn: ImageView
        lateinit var nextBtn: ImageView
        lateinit var playBtn: FloatingActionButton
        lateinit var volumeSeekBar: SeekBar
        lateinit var neomorphFrameLayout: NeomorphFrameLayout
        lateinit var viewPager: ViewPager

        var PLAYER_ON: Boolean = false

        var mediaPlayer: MediaPlayer? = null
        val handler = Handler()
        var songPosition = -1
        var albumType: String? = ""
        var album: String? = ""
        var newSongPosition = -1
        var newAlbumType: String? = ""
        var newAlbum: String? = ""
        var playList: MutableList<SongDataModel> = mutableListOf()
        lateinit var audioManager: AudioManager

        @RequiresApi(Build.VERSION_CODES.R)
        fun setSongDetails(context: Context, songPosition: Int) {
            initializeSeekBar()

            if (mediaPlayer!!.isPlaying) {
                playBtn.setImageResource(R.drawable.pause_icon)
                MainActivity.playBtn.setImageResource(R.drawable.pause_icon)
            } else {
                playBtn.setImageResource(R.drawable.play_icon)
                MainActivity.playBtn.setImageResource(R.drawable.play_icon)
            }

            titleTextView.text = playList[songPosition].name
            Glide.with(context)
                .load(playList[songPosition].albumArt)
                .error(R.drawable.music_logo)
                .into(imageView)

            MainActivity.titleTextView.text = playList[songPosition].name
            Glide.with(MainActivity.imageView.context)
                .load(playList[songPosition].albumArt)
                .error(R.drawable.music_logo)
                .placeholder(R.drawable.music_logo)
                .into(MainActivity.imageView)
            MainActivity.artist.text = playList[songPosition].artist

            MusicService.createNotificationChannel(context)
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun playNewSong(context: Context) {
            val selectedSong =
                Uri.parse(playList[songPosition].fileData.toString())
            mediaPlayer = MediaPlayer.create(context, selectedSong)
            mediaPlayer?.start()
            playBtn.setImageResource(R.drawable.pause_icon)
            setSongDetails(context, songPosition)
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun playNextSong(context: Context) {
            val selectedSong =
                Uri.parse(playList[songPosition].fileData.toString())
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, selectedSong)
            mediaPlayer?.start()
            playBtn.setImageResource(R.drawable.pause_icon)
            setSongDetails(context, songPosition)
        }

        private fun initializeSeekBar() {
            seekBar.max = mediaPlayer!!.duration
            endTime.text = MusicRepository.getTimeString(mediaPlayer!!.duration)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        seekBar.progress = mediaPlayer!!.currentPosition
                        val currentTime =
                            MusicRepository.getTimeString(mediaPlayer!!.currentPosition)
                        startTime.text = currentTime
                        handler.postDelayed(this, 1000)
                    } catch (e: Exception) {
                        seekBar.progress = 0
                    }
                }
            }, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        imageView = findViewById(R.id.player_img)
        titleTextView = findViewById(R.id.player_album)
        startTime = findViewById(R.id.player_startTime)
        endTime = findViewById(R.id.player_endTime)
        seekBar = findViewById(R.id.player_seekBar)
        prevBtn = findViewById(R.id.player_prevBtn)
        nextBtn = findViewById(R.id.player_nextBtn)
        playBtn = findViewById(R.id.player_playBtn)
        volumeSeekBar = findViewById(R.id.player__volume_seekBar)
        neomorphFrameLayout = findViewById(R.id.player_backBtn)
        viewPager = findViewById(R.id.player_viewpager)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        newSongPosition = intent.getIntExtra(MusicRepository.SONG_POSITION, -1)
        newAlbumType = intent.getStringExtra(MusicRepository.TYPE)
        newAlbum = intent.getStringExtra(MusicRepository.ALBUM)
        if (newSongPosition == -1) {
            newSongPosition = MusicService.SONG_POSITION
            newAlbumType = MusicService.ALBUM_TYPE
            newAlbum = MusicService.ALBUM
        }

        loadSongToPlay(application)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    public fun loadSongToPlay(application: Application) {

        if (newAlbumType == MusicRepository.SINGLE_ALBUM) {
            playList.clear()
            playList.addAll(MusicRepository.getSingleAlbum(application, newAlbum!!))
        } else {
            playList.clear()
            playList.addAll(MusicRepository.getAllSongs(application))
        }

//        val viewPagerAdapter = PlayerViewPagerAdapter(
//            application,
//            supportFragmentManager,
//            MusicPlayerActivity(),
//            playList
//        )
//        viewPager.adapter = viewPagerAdapter

        if (mediaPlayer == null) {
            songPosition = newSongPosition
            albumType = newAlbumType
            album = newAlbum
            playNewSong(this)
        } else if (mediaPlayer != null) {
            if (album != newAlbum || albumType != newAlbumType || newSongPosition != songPosition) {
                album = newAlbum
                albumType = newAlbumType
                songPosition = newSongPosition
                playNextSong(this)
            } else {
                setSongDetails(this, songPosition)
            }
        }
        playBtn.setOnClickListener {
            if (!mediaPlayer!!.isPlaying && mediaPlayer != null) {
                mediaPlayer?.start()
                playBtn.setImageResource(R.drawable.pause_icon)
            } else {
                mediaPlayer?.pause()
                playBtn.setImageResource(R.drawable.play_icon)
            }
            setSongDetails(this, songPosition)
        }

        prevBtn.setOnClickListener {
            songPosition -= 1
            if (songPosition < 0) {
                songPosition = playList.size - 1
                playNextSong(this)
            } else {
                playNextSong(this)
            }
        }

        nextBtn.setOnClickListener {
            songPosition += 1
            if (songPosition < playList.size) {
                playNextSong(this)
            } else {
                songPosition = 0
                playNextSong(this)
            }
        }

        neomorphFrameLayout.setOnClickListener {
            onBackPressed()
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                    if (changed) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                songPosition += 1
                if (songPosition < playList.size) {
                    playNextSong(this@MusicPlayerActivity)
                } else {
                    seekBar.progress = 0
                    startTime.text = MusicRepository.getTimeString(0)
                    endTime.text = MusicRepository.getTimeString(mp!!.duration)
                }
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun playSong(songDataModel: SongDataModel, position: Int) {
//        newAlbum = songDataModel.album
//        newSongPosition = position
//        newAlbumType = albumType
//        loadSongToPlay()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

}